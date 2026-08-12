package com.example.pelarikalcer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PelariDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DeepNavy,
    primaryContainer = NeonGreenDim,
    onPrimaryContainer = DeepNavy,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    secondaryContainer = AccentOrangeDim,
    onSecondaryContainer = TextPrimary,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = TextPrimary,
    outline = TextMuted,
)

@Composable
fun PelariKalcerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PelariDarkColorScheme,
        typography = Typography,
        content = content
    )
}