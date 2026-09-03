package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEffectManager
import com.example.audio.NetworkVoiceManager
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    EXPLORE,
    GAMES,
    MESSAGES,
    PROFILE
}

class MainViewModel(
    private val repository: VoiceChatRepository = VoiceChatRepository()
) : ViewModel() {

    val currentUser = repository.currentUser
    val rooms = repository.rooms
    val currentRoom = repository.currentRoom
    val roomMessages = repository.roomMessages
    val availableGifts = repository.availableGifts
    val latestGiftEvent = repository.latestGiftEvent
    val conversations = repository.conversations
    val directMessages = repository.directMessages

    // Real-time network voice engine state
    val networkVoiceState = NetworkVoiceManager.networkState

    // Active bottom navigation tab
    private val _selectedTab = MutableStateFlow(AppTab.EXPLORE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Selected category filter on Explore tab
    private val _selectedCategory = MutableStateFlow(RoomCategory.POPULAR)
    val selectedCategory: StateFlow<RoomCategory> = _selectedCategory.asStateFlow()

    // Search query on explore tab
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected conversation for DM screen
    private val _activeConversation = MutableStateFlow<Conversation?>(null)
    val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

    // Active Dialogs & Sheets
    private val _showGiftSheet = MutableStateFlow(false)
    val showGiftSheet: StateFlow<Boolean> = _showGiftSheet.asStateFlow()

    private val _selectedGiftReceiver = MutableStateFlow<MicSeat?>(null)
    val selectedGiftReceiver: StateFlow<MicSeat?> = _selectedGiftReceiver.asStateFlow()

    private val _showLudoGameDialog = MutableStateFlow(false)
    val showLudoGameDialog: StateFlow<Boolean> = _showLudoGameDialog.asStateFlow()

    private val _showLuckyWheelDialog = MutableStateFlow(false)
    val showLuckyWheelDialog: StateFlow<Boolean> = _showLuckyWheelDialog.asStateFlow()

    private val _showCreateRoomDialog = MutableStateFlow(false)
    val showCreateRoomDialog: StateFlow<Boolean> = _showCreateRoomDialog.asStateFlow()

    private val _showSoundboardSheet = MutableStateFlow(false)
    val showSoundboardSheet: StateFlow<Boolean> = _showSoundboardSheet.asStateFlow()

    private val _showWalletRechargeDialog = MutableStateFlow(false)
    val showWalletRechargeDialog: StateFlow<Boolean> = _showWalletRechargeDialog.asStateFlow()

    private val _showRoomSettingsDialog = MutableStateFlow(false)
    val showRoomSettingsDialog: StateFlow<Boolean> = _showRoomSettingsDialog.asStateFlow()

    private val _showNetworkVoiceDialog = MutableStateFlow(false)
    val showNetworkVoiceDialog: StateFlow<Boolean> = _showNetworkVoiceDialog.asStateFlow()

    // Ludo mini-game live state
    data class LudoState(
        val myDice: Int = 1,
        val isRolling: Boolean = false,
        val myScore: Int = 0,
        val opponentScore: Int = 0,
        val round: Int = 1,
        val statusText: String = "Tap 'Roll Dice' to start tournament round!"
    )
    private val _ludoState = MutableStateFlow(LudoState())
    val ludoState: StateFlow<LudoState> = _ludoState.asStateFlow()

    // Background speaking animation loop for rooms
    private var simulationJob: Job? = null

    init {
        startRoomAtmosphereSimulation()
        initNetworkVoiceObservers()
    }

    private fun initNetworkVoiceObservers() {
        // Remote seat synchronization from other devices over network
        NetworkVoiceManager.onRemoteSeatSyncListener = { roomId, seatIndex, eventType, userId, userName, avatarHex ->
            val room = repository.currentRoom.value
            if (room != null && room.id == roomId) {
                when (eventType) {
                    0 -> { // Take Seat
                        val remoteUser = UserProfile(
                            id = userId,
                            name = userName,
                            avatarEmoji = "🎙️",
                            avatarColorHex = if (avatarHex != 0L) avatarHex else 0xFF00E5FF,
                            level = 12
                        )
                        repository.updateRemoteSeatAssignment(seatIndex, remoteUser, isSpeaking = true)
                        repository.sendRoomMessage("$userName joined Mic Seat #${seatIndex + 1} over network 🌐", MessageType.SYSTEM)
                    }
                    1 -> { // Leave Seat
                        repository.updateRemoteSeatAssignment(seatIndex, null, isSpeaking = false)
                    }
                }
            }
        }

        // Observe network voice state to animate seat visualizers live with real mic volume
        viewModelScope.launch {
            NetworkVoiceManager.networkState.collect { netState ->
                val room = repository.currentRoom.value ?: return@collect
                val myId = currentUser.value.id
                val mySeat = room.seats.find { it.user?.id == myId }

                // 1. Update local user's seat with actual microphone volume
                if (mySeat != null && netState.isBroadcasting && !mySeat.isMuted) {
                    val isSpeaking = netState.localVolume > 0.04f
                    repository.updateSeatSpeaking(mySeat.seatIndex, isSpeaking, netState.localVolume)
                }

                // 2. Update remote seats based on incoming network audio packets
                for ((seatIdx, remoteInfo) in netState.remoteSpeakers) {
                    if (mySeat == null || seatIdx != mySeat.seatIndex) {
                        val isSpeaking = remoteInfo.amplitude > 0.04f
                        repository.updateSeatSpeaking(seatIdx, isSpeaking, remoteInfo.amplitude)
                    }
                }
            }
        }
    }

    private fun startRoomAtmosphereSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val sampleChatTexts = listOf(
                "Great voice brother! 🎶",
                "Hello everyone from Dhaka! 🇧🇩",
                "Sing that romantic song please! ❤️",
                "Who wants to play Ludo next? 🎲",
                "Welcome all new listeners! 👋",
                "That acoustic guitar sound is so soulful!",
                "Send some roses to the host! 🌹",
                "Hello from Chittagong! 🌟"
            )
            val randomUsers = listOf(
                Pair("Farid", 0xFF64B5F6),
                Pair("Rina", 0xFFFF80AB),
                Pair("Shuvo", 0xFF81C784),
                Pair("Nusrat", 0xFFFFD54F),
                Pair("Imran", 0xFFBA68C8)
            )

            while (true) {
                delay(3500)
                val room = repository.currentRoom.value
                if (room != null) {
                    // Randomly push a realistic chat or reaction
                    if (Random.nextInt(100) < 45) {
                        val user = randomUsers.random()
                        val text = sampleChatTexts.random()
                        val msg = RoomMessage(
                            id = "sim_${System.currentTimeMillis()}",
                            senderName = user.first,
                            senderLevel = Random.nextInt(5, 20),
                            senderAvatarColor = user.second,
                            content = text,
                            type = MessageType.CHAT
                        )
                        repository.sendRoomMessage(msg.content, MessageType.CHAT)
                    }
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun selectCategory(category: RoomCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun enterRoom(room: VoiceRoom) {
        repository.enterRoom(room)
        NetworkVoiceManager.onEnterRoom(room.id)
    }

    fun leaveRoom() {
        NetworkVoiceManager.stopTransmitting()
        NetworkVoiceManager.onLeaveRoom()
        repository.leaveCurrentRoom()
    }

    fun toggleMyMic() {
        repository.toggleMyMic()
        val room = repository.currentRoom.value ?: return
        val mySeat = room.seats.find { it.user?.id == currentUser.value.id }
        if (mySeat != null) {
            if (!mySeat.isMuted) {
                AudioEffectManager.playSoundEffect(700, 100)
                NetworkVoiceManager.startTransmitting(
                    roomId = room.id,
                    seatIndex = mySeat.seatIndex,
                    userId = currentUser.value.id,
                    userName = currentUser.value.name
                )
            } else {
                NetworkVoiceManager.stopTransmitting()
            }
        }
    }

    fun takeMicSeat(seatIndex: Int) {
        val room = repository.currentRoom.value ?: return
        repository.takeMicSeat(seatIndex)
        AudioEffectManager.playSoundEffect(800, 150)
        NetworkVoiceManager.startTransmitting(
            roomId = room.id,
            seatIndex = seatIndex,
            userId = currentUser.value.id,
            userName = currentUser.value.name
        )
    }

    fun leaveMicSeat(seatIndex: Int) {
        NetworkVoiceManager.stopTransmitting()
        repository.leaveMicSeat(seatIndex)
    }

    fun toggleNetworkVoiceDialog(show: Boolean) {
        _showNetworkVoiceDialog.value = show
    }

    fun addPeerIp(ipAddress: String) {
        NetworkVoiceManager.addManualTargetIp(ipAddress)
    }

    fun toggleVoiceLoopback(enabled: Boolean) {
        NetworkVoiceManager.setLoopbackEnabled(enabled)
    }

    fun refreshNetworkIp() {
        NetworkVoiceManager.updateLocalIp()
    }

    fun sendChatMessage(text: String) {
        if (text.isNotBlank()) {
            repository.sendRoomMessage(text, MessageType.CHAT)
        }
    }

    fun openGiftSheet(receiver: MicSeat? = null) {
        _selectedGiftReceiver.value = receiver
        _showGiftSheet.value = true
    }

    fun closeGiftSheet() {
        _showGiftSheet.value = false
        _selectedGiftReceiver.value = null
    }

    fun sendGift(gift: VirtualGift, multiplier: Int = 1) {
        repository.sendVirtualGift(gift, _selectedGiftReceiver.value, multiplier)
        AudioEffectManager.playGiftEffect()
        closeGiftSheet()
    }

    fun dismissGiftAnimation() {
        repository.clearLatestGiftEvent()
    }

    fun playSoundEffect(effect: RoomSoundEffect) {
        AudioEffectManager.playSoundEffect(effect.freq, 280)
        repository.sendRoomMessage("played ${effect.icon} ${effect.title} sound effect!", MessageType.REACTION)
        _showSoundboardSheet.value = false
    }

    fun toggleSoundboard(show: Boolean) {
        _showSoundboardSheet.value = show
    }

    fun toggleLudoGame(show: Boolean) {
        _showLudoGameDialog.value = show
    }

    fun toggleLuckyWheel(show: Boolean) {
        _showLuckyWheelDialog.value = show
    }

    fun toggleCreateRoom(show: Boolean) {
        _showCreateRoomDialog.value = show
    }

    fun toggleWalletRecharge(show: Boolean) {
        _showWalletRechargeDialog.value = show
    }

    fun toggleRoomSettings(show: Boolean) {
        _showRoomSettingsDialog.value = show
    }

    fun rollLudoDice() {
        if (_ludoState.value.isRolling) return
        viewModelScope.launch {
            _ludoState.update { it.copy(isRolling = true, statusText = "Rolling the dice... 🎲") }
            AudioEffectManager.playDiceRollSound()
            delay(500)
            val dice = Random.nextInt(1, 7)
            val oppDice = Random.nextInt(1, 7)
            val winDiff = dice - oppDice
            val newMyScore = _ludoState.value.myScore + (if (winDiff > 0) 100 else 20)
            val newOppScore = _ludoState.value.opponentScore + (if (winDiff < 0) 100 else 20)

            val resultMsg = when {
                winDiff > 0 -> "🎉 You won the roll! (+100 Tournament Coins)"
                winDiff < 0 -> "Opponent rolled higher! (+20 Coins)"
                else -> "It's a tie! Both got $dice 🤝"
            }

            if (winDiff > 0) {
                repository.rechargeCoins(100)
                AudioEffectManager.playGiftEffect()
            }

            _ludoState.update {
                it.copy(
                    myDice = dice,
                    isRolling = false,
                    myScore = newMyScore,
                    opponentScore = newOppScore,
                    round = it.round + 1,
                    statusText = resultMsg
                )
            }
            repository.sendRoomMessage("rolled a 🎲 $dice in Ludo tournament!", MessageType.DICE_ROLL)
        }
    }

    fun spinWheelResult(prizeCoins: Long, prizeName: String) {
        repository.rechargeCoins(prizeCoins)
        AudioEffectManager.playGiftEffect()
        repository.sendRoomMessage("won $prizeName ($prizeCoins coins) on the Lucky Wheel! 🎡✨", MessageType.REACTION)
    }

    fun createRoom(title: String, category: RoomCategory, tag: String, wallpaper: String) {
        repository.createRoom(title, category, tag, wallpaper)
        _showCreateRoomDialog.value = false
    }

    fun rechargeCoins(amount: Long) {
        repository.rechargeCoins(amount)
        AudioEffectManager.playGiftEffect()
        _showWalletRechargeDialog.value = false
    }

    fun exchangeDiamonds(amount: Long) {
        repository.exchangeDiamonds(amount)
        AudioEffectManager.playGiftEffect()
    }

    fun openConversation(conv: Conversation) {
        _activeConversation.value = conv
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    fun sendDirectMessage(convId: String, text: String, isVoice: Boolean = false, voiceSec: Int = 0) {
        if (text.isNotBlank() || isVoice) {
            repository.sendDirectMessage(convId, text, isVoice, voiceSec)
        }
    }
}
