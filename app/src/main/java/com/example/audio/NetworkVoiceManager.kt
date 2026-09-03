package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Real-time VoIP & LAN Audio Network Engine for YoHo Voice Chat.
 *
 * Allows users to take any seat in a room, record their microphone,
 * broadcast live audio over local network / Wi-Fi via UDP Datagram packets,
 * and receive / play live audio from all other phones in real time!
 */
object NetworkVoiceManager {

    private const val TAG = "NetworkVoiceManager"
    const val DEFAULT_PORT = 50005
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
    private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
    private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private const val FRAME_SAMPLE_COUNT = 320 // 20ms of 16kHz audio for minimal latency
    private const val FRAME_BYTE_SIZE = FRAME_SAMPLE_COUNT * 2 // 640 bytes

    private val MAGIC_BYTES = byteArrayOf('Y'.code.toByte(), 'H'.code.toByte(), 'V'.code.toByte(), 'C'.code.toByte())

    const val TYPE_AUDIO: Byte = 0x01
    const val TYPE_SEAT_EVENT: Byte = 0x02
    const val TYPE_DISCOVERY: Byte = 0x03

    // Network Voice UI State
    data class NetworkState(
        val localIp: String = "Detecting...",
        val isBroadcasting: Boolean = false,
        val isReceiving: Boolean = false,
        val isLoopbackEnabled: Boolean = false,
        val packetsSent: Long = 0L,
        val packetsReceived: Long = 0L,
        val localVolume: Float = 0f,
        val remoteSpeakers: Map<Int, RemoteSpeakerInfo> = emptyMap(),
        val knownPeerIps: List<String> = emptyList(),
        val currentRoomId: String = "",
        val currentSeatIndex: Int = -1,
        val networkError: String? = null
    )

    data class RemoteSpeakerInfo(
        val seatIndex: Int,
        val senderName: String,
        val amplitude: Float,
        val lastPacketTime: Long = System.currentTimeMillis()
    )

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordingJob: Job? = null
    private var listeningJob: Job? = null
    private var speakerExpiryJob: Job? = null

    private var udpSocket: DatagramSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private val manualTargetIps = ConcurrentHashMap.newKeySet<String>()
    private var wifiMulticastLock: WifiManager.MulticastLock? = null

    // Callback for seat synchronization over network
    var onRemoteSeatSyncListener: ((roomId: String, seatIndex: Int, eventType: Int, userId: String, userName: String, avatarHex: Long) -> Unit)? = null

    init {
        updateLocalIp()
    }

    fun init(context: Context) {
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifi != null && wifiMulticastLock == null) {
                wifiMulticastLock = wifi.createMulticastLock("YoHoVoiceMulticastLock").apply {
                    setReferenceCounted(true)
                    acquire()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire multicast lock: ${e.message}")
        }
        updateLocalIp()
        startListening()
        startSpeakerExpiryTimer()
    }

    fun updateLocalIp(): String {
        val ip = getLocalIpAddress()
        _networkState.update { it.copy(localIp = ip) }
        return ip
    }

    fun addManualTargetIp(ipAddress: String) {
        val clean = ipAddress.trim()
        if (clean.isNotEmpty()) {
            manualTargetIps.add(clean)
            _networkState.update { state ->
                val list = (state.knownPeerIps + clean).distinct()
                state.copy(knownPeerIps = list)
            }
            // Send discovery packet
            sendDiscoveryPacket()
        }
    }

    fun setLoopbackEnabled(enabled: Boolean) {
        _networkState.update { it.copy(isLoopbackEnabled = enabled) }
    }

    /**
     * Called when user enters a voice room or changes room.
     */
    fun onEnterRoom(roomId: String) {
        _networkState.update { it.copy(currentRoomId = roomId) }
        sendDiscoveryPacket()
    }

    /**
     * Called when user leaves the voice room.
     */
    fun onLeaveRoom() {
        stopTransmitting()
        _networkState.update {
            it.copy(
                currentRoomId = "",
                currentSeatIndex = -1,
                remoteSpeakers = emptyMap()
            )
        }
    }

