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
    val AccentSafe: Color       // Emerald Green — low risk, success states
    val AccentDanger: Color     // Vivid Crimson — high risk, alert states
    val AccentWarning: Color    // Rich Amber — moderate risk, caution states
    val TrustBlue: Color        // Deep Navy — primary action, trust signals
    val Border: Color
}

// ── Light Mode Tokens ─────────────────────────────────────────────────────────
object LGColorsLight : LGColorSystem {
    override val Background = Color(0xFFF8F9FA)     // Off-white — makes cards pop
    override val Surface = Color(0xFFFFFFFF)         // Pure white card surface
    override val TextPrimary = Color(0xFF1C2541)     // Deep Slate — never pure #000
    override val TextSecondary = Color(0xFF5A6677)   // Muted slate for secondary text
    override val AccentSafe = Color(0xFF2A9D8F)      // Emerald Green
    override val AccentDanger = Color(0xFFD90429)    // Vivid Crimson Red
    override val AccentWarning = Color(0xFFE9A319)   // Rich Amber
    override val TrustBlue = Color(0xFF0B132B)       // Deep Navy Blue
    override val Border = Color(0xFFDDE1E6)          // Subtle light border
}

// ── Dark Mode Tokens ──────────────────────────────────────────────────────────
object LGColorsDark : LGColorSystem {
    override val Background = Color(0xFF0D1117)      // GitHub-style deep dark
    override val Surface = Color(0xFF161B22)          // Slightly lifted card surface
    override val TextPrimary = Color(0xFFE6EDF3)     // Soft white for readability
    override val TextSecondary = Color(0xFF8B949E)   // Muted text for dark mode
    override val AccentSafe = Color(0xFF3DBAA8)      // Brighter emerald for dark bg
    override val AccentDanger = Color(0xFFFF4D6A)    // Brighter crimson for dark bg
    override val AccentWarning = Color(0xFFF5C542)   // Brighter amber for dark bg
    override val TrustBlue = Color(0xFF58A6FF)       // Bright blue for dark contrast
    override val Border = Color(0xFF30363D)          // Subtle dark border
}

val LocalLGColors = staticCompositionLocalOf<LGColorSystem> { LGColorsLight }
