package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MuseViolet,
    onPrimary = Color.White,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = MuseVioletLight,
    secondary = MuseIndigo,
    onSecondary = Color.White,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = MuseCoral,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderMedium
)

private val LightColorScheme = lightColorScheme(
    primary = MuseVioletDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = MuseVioletDark,
    secondary = MuseIndigo,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun MUSETheme(
    darkTheme: Boolean = true, // MUSE is designed with dark mode as the primary experience
    dynamicColor: Boolean = false, // Preserve MUSE's custom curated color system
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