    /**
     * Starts recording microphone audio and broadcasting to the network.
     * Called when the user takes a seat and is unmuted.
     */
    @SuppressLint("MissingPermission")
    fun startTransmitting(
        roomId: String,
        seatIndex: Int,
        userId: String,
        userName: String
    ) {
        if (recordingJob?.isActive == true && _networkState.value.currentSeatIndex == seatIndex) {
            return
        }
        stopTransmitting()

        _networkState.update {
            it.copy(
                isBroadcasting = true,
                currentRoomId = roomId,
                currentSeatIndex = seatIndex
            )
        }

        recordingJob = scope.launch {
            try {
                val minBuffer = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    AUDIO_ENCODING
                )
                val bufferSize = maxOf(minBuffer, FRAME_BYTE_SIZE * 4)

                val record = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    AUDIO_ENCODING,
                    bufferSize
                )

                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize")
                    _networkState.update { it.copy(networkError = "Microphone initialization failed", isBroadcasting = false) }
                    return@launch
                }

                audioRecord = record
                record.startRecording()

                val audioBuffer = ShortArray(FRAME_SAMPLE_COUNT)
                val byteBuffer = ByteArray(FRAME_BYTE_SIZE)

                ensureSocket()
                ensureAudioTrack()

                // Broadcast seat take event to all other phones
                broadcastSeatEvent(roomId, seatIndex, 0, userId, userName, 0xFFFF2E93)

