package com.example.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Custom Color Token System
// ═══════════════════════════════════════════════════════════════════════════════
// Provides direct access to design tokens outside of MaterialTheme.colorScheme.
// Used by custom primitives (LGSurface, LGButton, LGRiskGauge, etc.) that
// need raw palette values without M3 semantic mapping.

interface LGColorSystem {
    val Background: Color
    val Surface: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val AccentSafe: Color       // Green #2E7D32 — low risk, success states
    val AccentDanger: Color     // Soft Coral/Red #FF4B4B — high risk, alert states
    val AccentWarning: Color    // Amber #F9A825 — moderate risk, caution states
    val TrustBlue: Color        // Deep Navy #0A192F — primary action, trust signals
    val Border: Color
    val HeaderBackground: Color // Dark #121212 in dark mode / Navy #0A192F in light mode
    val HeaderContent: Color
    val BannerSafeBackground: Color
    val BannerSafeContent: Color
    val BannerWarningBackground: Color
    val BannerWarningContent: Color
    val BannerDangerBackground: Color
    val BannerDangerContent: Color
    val ExpandedSurface: Color
}

// ── Light Mode Tokens ─────────────────────────────────────────────────────────
object LGColorsLight : LGColorSystem {
    override val Background = Color(0xFFF9F9F7)     // Cream
    override val Surface = Color(0xFFFFFFFF)        // Pure white card surface
    override val TextPrimary = Color(0xFF0A192F)    // Deep Navy
    override val TextSecondary = Color(0xFF5A6677)  // Muted slate for secondary text
    override val AccentSafe = Color(0xFF2E7D32)     // Green
    override val AccentDanger = Color(0xFFFF4B4B)   // Modern Soft Coral/Red #FF4B4B
    override val AccentWarning = Color(0xFFF9A825)  // Amber
    override val TrustBlue = Color(0xFF0A192F)      // Deep Navy
    override val Border = Color(0xFFE8EAE6)         // Subtle soft border
    override val HeaderBackground = Color(0xFF0A192F) // Deep Navy
    override val HeaderContent = Color(0xFFFFFFFF)
    override val BannerSafeBackground = Color(0xFFE8F5E9)
    override val BannerSafeContent = Color(0xFF2E7D32)
    override val BannerWarningBackground = Color(0xFFFFF8E1)
    override val BannerWarningContent = Color(0xFFB26A00)
    override val BannerDangerBackground = Color(0xFFFF4B4B).copy(alpha = 0.08f) // Soft translucent coral
    override val BannerDangerContent = Color(0xFFFF4B4B)
    override val ExpandedSurface = Color(0xFFF4F4F0)
}

// ── Dark Mode Tokens ──────────────────────────────────────────────────────────
object LGColorsDark : LGColorSystem {
    override val Background = Color(0xFF121212)     // #121212 dark background
    override val Surface = Color(0xFF1E1E1E)        // Lifted card surface
    override val TextPrimary = Color(0xFFE6EDF3)    // Soft white for readability
    override val TextSecondary = Color(0xFF9E9E9E)  // Muted text for dark mode
    override val AccentSafe = Color(0xFF2E7D32)     // Green
    override val AccentDanger = Color(0xFFFF4B4B)   // Modern Soft Coral/Red #FF4B4B
    override val AccentWarning = Color(0xFFF9A825)  // Amber
    override val TrustBlue = Color(0xFF0A192F)      // Deep Navy
    override val Border = Color(0xFF2C2C2C)         // Subtle dark border
    override val HeaderBackground = Color(0xFF121212) // #121212 dark header
    override val HeaderContent = Color(0xFFE6EDF3)
    override val BannerSafeBackground = Color(0xFF1B3B1D) // Dark green container
    override val BannerSafeContent = Color(0xFF81C784)    // Readable light green
    override val BannerWarningBackground = Color(0xFF3E2E10)
    override val BannerWarningContent = Color(0xFFFFD54F)
    override val BannerDangerBackground = Color(0xFFFF4B4B).copy(alpha = 0.15f) // Soft translucent coral in dark
    override val BannerDangerContent = Color(0xFFFF7878)
    override val ExpandedSurface = Color(0xFF252525)
}

val LocalLGColors = staticCompositionLocalOf<LGColorSystem> { LGColorsLight }
