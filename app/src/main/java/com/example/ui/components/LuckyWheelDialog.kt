package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun LuckyWheelDialog(
    userCoins: Long,
    onSpinPrize: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var winMessage by remember { mutableStateOf("Spin the wheel for 50 🪙 to win up to 5,000 Coins!") }

    val prizes = listOf(
        Pair(100L, "100 Coins 🪙"),
        Pair(20L, "20 Coins 🪙"),
        Pair(500L, "500 Coins 💎"),
        Pair(50L, "Free Rose 🌹"),
        Pair(1000L, "1,000 Coins 💰"),
        Pair(200L, "200 Coins 🪙"),
        Pair(5000L, "JACKPOT 5,000 👑"),
        Pair(300L, "300 Coins 🪙")
    )

    val spinAnimation = remember { Animatable(0f) }

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lucky Fortune Wheel",
                            color = VibrantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    if (!isSpinning) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Spinning Wheel
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(4.dp, VibrantGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Wheel Canvas / Dial
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationAngle)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF9C27B0),
                                        Color(0xFF2196F3),
                                        Color(0xFF00BCD4),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFFEB3B),
                                        Color(0xFFFF9800),
                                        Color(0xFFE91E63)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑 💎 🌹 🪙 ⭐ 🎁",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    // Center Pointer & Pin
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(VibrantSurface)
                            .border(3.dp, VibrantGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔻",
                            fontSize = 20.sp,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result / Status text
                Text(
                    text = winMessage,
                    color = VibrantTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Spin Button
                Button(
                    onClick = {
                        if (isSpinning) return@Button
                        if (userCoins < 50) {
                            winMessage = "⚠️ You need at least 50 coins to spin!"
                            return@Button
                        }
                        isSpinning = true
                        winMessage = "Spinning the wheel... Good luck! 🍀"

                        coroutineScope.launch {
                            val selectedIndex = Random.nextInt(prizes.size)
                            val prize = prizes[selectedIndex]
                            val extraSpins = 360f * (4 + Random.nextInt(3))
                            val targetAngle = extraSpins + (selectedIndex * (360f / prizes.size))

                            spinAnimation.snapTo(rotationAngle % 360f)
                            spinAnimation.animateTo(
                                targetValue = rotationAngle + targetAngle,
                                animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing)
                            ) {
                                rotationAngle = value
                            }

                            winMessage = "🎉 Congratulations! You won ${prize.second}!"
                            isSpinning = false
                            onSpinPrize(prize.first, prize.second)
                        }
                    },
                    enabled = !isSpinning,
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(46.dp)
                ) {
                    Text(
                        text = if (isSpinning) "Spinning... 🎡" else "Spin Now (50 🪙)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
