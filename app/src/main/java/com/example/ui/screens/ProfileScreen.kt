package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedLanguage by remember { mutableStateOf("English") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar with VIP Border
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(currentUser.avatarColorHex).copy(alpha = 0.25f))
                                .border(2.dp, VibrantGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentUser.avatarEmoji, fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.name,
                                    color = VibrantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = currentUser.countryFlag, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "YoHo ID: 88492041",
                                color = VibrantTextMuted,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibrantGold)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentUser.vipBadge,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibrantPrimary)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Lv.${currentUser.level}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentUser.bio,
                        color = VibrantTextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats: Following, Followers, Charm, Wealth
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(VibrantSurfaceVariant)
                            .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "142", color = VibrantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Following", color = VibrantTextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "1,890", color = VibrantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Followers", color = VibrantTextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Lv.${currentUser.charmLevel}", color = VibrantPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Charm", color = VibrantTextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Lv.${currentUser.wealthLevel}", color = VibrantGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Wealth", color = VibrantTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Wallet & Store Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Wallet & Vault 🪙",
                            color = VibrantTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewModel.toggleWalletRecharge(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = "+ Recharge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Gold Coins
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Gold Coins", color = VibrantTextSecondary, fontSize = 12.sp)
                            Text(
                                text = "🪙 ${currentUser.coins}",
                                color = VibrantGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        // Diamonds
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Diamonds", color = VibrantTextSecondary, fontSize = 12.sp)
                            Text(
                                text = "💎 ${currentUser.diamonds}",
                                color = VibrantCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Backpack & Noble Privileges
        item {
            Text(
                text = "Backpack & Aristocracy 👑",
                color = VibrantTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 1: Avatar Frames
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🖼️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "VIP Frames", color = VibrantTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "3 Active", color = VibrantGold, fontSize = 10.sp)
                    }
                }

                // Item 2: Entry Effects
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🚀", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Entry Car", color = VibrantTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Ferrari Glow", color = VibrantPrimary, fontSize = 10.sp)
                    }
                }

                // Item 3: Room Themes
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌌", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Wallpapers", color = VibrantTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Galaxy Party", color = VibrantCyan, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Language & App Settings
        item {
            Text(
                text = "Settings & Support ⚙️",
                color = VibrantTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Language Switcher Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = "Language", tint = VibrantPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "App Language", color = VibrantTextPrimary, fontSize = 14.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("English", "বাংলা").forEach { lang ->
                                val isSel = selectedLanguage == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) VibrantPrimary else VibrantSurfaceVariant)
                                        .border(1.dp, if (isSel) Color.Transparent else VibrantBorder, RoundedCornerShape(10.dp))
                                        .clickable { selectedLanguage = lang }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = lang,
                                        color = if (isSel) Color.White else VibrantTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = "Privacy", tint = VibrantGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Safety & Moderation", color = VibrantTextPrimary, fontSize = 14.sp)
                        }
                        Text(text = "24/7 Active", color = VibrantGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "About", tint = VibrantPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Version", color = VibrantTextPrimary, fontSize = 14.sp)
                        }
                        Text(text = "v3.2.0 (YoHo Build)", color = VibrantTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
