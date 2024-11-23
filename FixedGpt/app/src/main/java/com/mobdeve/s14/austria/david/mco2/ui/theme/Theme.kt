package com.mobdeve.s14.austria.david.mco2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = GreenIndicator,
    onSecondary = White,
    background = LightGray,
    surface = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun DashboardUITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

val CurrencyGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEFFC8E),
        Color(0xFF68B09E)
    )
)