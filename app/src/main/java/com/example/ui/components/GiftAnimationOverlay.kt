package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VoiceChatRepository
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalGold
import kotlinx.coroutines.delay

@Composable
fun GiftAnimationOverlay(
    event: VoiceChatRepository.GiftEvent?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    LaunchedEffect(event) {
        delay(3200)
        onDismiss()
    }

    val scaleTransition = rememberInfiniteTransition(label = "gift_pulse")
    val giftScale by scaleTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "giftScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Sparkles Header
            Text(
                text = "✨ SPECIAL GIFT SEND ✨",
                color = RoyalGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Big Animated Gift Icon with Glowing Halo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(event.gift.colorHex).copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.gift.icon,
                    fontSize = 72.sp,
                    modifier = Modifier.scale(giftScale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gift Details Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1035).copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${event.gift.name} x${event.multiplier}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = event.sender,
                            color = NeonPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = " 🎁 sent to ",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = event.receiver,
                            color = RoyalGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💎 Value: ${event.gift.costCoins * event.multiplier} Coins",
                        color = Color(0xFF00F5D4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
