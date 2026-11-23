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
    const val OCEAN = "ocean"
    const val NEBULA = "nebula"
    const val AURORA = "aurora"
    const val SUNSET = "sunset"
    const val DAYLIGHT = "daylight"
    const val SAND = "sand"

    val choices = listOf(
        NEBULA to "Nebula",
        MIDNIGHT to "Midnight",
        AURORA to "Aurora",
        CARBON to "Carbon",
        OCEAN to "Ocean",
        SUNSET to "Sunset",
        DAYLIGHT to "Daylight",
        SAND to "Sand"
    )
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

private val Nebula = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1A0B3D),
    primaryContainer = Color(0xFF2B1B57),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF3D0A25),
    secondaryContainer = Color(0xFF4A1533),
    onSecondaryContainer = Color(0xFFFFD9EA),
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF04262B),
    background = Color(0xFF0A0714),
    onBackground = Color(0xFFECE8F8),
    surface = Color(0xFF120C22),
    onSurface = Color(0xFFECE8F8),
    surfaceVariant = Color(0xFF1C1430),
    onSurfaceVariant = Color(0xFFB0A6C8),
    outline = Color(0xFF3B2F55),
    error = Color(0xFFFF6B8B),
    onError = Color(0xFF2A0A14)
)

private val Aurora = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF032420),
    primaryContainer = Color(0xFF0E3B35),
    onPrimaryContainer = Color(0xFFCFFBF3),
    secondary = Color(0xFFA3E635),
    onSecondary = Color(0xFF1A2A00),
    secondaryContainer = Color(0xFF2C3D0A),
    onSecondaryContainer = Color(0xFFEAFFC2),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF04283A),
    background = Color(0xFF04100E),
    onBackground = Color(0xFFE3F5F0),
    surface = Color(0xFF081A17),
    onSurface = Color(0xFFE3F5F0),
    surfaceVariant = Color(0xFF0F2621),
    onSurfaceVariant = Color(0xFF9DB8B1),
    outline = Color(0xFF1F3B35),
    error = Color(0xFFFF7B72),
    onError = Color(0xFF2A0A0A)
)

private val Sunset = darkColorScheme(
    primary = Color(0xFFFB7185),
    onPrimary = Color(0xFF3D0611),
    primaryContainer = Color(0xFF4A1420),
    onPrimaryContainer = Color(0xFFFFDDE3),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF2B1D00),
    secondaryContainer = Color(0xFF3A2C08),
    onSecondaryContainer = Color(0xFFFDEDC2),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF1A0B3D),
    background = Color(0xFF140A0A),
    onBackground = Color(0xFFF7E9E7),
    surface = Color(0xFF1E1010),
    onSurface = Color(0xFFF7E9E7),
    surfaceVariant = Color(0xFF2A1717),
    onSurfaceVariant = Color(0xFFC9AFA9),
    outline = Color(0xFF4A2E2E),
    error = Color(0xFFFF8080),
    onError = Color(0xFF2A0A0A)
)

private val Ocean = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF04283A),
    primaryContainer = Color(0xFF0B3B52),
    onPrimaryContainer = Color(0xFFD6F2FF),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF0B1240),
    secondaryContainer = Color(0xFF1B2354),
    onSecondaryContainer = Color(0xFFE0E3FF),
    background = Color(0xFF071019),
    onBackground = Color(0xFFE2EDF5),
    surface = Color(0xFF0D1822),
    onSurface = Color(0xFFE2EDF5),
    surfaceVariant = Color(0xFF16242F),
    onSurfaceVariant = Color(0xFFA5B8C6),
    outline = Color(0xFF354A58),
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

private val Sand = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF3A1A00),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF00352F),
    background = Color(0xFFFAF6EF),
    onBackground = Color(0xFF201A12),
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF201A12),
    surfaceVariant = Color(0xFFF0E7D8),
    onSurfaceVariant = Color(0xFF5E5344),
    outline = Color(0xFFC7B99F),
    error = Color(0xFFB91C1C),
    onError = Color.White
)

@Composable
fun AntiAgentTheme(choice: String, content: @Composable () -> Unit) {
    val scheme = when (choice) {
        Themes.MIDNIGHT -> Midnight
        Themes.CARBON -> Carbon
        Themes.OCEAN -> Ocean
        Themes.NEBULA -> Nebula
        Themes.AURORA -> Aurora
        Themes.SUNSET -> Sunset
        Themes.DAYLIGHT -> Daylight
        Themes.SAND -> Sand
        else -> Nebula
    }
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}
