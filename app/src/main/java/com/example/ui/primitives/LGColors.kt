package com.example.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Custom Color Token System (Navy & White Rebrand)
// ═══════════════════════════════════════════════════════════════════════════════
// Provides direct access to design tokens outside of MaterialTheme.colorScheme.
// Used by custom primitives (LGSurface, LGButton, LGRiskGauge, etc.) that
// need raw palette values without M3 semantic mapping.

interface LGColorSystem {
    val Background: Color
    val Surface: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val PrimaryAccent: Color     // Vibrant Scanner Blue #0066FF — buttons, active states, risk gauges
    val AccentSafe: Color        // Green #2E7D32 — low risk, success states
    val AccentDanger: Color      // Soft Coral/Red #FF4B4B — high risk, alert states
    val AccentWarning: Color     // Amber #F9A825 — moderate risk, caution states
    val TrustBlue: Color         // Deep Navy #0A142F — trust signals
    val Border: Color
    val HeaderBackground: Color
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
    override val Background = Color(0xFFFFFFFF)        // Pure White
    override val Surface = Color(0xFFFFFFFF)            // Pure White card surface
    override val TextPrimary = Color(0xFF0A142F)        // Deep Navy
    override val TextSecondary = Color(0xFF4A5568)      // Muted navy-gray for secondary text
    override val PrimaryAccent = Color(0xFF0066FF)      // Vibrant Scanner Blue
    override val AccentSafe = Color(0xFF2E7D32)         // Green
    override val AccentDanger = Color(0xFFFF4B4B)       // Soft Coral/Red
    override val AccentWarning = Color(0xFFF9A825)      // Amber
    override val TrustBlue = Color(0xFF0A142F)          // Deep Navy
    override val Border = Color(0xFFE2E8F0)             // Subtle cool-gray border
    override val HeaderBackground = Color(0xFF0A142F)   // Deep Navy header
    override val HeaderContent = Color(0xFFFFFFFF)
    override val BannerSafeBackground = Color(0xFFE8F5E9)
    override val BannerSafeContent = Color(0xFF2E7D32)
    override val BannerWarningBackground = Color(0xFFFFF8E1)
    override val BannerWarningContent = Color(0xFFB26A00)
    override val BannerDangerBackground = Color(0xFFFF4B4B).copy(alpha = 0.08f)
    override val BannerDangerContent = Color(0xFFFF4B4B)
    override val ExpandedSurface = Color(0xFFF7FAFC)    // Very light cool-gray
}

// ── Dark Mode Tokens ──────────────────────────────────────────────────────────
object LGColorsDark : LGColorSystem {
    override val Background = Color(0xFF0A142F)         // Deep Navy background
    override val Surface = Color(0xFF11224D)             // Slightly Elevated Navy card surface
    override val TextPrimary = Color(0xFFFFFFFF)         // Pure White
    override val TextSecondary = Color(0xFF94A3B8)       // Muted slate-blue for dark mode
    override val PrimaryAccent = Color(0xFF0066FF)       // Vibrant Scanner Blue
    override val AccentSafe = Color(0xFF2E7D32)          // Green
    override val AccentDanger = Color(0xFFFF4B4B)        // Soft Coral/Red
    override val AccentWarning = Color(0xFFF9A825)       // Amber
    override val TrustBlue = Color(0xFF0A142F)           // Deep Navy
    override val Border = Color(0xFF1E3A5F)              // Subtle navy border
    override val HeaderBackground = Color(0xFF0A142F)    // Deep Navy header
    override val HeaderContent = Color(0xFFFFFFFF)
    override val BannerSafeBackground = Color(0xFF1B3B1D)
    override val BannerSafeContent = Color(0xFF81C784)
    override val BannerWarningBackground = Color(0xFF3E2E10)
    override val BannerWarningContent = Color(0xFFFFD54F)
    override val BannerDangerBackground = Color(0xFFFF4B4B).copy(alpha = 0.12f)
    override val BannerDangerContent = Color(0xFFFF7878)
    override val ExpandedSurface = Color(0xFF162B50)     // Deeper navy for expanded sections
}

val LocalLGColors = staticCompositionLocalOf<LGColorSystem> { LGColorsLight }
