package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.audio.NetworkVoiceManager
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.components.AvatarWithBadge
import com.example.ui.components.EqualizerBars
import com.example.ui.components.NetworkVoiceDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LiveRoomScreen(
    room: VoiceRoom,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.roomMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRoomLive by viewModel.currentRoom.collectAsState()
    val activeRoom = currentRoomLive ?: room
    val networkVoiceState by viewModel.networkVoiceState.collectAsState()
    val showNetworkVoiceDialog by viewModel.showNetworkVoiceDialog.collectAsState()

    var chatInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var pendingSeatIndex by remember { mutableStateOf<Int?>(null) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingSeatIndex?.let { seatIdx ->
                viewModel.takeMicSeat(seatIdx)
                pendingSeatIndex = null
            }
        }
    }

    // Auto scroll chat to bottom when messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isUserOnMic = activeRoom.seats.any { it.user?.id == currentUser.id }
    val mySeat = activeRoom.seats.find { it.user?.id == currentUser.id }

    Box(modifier = Modifier.fillMaxSize()) {
        // Room Background Wallpaper
        Image(
            painter = painterResource(id = R.drawable.room_bg_party),
            contentDescription = "Room Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color(0xFF0E0720).copy(alpha = 0.82f),
                            Color(0xFF0E0720).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // Main Room Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Room Top Bar
            RoomTopBar(
                room = activeRoom,
                onClose = onClose,
                onGameClick = { viewModel.toggleLudoGame(true) },
                onWheelClick = { viewModel.toggleLuckyWheel(true) },
                onNetworkClick = { viewModel.toggleNetworkVoiceDialog(true) }
            )

            // 2. PK Battle Bar (if PK mode)
            if (activeRoom.roomMode == RoomMode.PK_BATTLE) {
                PkBattleBar(leftScore = activeRoom.pkScoreLeft, rightScore = activeRoom.pkScoreRight)
            }

            // 3. Room Notice Ticker
            RoomNoticeBar(text = activeRoom.announcement)

            // 3.5. Live Network Voice Status Bar
            NetworkVoiceStatusBar(
                networkState = networkVoiceState,
                isUserOnMic = isUserOnMic,
                isMuted = mySeat?.isMuted ?: true,
                onClick = { viewModel.toggleNetworkVoiceDialog(true) }
            )

            if (isUserOnMic && !(mySeat?.isMuted ?: true)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantGreen.copy(alpha = 0.16f))
                        .border(1.dp, VibrantGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎙️ আপনি সিটে আছেন: ফোনে কথা বলুন, অন্য সব ফোনে শোনা যাচ্ছে!",
                        color = VibrantGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Voice Stage (8 Mic Seats)
            VoiceStageSeats(
                seats = activeRoom.seats,
                currentUserId = currentUser.id,
                onSeatClick = { seat ->
                    if (seat.user == null) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.takeMicSeat(seat.seatIndex)
                        } else {
                            pendingSeatIndex = seat.seatIndex
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else if (seat.user.id == currentUser.id) {
                        viewModel.leaveMicSeat(seat.seatIndex)
                    } else {
                        viewModel.openGiftSheet(seat)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. In-Room Interactive Mini Features Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ludo Quick Game Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A154D))
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleLudoGame(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎲 Ludo Arena", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Lucky Wheel Spin Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A154D))
                        .border(1.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleLuckyWheel(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎡 Lucky Spin", color = RoyalGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Soundboard Reactions Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A154D))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleSoundboard(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👏 Soundboard", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Live Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Bottom Control Bar (Input, Mic, Soundboard, Gift)
            RoomBottomBar(
                chatInput = chatInput,
                onInputChange = { chatInput = it },
                onSend = {
                    viewModel.sendChatMessage(chatInput)
                    chatInput = ""
                },
                isUserOnMic = isUserOnMic,
                isMuted = mySeat?.isMuted ?: true,
                onToggleMic = { viewModel.toggleMyMic() },
                onOpenGiftSheet = { viewModel.openGiftSheet(null) },
                onOpenSoundboard = { viewModel.toggleSoundboard(true) },
                onOpenNetworkVoice = { viewModel.toggleNetworkVoiceDialog(true) },
                isTransmitting = networkVoiceState.isBroadcasting
            )
        }

        // Network Voice Dialog
        if (showNetworkVoiceDialog) {
            NetworkVoiceDialog(
                networkState = networkVoiceState,
                isUserOnMic = isUserOnMic,
                isMuted = mySeat?.isMuted ?: true,
                onDismiss = { viewModel.toggleNetworkVoiceDialog(false) },
                onAddPeerIp = { ip -> viewModel.addPeerIp(ip) },
                onToggleLoopback = { enabled -> viewModel.toggleVoiceLoopback(enabled) },
                onRefreshIp = { viewModel.refreshNetworkIp() }
            )
        }
    }
}

@Composable
fun RoomTopBar(
    room: VoiceRoom,
    onClose: () -> Unit,
    onGameClick: () -> Unit,
    onWheelClick: () -> Unit,
    onNetworkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Room Info & Host Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(room.host.avatarColorHex))
                    .border(1.dp, RoyalGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = room.host.avatarEmoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = room.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 130.dp)
                )
                Text(
                    text = "ID: ${room.id.takeLast(6)} • ${room.host.name}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }

        // Right actions: Online counter, Network Voice button & Close
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNetworkClick,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Text(text = "🌐", fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "👥 ${room.onlineCount}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Leave Room",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun NetworkVoiceStatusBar(
    networkState: NetworkVoiceManager.NetworkState,
    isUserOnMic: Boolean,
    isMuted: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_net")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val (barColor, text) = when {
        networkState.isBroadcasting -> Pair(VibrantGreen, "🎙️ নেটওয়ার্ক ভয়েস চালু: কথা বলছেন (সব ফোনে শোনা যাচ্ছে)")
        networkState.isReceiving -> Pair(VibrantCyan, "🔊 অন্য ফোন থেকে কথা আসছে (Live Network Audio)")
        isUserOnMic && isMuted -> Pair(Color(0xFFFF9800), "🔇 মাইক মিউট আছে — কথা বলতে আনমিউট করুন")
        isUserOnMic -> Pair(VibrantPrimary, "🎙️ আপনি সিটে আছেন — কথা বললে সব ফোনে পৌঁছাবে")
        else -> Pair(VibrantPrimary, "🌐 নেটওয়ার্ক ভয়েস কানেক্টেড: সিটে উঠে কথা বলুন • IP: ${networkState.localIp}")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, barColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(barColor.copy(alpha = dotAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(barColor.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "সেটিংস ⚙️",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PkBattleBar(leftScore: Int, rightScore: Int) {
    val total = (leftScore + rightScore).coerceAtLeast(1)
    val leftFraction = leftScore.toFloat() / total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "🔥 Red: $leftScore", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "⚡ PK BATTLE ⚡", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(text = "Blue: $rightScore ⚡", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(leftFraction.coerceIn(0.1f, 0.9f))
                    .fillMaxHeight()
                    .background(NeonPink)
            )
            Box(
                modifier = Modifier
                    .weight((1f - leftFraction).coerceIn(0.1f, 0.9f))
                    .fillMaxHeight()
                    .background(NeonCyan)
            )
        }
    }
}

@Composable
fun RoomNoticeBar(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF22113A).copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📢 ", fontSize = 11.sp)
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VoiceStageSeats(
    seats: List<MicSeat>,
    currentUserId: String,
    onSeatClick: (MicSeat) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Row: Seats 0..3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            seats.take(4).forEach { seat ->
                MicSeatView(
                    seat = seat,
                    isMe = seat.user?.id == currentUserId,
                    onClick = { onSeatClick(seat) }
                )
            }
        }

        // Lower Row: Seats 4..7
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            seats.drop(4).take(4).forEach { seat ->
                MicSeatView(
                    seat = seat,
                    isMe = seat.user?.id == currentUserId,
                    onClick = { onSeatClick(seat) }
                )
            }
        }
    }
}

@Composable
fun MicSeatView(
    seat: MicSeat,
    isMe: Boolean,
    onClick: () -> Unit
) {
    val user = seat.user
    val isHost = seat.seatIndex == 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            if (user != null) {
                AvatarWithBadge(
                    user = user,
                    size = 48.dp,
                    isSpeaking = seat.isSpeaking && !seat.isMuted,
                    isHost = isHost
                )

                // Mic Mute Status Indicator
                if (seat.isMuted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Muted",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            } else {
                // Empty Seat Slot
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B0E33).copy(alpha = 0.8f))
                        .border(
                            1.dp,
                            if (seat.isLocked) Color.Red.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (seat.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Seat",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Take Seat",
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Seat Name / Label
        val seatLabel = when {
            user != null && isMe -> "আপনি 🎙️"
            user != null -> user.name
            isHost -> "হোস্ট মাইক"
            else -> "সিট ${seat.seatIndex + 1}"
        }

        Text(
            text = seatLabel,
            color = if (user != null) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (user != null) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Live speaking equalizer indicator under avatar
        if (seat.isSpeaking && !seat.isMuted) {
            Spacer(modifier = Modifier.height(2.dp))
            EqualizerBars(barCount = 3, height = 8.dp)
        }
    }
}

@Composable
fun ChatMessageItem(message: RoomMessage) {
    when (message.type) {
        MessageType.SYSTEM -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF7928CA).copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color(0xFFE2D6FF),
                    fontSize = 11.sp
                )
            }
        }
        MessageType.GIFT -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF2E93).copy(alpha = 0.4f), Color(0xFFFFD166).copy(alpha = 0.3f))
                        )
                    )
                    .border(1.dp, RoyalGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lv.${message.senderLevel} ${message.senderName}: ",
                        color = RoyalGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        else -> {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Sender level badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonPink)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${message.senderLevel}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${message.senderName}: ",
                    color = Color(message.senderAvatarColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RoomBottomBar(
    chatInput: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isUserOnMic: Boolean,
    isMuted: Boolean,
    onToggleMic: () -> Unit,
    onOpenGiftSheet: () -> Unit,
    onOpenSoundboard: () -> Unit,
    onOpenNetworkVoice: () -> Unit,
    isTransmitting: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF140A28))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chat text input field
        OutlinedTextField(
            value = chatInput,
            onValueChange = onInputChange,
            placeholder = {
                Text(text = "Chat with room...", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = DarkCardSurface,
                unfocusedContainerColor = DarkCardSurface
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send chat button (if typed)
        if (chatInput.isNotBlank()) {
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonPink)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Mic Mute/Unmute Toggle (if on mic)
        if (isUserOnMic) {
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.Red else NeonCyan)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic Toggle",
                    tint = if (isMuted) Color.White else Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Network Voice Status & Settings button
        IconButton(
            onClick = onOpenNetworkVoice,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isTransmitting) VibrantGreen.copy(alpha = 0.35f) else Color(0xFF2B174D))
                .border(1.dp, if (isTransmitting) VibrantGreen else Color.Transparent, CircleShape)
        ) {
            Text(text = "🌐", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Soundboard reactions button
        IconButton(
            onClick = onOpenSoundboard,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF2B174D))
        ) {
            Text(text = "👏", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Send Gift Button
        IconButton(
            onClick = onOpenGiftSheet,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(RoyalGold, NeonPink)
                    )
                )
        ) {
            Text(text = "🎁", fontSize = 20.sp)
        }
    }
}
