package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RoomCategory
import com.example.data.VoiceRoom
import com.example.ui.MainViewModel
import com.example.ui.components.EqualizerBars
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onEnterRoom: (VoiceRoom) -> Unit,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.rooms.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredRooms = remember(rooms, selectedCategory, searchQuery) {
        rooms.filter { room ->
            val matchesCategory = if (selectedCategory == RoomCategory.POPULAR) true
            else room.category == selectedCategory

            val matchesSearch = if (searchQuery.isBlank()) true
            else room.title.contains(searchQuery, ignoreCase = true) ||
                    room.host.name.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Top Bar: App Logo & User Coin Counter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, VibrantPrimary, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_yoho_logo),
                                contentDescription = "YoHo Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "YoHo Live",
                                color = VibrantTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Group Voice Party",
                                color = VibrantPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Coins Button / Recharge Trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(VibrantSurfaceVariant)
                            .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleWalletRecharge(true) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = "🪙", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currentUser.coins}",
                            color = VibrantGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(VibrantPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Coins",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search rooms, friends, music, Ludo...",
                            color = VibrantTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = VibrantTextSecondary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VibrantTextPrimary,
                        unfocusedTextColor = VibrantTextPrimary,
                        focusedBorderColor = VibrantPrimary,
                        unfocusedBorderColor = VibrantBorder,
                        focusedContainerColor = VibrantSurface,
                        unfocusedContainerColor = VibrantSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Feature Party Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(134.dp)
                        .clickable {
                            rooms.firstOrNull()?.let { onEnterRoom(it) }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(VibrantPrimary, VibrantPrimaryGradientEnd)
                                )
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.banner_voice_party),
                            contentDescription = "Voice Party Banner",
                            contentScale = ContentScale.Crop,
                            alpha = 0.35f,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibrantTertiary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE NOW",
                                        color = VibrantOnTertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Join Live Voice Rooms",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Chat, Sing & Play Ludo With Friends 🎤",
                                    color = VibrantPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(RoomCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(VibrantPrimary, VibrantPrimaryGradientEnd))
                                    else Brush.horizontalGradient(listOf(VibrantSurfaceVariant, VibrantSurfaceVariant))
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else VibrantBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = category.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = category.label,
                                    color = if (isSelected) Color.White else VibrantTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Room Cards List
            items(filteredRooms) { room ->
                RoomCard(
                    room = room,
                    onClick = { onEnterRoom(room) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Floating Action Button: Create Room
        FloatingActionButton(
            onClick = { viewModel.toggleCreateRoom(true) },
            containerColor = VibrantPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Room")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Go Live", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoomCard(
    room: VoiceRoom,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VibrantSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Host Avatar, Name, Country, Live Badge, Listeners
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Host Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(room.host.avatarColorHex).copy(alpha = 0.25f))
                            .border(1.5.dp, VibrantGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = room.host.avatarEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = room.host.name,
                                color = VibrantTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = room.host.countryFlag, fontSize = 12.sp)
                        }
                        Text(
                            text = room.host.vipBadge,
                            color = VibrantGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Live Equalizer & Online count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantSurfaceVariant)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    EqualizerBars(barCount = 3, height = 12.dp, color = VibrantPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "👥 ${room.onlineCount}",
                        color = VibrantTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room Title
            Text(
                text = room.title,
                color = VibrantTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Info: Mic Seats count & Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Occupied Mic Avatars preview
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val occupiedSeats = room.seats.filter { it.user != null }
                    occupiedSeats.take(4).forEachIndexed { index, seat ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = (-6 * index).dp)
                                .clip(CircleShape)
                                .background(Color(seat.user?.avatarColorHex ?: 0xFF9C27B0))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = seat.user?.avatarEmoji ?: "🎤", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(if (occupiedSeats.size > 1) 6.dp else 4.dp))

                    Text(
                        text = "🎙️ ${occupiedSeats.size}/8 on Mic",
                        color = VibrantPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    room.tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VibrantSurfaceVariant)
                                .border(1.dp, VibrantBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = VibrantTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
