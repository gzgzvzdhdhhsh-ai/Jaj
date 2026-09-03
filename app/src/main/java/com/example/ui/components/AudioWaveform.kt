package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun PulsingSpeakingRing(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    ringColor: Color = NeonCyan,
    size: Dp = 64.dp
) {
    if (!isSpeaking) return

    val infiniteTransition = rememberInfiniteTransition(label = "speaking_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .clip(CircleShape)
            .background(ringColor.copy(alpha = alpha))
    )
}

@Composable
fun EqualizerBars(
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    height: Dp = 16.dp,
    color: Color = NeonPink
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val barScale by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (i * 120),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height * barScale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonPink, color)
                        )
                    )
            )
        }
    }
}
