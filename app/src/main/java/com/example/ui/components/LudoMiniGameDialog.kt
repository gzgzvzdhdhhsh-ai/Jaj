package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun LudoMiniGameDialog(
    state: MainViewModel.LudoState,
    onRollDice: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_wobble")
    val diceRotation by infiniteTransition.animateFloat(
        initialValue = if (state.isRolling) -15f else 0f,
        targetValue = if (state.isRolling) 15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎲", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ludo Voice Arena",
                            color = VibrantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VibrantTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ludo Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.banner_ludo),
                        contentDescription = "Ludo Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Matchup / Scores Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VibrantSurfaceVariant)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player 1 (You)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "👤 You", color = VibrantCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${state.myScore} pts",
                            color = VibrantGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // VS indicator
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VibrantPrimary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    // Opponent
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🤖 Room Rivals", color = VibrantPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${state.opponentScore} pts",
                            color = VibrantGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mini Ludo Board Matrix Visualizer
                MiniLudoBoardView()

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 3D Dice Display
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .rotate(if (state.isRolling) diceRotation else 0f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White, Color(0xFFE2E8F0))
                            )
                        )
                        .border(3.dp, VibrantGold, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val diceEmoji = when (state.myDice) {
                        1 -> "⚀"
                        2 -> "⚁"
                        3 -> "⚂"
                        4 -> "⚃"
                        5 -> "⚄"
                        else -> "⚅"
                    }
                    Text(
                        text = if (state.isRolling) "🎲" else diceEmoji,
                        fontSize = 44.sp,
                        color = if (state.isRolling) VibrantPrimary else VibrantTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status message
                Text(
                    text = state.statusText,
                    color = VibrantTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Roll Button
                Button(
                    onClick = onRollDice,
                    enabled = !state.isRolling,
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(46.dp)
                ) {
                    Text(
                        text = if (state.isRolling) "Rolling... 🎲" else "Roll Dice! 🎲",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MiniLudoBoardView() {
    Column(
        modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, VibrantBorder, RoundedCornerShape(8.dp))
    ) {
        // Row 1: Red Base (Top Left), Path, Green Base (Top Right)
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔴", fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(VibrantSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⭐", fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF43A047)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🟢", fontSize = 16.sp)
            }
        }

        // Row 2: Center Home
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(VibrantSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⬆️", fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.radialGradient(
                            listOf(VibrantGold, Color(0xFFFF9800))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏆", fontSize = 18.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(VibrantSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⬇️", fontSize = 11.sp)
            }
        }

        // Row 3: Blue Base (Bottom Left), Path, Yellow Base (Bottom Right)
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E88E5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔵", fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(VibrantSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⭐", fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFFDD835)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🟡", fontSize = 16.sp)
            }
        }
    }
}
