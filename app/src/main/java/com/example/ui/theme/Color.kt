package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MUSE Signature Backgrounds & Surfaces (Deep near-black with subtle warm/violet undertones)
val BackgroundDark = Color(0xFF08080C)
val BackgroundDeep = Color(0xFF050508)
val SurfaceDark = Color(0xFF101118)
val SurfaceElevated = Color(0xFF161822)
val SurfaceCard = Color(0xFF1C1E2A)
val SurfaceGlass = Color(0xE6151620)
val SurfaceGlassStroke = Color(0x1FFFFFFF)
val SurfaceHighlight = Color(0x0FFFFFFF)

// Accent & Brand Colors
val MuseViolet = Color(0xFF8B5CF6)
val MuseVioletLight = Color(0xFFA78BFA)
val MuseVioletDark = Color(0xFF6D28D9)
val MuseIndigo = Color(0xFF6366F1)
val MuseCoral = Color(0xFFFF5376)
val MuseCyan = Color(0xFF06B6D4)
val MuseEmerald = Color(0xFF10B981)
val MuseAmber = Color(0xFFF59E0B)

// Text Tokens
val TextPrimary = Color(0xFFF3F4F8)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextDim = Color(0xFF475569)

// Borders & Dividers
val BorderSubtle = Color(0x14FFFFFF)
val BorderGlass = Color(0x24FFFFFF)
val BorderMedium = Color(0x29FFFFFF)
val BorderAccent = Color(0x4D8B5CF6)

// Gradients
val HeroGradient1 = Brush.linearGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
)

val HeroGradient2 = Brush.linearGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF6366F1))
)

val HeroGradient3 = Brush.linearGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFF9333EA), Color(0xFF4F46E5))
)

val CardOverlayGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0xCC08080C), Color(0xF508080C))
)