                while (isActive && _networkState.value.isBroadcasting) {
                    val readSamples = record.read(audioBuffer, 0, FRAME_SAMPLE_COUNT)
                    if (readSamples > 0) {
                        // Calculate real-time amplitude / RMS
                        var sum = 0.0
                        for (i in 0 until readSamples) {
                            val sample = audioBuffer[i]
                            sum += sample * sample

                            // Convert short to little-endian bytes
                            byteBuffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        val rms = sqrt(sum / readSamples)
                        val normalizedVol = min(1.0f, (rms / 8000.0).toFloat())

                        _networkState.update { it.copy(localVolume = normalizedVol) }

                        // Loopback test mode
                        if (_networkState.value.isLoopbackEnabled) {
                            audioTrack?.write(byteBuffer, 0, readSamples * 2)
                        }

                        // Send audio packet over network
                        sendAudioPacket(
                            roomId = roomId,
                            seatIndex = seatIndex,
                            senderId = userId,
                            senderName = userName,
                            amplitude = normalizedVol,
                            audioData = byteBuffer,
                            length = readSamples * 2
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Recording error: ${e.message}", e)
                _networkState.update { it.copy(networkError = e.message, isBroadcasting = false) }
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
                _networkState.update { it.copy(isBroadcasting = false, localVolume = 0f) }
            }
        }
    }

    /**
     * Stops broadcasting microphone audio.
     */
    fun stopTransmitting() {
        val oldSeat = _networkState.value.currentSeatIndex
        val roomId = _networkState.value.currentRoomId
        _networkState.update { it.copy(isBroadcasting = false, localVolume = 0f) }
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        if (oldSeat >= 0 && roomId.isNotEmpty()) {
            broadcastSeatEvent(roomId, oldSeat, 1, "me", "me", 0)
        }
    }

    private fun ensureSocket(): DatagramSocket {
        var sock = udpSocket
        if (sock == null || sock.isClosed) {
            sock = DatagramSocket(DEFAULT_PORT).apply {
                broadcast = true
                reuseAddress = true
                receiveBufferSize = 64 * 1024
                sendBufferSize = 64 * 1024
            }
            udpSocket = sock
        }
        return sock
    }

    private fun ensureAudioTrack(): AudioTrack {
        var track = audioTrack
        if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
            val minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, AUDIO_ENCODING)
            val bufSize = maxOf(minBuf, FRAME_BYTE_SIZE * 4)

            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_ENCODING)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_OUT)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track.play()
            audioTrack = track
        }
        return track
    }

    /**
     * Starts listening for incoming UDP voice packets from other phones.
     */
    fun startListening() {
        if (listeningJob?.isActive == true) return

        listeningJob = scope.launch {
            val receiveBuffer = ByteArray(2048)
            val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)

            while (isActive) {
                try {
                    val socket = ensureSocket()
                    socket.receive(packet)

                    val senderAddress = packet.address.hostAddress ?: ""
                    val currentLocalIp = _networkState.value.localIp

                    // Ignore packets sent by ourselves if matching local IP
                    if (senderAddress == currentLocalIp && !_networkState.value.isLoopbackEnabled) {
                        continue
                    }

                    // Parse incoming packet
                    if (packet.length >= 5) {
                        val isMagic = receiveBuffer[0] == MAGIC_BYTES[0] &&
                                receiveBuffer[1] == MAGIC_BYTES[1] &&
                                receiveBuffer[2] == MAGIC_BYTES[2] &&
                                receiveBuffer[3] == MAGIC_BYTES[3]

                        if (isMagic) {
                            handleIncomingPacket(receiveBuffer, packet.length, senderAddress)
                        }
                    }
                } catch (e: Exception) {
                    if (e is SocketException && !isActive) break
                    // Exponential or short backoff on network failure
                    kotlinx.coroutines.delay(100)
                }
            }
        }
    }

    private fun handleIncomingPacket(data: ByteArray, length: Int, senderIp: String) {
        try {
            val bais = ByteArrayInputStream(data, 4, length - 4)
            val dis = DataInputStream(bais)

            val type = dis.readByte()
            val roomId = dis.readUTF()

            // Update peer list if new
            if (senderIp.isNotEmpty() && senderIp != _networkState.value.localIp) {
                if (!_networkState.value.knownPeerIps.contains(senderIp)) {
                    _networkState.update { it.copy(knownPeerIps = (it.knownPeerIps + senderIp).distinct()) }
                }
            }

            when (type) {
                TYPE_AUDIO -> {
                    val seatIndex = dis.readByte().toInt()
                    val senderId = dis.readUTF()
                    val senderName = dis.readUTF()
                    val ampInt = dis.readShort().toInt()
                    val amplitude = ampInt / 1000f
                    val audioLen = dis.readShort().toInt()

                    if (audioLen > 0 && dis.available() >= audioLen) {
                        val audioData = ByteArray(audioLen)
                        dis.readFully(audioData)

                        // If user is currently in this room, play the audio!
                        val currentRoom = _networkState.value.currentRoomId
                        if (currentRoom.isEmpty() || currentRoom == roomId) {
                            // Play audio on AudioTrack
                            ensureAudioTrack().write(audioData, 0, audioLen)

                            // Update active remote speaker status
                            _networkState.update { state ->
                                val updatedSpeakers = state.remoteSpeakers.toMutableMap()
                                updatedSpeakers[seatIndex] = RemoteSpeakerInfo(
                                    seatIndex = seatIndex,
                                    senderName = senderName,
                                    amplitude = amplitude,
                                    lastPacketTime = System.currentTimeMillis()
                                )
                                state.copy(
                                    packetsReceived = state.packetsReceived + 1,
                                    remoteSpeakers = updatedSpeakers,
                                    isReceiving = true
                                )
                            }
                        }
                    }
                }
                TYPE_SEAT_EVENT -> {
                    val seatIndex = dis.readByte().toInt()
                    val eventType = dis.readByte().toInt()
                    val userId = dis.readUTF()
                    val userName = dis.readUTF()
                    val avatarHex = dis.readLong()

                    onRemoteSeatSyncListener?.invoke(roomId, seatIndex, eventType, userId, userName, avatarHex)
                }
                TYPE_DISCOVERY -> {
                    val userName = dis.readUTF()
                    Log.d(TAG, "Discovered peer $userName at $senderIp")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode packet: ${e.message}")
        }
    }

    private fun sendAudioPacket(
        roomId: String,
        seatIndex: Int,
        senderId: String,
        senderName: String,
        amplitude: Float,
        audioData: ByteArray,
        length: Int
    ) {
        scope.launch {
            try {
                val baos = ByteArrayOutputStream()
                val dos = DataOutputStream(baos)

                dos.write(MAGIC_BYTES)
                dos.writeByte(TYPE_AUDIO.toInt())
                dos.writeUTF(roomId)
                dos.writeByte(seatIndex)
                dos.writeUTF(senderId)
                dos.writeUTF(senderName)
                dos.writeShort((amplitude * 1000).toInt())
                dos.writeShort(length)
                dos.write(audioData, 0, length)
                dos.flush()

                val packetBytes = baos.toByteArray()
                sendToAllDestinations(packetBytes)

                _networkState.update { it.copy(packetsSent = it.packetsSent + 1) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send audio packet: ${e.message}")
            }
        }
    }

    fun broadcastSeatEvent(
        roomId: String,
        seatIndex: Int,
        eventType: Int,
        userId: String,
        userName: String,
        avatarHex: Long
    ) {
        scope.launch {
            try {
                val baos = ByteArrayOutputStream()
                val dos = DataOutputStream(baos)

                dos.write(MAGIC_BYTES)
                dos.writeByte(TYPE_SEAT_EVENT.toInt())
                dos.writeUTF(roomId)
                dos.writeByte(seatIndex)
                dos.writeByte(eventType)
                dos.writeUTF(userId)
                dos.writeUTF(userName)
                dos.writeLong(avatarHex)
                dos.flush()

                sendToAllDestinations(baos.toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast seat event: ${e.message}")
            }
        }
    }

    private fun sendDiscoveryPacket() {
        scope.launch {
            try {
                val baos = ByteArrayOutputStream()
                val dos = DataOutputStream(baos)

                dos.write(MAGIC_BYTES)
                dos.writeByte(TYPE_DISCOVERY.toInt())
                dos.writeUTF(_networkState.value.currentRoomId)
                dos.writeUTF("YoHo Device")
                dos.flush()

                sendToAllDestinations(baos.toByteArray())
            } catch (_: Exception) {}
        }
    }

    private fun sendToAllDestinations(bytes: ByteArray) {
        val socket = ensureSocket()

        // 1. Broadcast address
        try {
            val broadcastAddr = InetAddress.getByName("255.255.255.255")
            val p = DatagramPacket(bytes, bytes.size, broadcastAddr, DEFAULT_PORT)
            socket.send(p)
        } catch (_: Exception) {}

        // 2. Subnet directed broadcast if possible
        try {
            val local = _networkState.value.localIp
            if (local.contains(".")) {
                val subnetBroadcast = local.substringBeforeLast(".") + ".255"
                val p = DatagramPacket(bytes, bytes.size, InetAddress.getByName(subnetBroadcast), DEFAULT_PORT)
                socket.send(p)
            }
        } catch (_: Exception) {}

        // 3. Any manually registered or auto-discovered peer IPs
        for (ip in manualTargetIps) {
            try {
                val p = DatagramPacket(bytes, bytes.size, InetAddress.getByName(ip), DEFAULT_PORT)
                socket.send(p)
            } catch (_: Exception) {}
        }
    }

    private fun startSpeakerExpiryTimer() {
        speakerExpiryJob?.cancel()
        speakerExpiryJob = scope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(350)
                val now = System.currentTimeMillis()
                val currentSpeakers = _networkState.value.remoteSpeakers
                if (currentSpeakers.isNotEmpty()) {
                    val active = currentSpeakers.filter { now - it.value.lastPacketTime < 500 }
                    if (active.size != currentSpeakers.size) {
                        _networkState.update {
                            it.copy(
                                remoteSpeakers = active,
                                isReceiving = active.isNotEmpty()
                            )
                        }
                    }
                } else if (_networkState.value.isReceiving) {
                    _networkState.update { it.copy(isReceiving = false) }
                }
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr.hostAddress?.contains(':') == false) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    fun release() {
        stopTransmitting()
        listeningJob?.cancel()
        speakerExpiryJob?.cancel()
        try {
            udpSocket?.close()
        } catch (_: Exception) {}
        udpSocket = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null

        try {
            wifiMulticastLock?.release()
        } catch (_: Exception) {}
        wifiMulticastLock = null
    }
}
