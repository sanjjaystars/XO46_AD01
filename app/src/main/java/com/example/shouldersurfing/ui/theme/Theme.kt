package com.example.shouldersurfing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    background = SurfaceLight,
    surface = SurfaceLight,
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    background = DarkNavy,
    surface = DarkNavy,
    error = AlertRed
)

@Composable
fun ShoulderSurfingTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
