package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VibrantColorScheme = lightColorScheme(
    primary = VibrantPrimary,
    onPrimary = Color.White,
    primaryContainer = VibrantPrimaryContainer,
    onPrimaryContainer = VibrantOnPrimaryContainer,
    secondary = VibrantPrimaryGradientEnd,
    onSecondary = Color.White,
    secondaryContainer = VibrantSecondaryContainer,
    onSecondaryContainer = VibrantOnPrimaryContainer,
    tertiary = VibrantTertiary,
    onTertiary = VibrantOnTertiary,
    background = VibrantBackground,
    onBackground = VibrantTextPrimary,
    surface = VibrantSurface,
    onSurface = VibrantTextPrimary,
    surfaceVariant = VibrantSurfaceVariant,
    onSurfaceVariant = VibrantTextSecondary,
    outline = VibrantBorder,
    outlineVariant = VibrantBorderDivider,
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = VibrantBackground.toArgb()
                window.navigationBarColor = VibrantSurfaceVariant.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = VibrantColorScheme,
        typography = Typography,
        content = content
    )
}

