package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.RoyalGold

@Composable
fun AvatarWithBadge(
    user: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isSpeaking: Boolean = false,
    showLevel: Boolean = true,
    isHost: Boolean = false
) {
    Box(
        modifier = modifier.size(size + 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing speaking indicator ring
        if (isSpeaking) {
            PulsingSpeakingRing(
                isSpeaking = true,
                size = size + 8.dp,
                ringColor = if (isHost) RoyalGold else NeonCyan
            )
        }

        // Host VIP Glowing Border
        val borderBrush = when {
            isHost -> Brush.sweepGradient(listOf(RoyalGold, NeonPink, RoyalGold))
            isSpeaking -> Brush.sweepGradient(listOf(NeonCyan, NeonPink, NeonCyan))
            else -> Brush.sweepGradient(listOf(Color(user.avatarColorHex), Color(0xFF6B46C1)))
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, borderBrush, CircleShape)
                .background(Color(user.avatarColorHex).copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.avatarEmoji,
                fontSize = (size.value * 0.45).sp
            )
        }

        // Crown for host at top
        if (isHost) {
            Text(
                text = "👑",
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-6).dp)
            )
        }

        // Level badge at bottom
        if (showLevel) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isHost) listOf(RoyalGold, Color(0xFFFF9800))
                            else listOf(NeonPink, NeonPurple)
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Lv.${user.level}",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val NeonPurple = Color(0xFF8A2BE2)
