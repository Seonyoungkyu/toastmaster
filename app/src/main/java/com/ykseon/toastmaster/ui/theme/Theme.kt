package com.ykseon.toastmaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFF52499C), // 0xFF6200EE
    onPrimary = Color.White,
    surface = Color.DarkGray,
    onSurface = Color.LightGray,
    background = Color.Black,
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF52499C),
    onPrimary = Color.White,
    surface = Color.LightGray,
    onSurface = Color.Black,
    background = Color.White
)

@Composable
fun TimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}