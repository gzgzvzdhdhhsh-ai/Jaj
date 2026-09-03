package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun GamesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YoHo Games & Arena 🎲",
                    color = VibrantTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VibrantSurfaceVariant)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = "🪙 ${currentUser.coins}", color = VibrantGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Ludo Game Card Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleLudoGame(true) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.banner_ludo),
                        contentDescription = "Ludo Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibrantTertiary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "MOST POPULAR",
                                        color = VibrantOnTertiary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ludo Party Live",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Play with room friends & roll the dice! 🎲",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleLudoGame(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(text = "Play", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lucky Wheel Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantBorder, RoundedCornerShape(20.dp))
                    .clickable { viewModel.toggleLuckyWheel(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎡", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Lucky Fortune Wheel",
                                color = VibrantTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Win Jackpots up to 5,000 Coins & VIP frames",
                                color = VibrantGold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleLuckyWheel(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantGoldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGold),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(text = "Spin", color = VibrantGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Gaming Champions Leaderboard
        item {
            Text(
                text = "🏆 Weekly Gaming Champions",
                color = VibrantTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val champions = listOf(
                Triple("Sara Queen", "12,450 🪙 won", "🥇"),
                Triple("Tanvir", "9,800 🪙 won", "🥈"),
                Triple("Priya", "7,200 🪙 won", "🥉"),
                Triple("Kabir", "5,100 🪙 won", "4th")
            )

            champions.forEach { (name, score, rank) ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = rank, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = name, color = VibrantTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(text = score, color = VibrantGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
