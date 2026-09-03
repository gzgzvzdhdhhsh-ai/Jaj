package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val availableGifts = viewModel.availableGifts
    val showGiftSheet by viewModel.showGiftSheet.collectAsState()
    val selectedReceiver by viewModel.selectedGiftReceiver.collectAsState()
    val latestGiftEvent by viewModel.latestGiftEvent.collectAsState()
    val showLudoDialog by viewModel.showLudoGameDialog.collectAsState()
    val ludoState by viewModel.ludoState.collectAsState()
    val showLuckyWheel by viewModel.showLuckyWheelDialog.collectAsState()
    val showCreateRoom by viewModel.showCreateRoomDialog.collectAsState()
    val showSoundboard by viewModel.showSoundboardSheet.collectAsState()
    val showWalletRecharge by viewModel.showWalletRechargeDialog.collectAsState()

    var isRoomMinimized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main Tab Content
        Scaffold(
            bottomBar = {
                Column {
                    // Floating Mini Room Player Bar (if in room & minimized)
                    if (currentRoom != null && isRoomMinimized) {
                        MiniRoomPlayer(
                            room = currentRoom!!,
                            onExpand = { isRoomMinimized = false },
                            onLeave = {
                                viewModel.leaveRoom()
                                isRoomMinimized = false
                            }
                        )
                    }

                    // Bottom Navigation Bar
                    YoHoBottomNavigationBar(
                        selectedTab = selectedTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    AppTab.EXPLORE -> HomeScreen(
                        viewModel = viewModel,
                        onEnterRoom = { room ->
                            viewModel.enterRoom(room)
                            isRoomMinimized = false
                        }
                    )
                    AppTab.GAMES -> GamesScreen(viewModel = viewModel)
                    AppTab.MESSAGES -> MessagesScreen(viewModel = viewModel)
                    AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }

        // Full Screen Live Room Overlay (when room is open and not minimized)
        if (currentRoom != null && !isRoomMinimized) {
            LiveRoomScreen(
                room = currentRoom!!,
                viewModel = viewModel,
                onClose = { isRoomMinimized = true }
            )
        }

        // Global Gift Celebration Animation Overlay
        GiftAnimationOverlay(
            event = latestGiftEvent,
            onDismiss = { viewModel.dismissGiftAnimation() }
        )

        // Gift Bottom Sheet
        if (showGiftSheet) {
            GiftBottomSheet(
                gifts = availableGifts,
                userCoins = currentUser.coins,
                receiverSeat = selectedReceiver,
                seats = currentRoom?.seats ?: emptyList(),
                onSendGift = { gift, multiplier ->
                    viewModel.sendGift(gift, multiplier)
                },
                onDismiss = { viewModel.closeGiftSheet() },
                onRechargeClick = {
                    viewModel.closeGiftSheet()
                    viewModel.toggleWalletRecharge(true)
                }
            )
        }

        // Soundboard Bottom Sheet
        if (showSoundboard) {
            SoundboardBottomSheet(
                onPlayEffect = { viewModel.playSoundEffect(it) },
                onDismiss = { viewModel.toggleSoundboard(false) }
            )
        }

        // Ludo Mini Game Dialog
        if (showLudoDialog) {
            LudoMiniGameDialog(
                state = ludoState,
                onRollDice = { viewModel.rollLudoDice() },
                onDismiss = { viewModel.toggleLudoGame(false) }
            )
        }

        // Lucky Fortune Wheel Dialog
        if (showLuckyWheel) {
            LuckyWheelDialog(
                userCoins = currentUser.coins,
                onSpinPrize = { coins, name ->
                    viewModel.spinWheelResult(coins, name)
                },
                onDismiss = { viewModel.toggleLuckyWheel(false) }
            )
        }

        // Create Room Dialog
        if (showCreateRoom) {
            CreateRoomDialog(
                onCreateRoom = { title, cat, tag, wallpaper ->
                    viewModel.createRoom(title, cat, tag, wallpaper)
                    isRoomMinimized = false
                },
                onDismiss = { viewModel.toggleCreateRoom(false) }
            )
        }

        // Wallet Recharge & Diamonds Exchange Dialog
        if (showWalletRecharge) {
            WalletRechargeDialog(
                currentCoins = currentUser.coins,
                currentDiamonds = currentUser.diamonds,
                onRecharge = { viewModel.rechargeCoins(it) },
                onExchangeDiamonds = { viewModel.exchangeDiamonds(it) },
                onDismiss = { viewModel.toggleWalletRecharge(false) }
            )
        }
    }
}

@Composable
fun MiniRoomPlayer(
    room: com.example.data.VoiceRoom,
    onExpand: () -> Unit,
    onLeave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(VibrantPrimary, VibrantPrimaryGradientEnd)
                )
            )
            .border(1.dp, VibrantPrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable { onExpand() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            EqualizerBars(barCount = 4, height = 16.dp, color = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = room.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to enter • Host: ${room.host.name}",
                    color = VibrantPrimaryContainer,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onLeave,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Leave",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun YoHoBottomNavigationBar(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = VibrantSurfaceVariant,
        tonalElevation = 6.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, VibrantBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = selectedTab == AppTab.EXPLORE,
            onClick = { onSelectTab(AppTab.EXPLORE) },
            icon = {
                Icon(Icons.Default.Radio, contentDescription = "Rooms")
            },
            label = {
                Text(text = "Rooms", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VibrantPrimary,
                selectedTextColor = VibrantPrimary,
                unselectedIconColor = VibrantTextSecondary,
                unselectedTextColor = VibrantTextSecondary,
                indicatorColor = VibrantSecondaryContainer
            ),
            modifier = Modifier.testTag("nav_explore")
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.GAMES,
            onClick = { onSelectTab(AppTab.GAMES) },
            icon = {
                Icon(Icons.Default.Casino, contentDescription = "Games")
            },
            label = {
                Text(text = "Games", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VibrantPrimary,
                selectedTextColor = VibrantPrimary,
                unselectedIconColor = VibrantTextSecondary,
                unselectedTextColor = VibrantTextSecondary,
                indicatorColor = VibrantSecondaryContainer
            ),
            modifier = Modifier.testTag("nav_games")
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.MESSAGES,
            onClick = { onSelectTab(AppTab.MESSAGES) },
            icon = {
                Icon(Icons.Default.ChatBubble, contentDescription = "Messages")
            },
            label = {
                Text(text = "Messages", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VibrantPrimary,
                selectedTextColor = VibrantPrimary,
                unselectedIconColor = VibrantTextSecondary,
                unselectedTextColor = VibrantTextSecondary,
                indicatorColor = VibrantSecondaryContainer
            ),
            modifier = Modifier.testTag("nav_messages")
        )

        NavigationBarItem(
            selected = selectedTab == AppTab.PROFILE,
            onClick = { onSelectTab(AppTab.PROFILE) },
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Me")
            },
            label = {
                Text(text = "Me", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VibrantPrimary,
                selectedTextColor = VibrantPrimary,
                unselectedIconColor = VibrantTextSecondary,
                unselectedTextColor = VibrantTextSecondary,
                indicatorColor = VibrantSecondaryContainer
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
