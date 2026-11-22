package com.antiagent.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Themes {
    const val MIDNIGHT = "midnight"
    const val CARBON = "carbon"
    const val DAYLIGHT = "daylight"

    val choices = listOf(MIDNIGHT to "Midnight", CARBON to "Carbon", DAYLIGHT to "Daylight")
}

private val Midnight = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E2350),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF04262B),
    secondaryContainer = Color(0xFF123B42),
    onSecondaryContainer = Color(0xFFC9F6FC),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE7EBF3),
    surface = Color(0xFF11161D),
    onSurface = Color(0xFFE7EBF3),
    surfaceVariant = Color(0xFF1B222C),
    onSurfaceVariant = Color(0xFFA9B4C4),
    outline = Color(0xFF3A4453),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0A0A)
)

private val Carbon = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF05281B),
    primaryContainer = Color(0xFF0E3B2B),
    onPrimaryContainer = Color(0xFFD3FBE9),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF2B1D00),
    secondaryContainer = Color(0xFF3A2C08),
    onSecondaryContainer = Color(0xFFFDEDC2),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFE9E9EC),
    surface = Color(0xFF161619),
    onSurface = Color(0xFFE9E9EC),
    surfaceVariant = Color(0xFF222226),
    onSurfaceVariant = Color(0xFFB4B4BC),
    outline = Color(0xFF43434B),
    error = Color(0xFFFF7B72),
    onError = Color(0xFF2A0A0A)
)

private val Daylight = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF3FA),
    onSecondaryContainer = Color(0xFF00363D),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF17181C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17181C),
    surfaceVariant = Color(0xFFECEAF2),
    onSurfaceVariant = Color(0xFF55525F),
    outline = Color(0xFFB9B5C4),
    error = Color(0xFFC0281F),
    onError = Color.White
)

@Composable
fun AntiAgentTheme(choice: String, content: @Composable () -> Unit) {
    val scheme = when (choice) {
        Themes.CARBON -> Carbon
        Themes.DAYLIGHT -> Daylight
        else -> Midnight
    }
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}
