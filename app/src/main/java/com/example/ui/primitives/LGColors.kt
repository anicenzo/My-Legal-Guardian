package com.example.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Modern Dark-Only Color Token System
// ═══════════════════════════════════════════════════════════════════════════════
// Deep obsidian surface hierarchy, crisp hairline borders, and high-trust
// semantic risk indicators.

object LGColorsDark {
    // ── Surfaces — stepped luminance for OLED depth ──────────────────────────
    val Background      = Color(0xFF090B0E)   // Deep obsidian midnight
    val Surface         = Color(0xFF131720)   // Layer 1 card surface
    val SurfaceElevated = Color(0xFF1B212D)   // Layer 2 elevated surface (modals, sheets)
    val SurfaceSubdued  = Color(0xFF0E1117)   // Recessed quotes & code blocks
    val Border          = Color(0xFF222B3A)   // Crisp hairline border
    val BorderSubtle    = Color(0xFF181E29)   // Subtle divider

    // ── Text ─────────────────────────────────────────────────────────────────
    val TextPrimary   = Color(0xFFF1F4F8)     // High-contrast crisp white
    val TextSecondary = Color(0xFF94A3B8)     // Slate secondary
    val TextTertiary  = Color(0xFF7B8BA6)     // Slate tertiary (timestamps, metadata) - WCAG AA 5.8:1

    // ── Interactive Accents ──────────────────────────────────────────────────
    val Accent        = Color(0xFF6366F1)     // Electric Indigo
    val AccentMuted   = Color(0xFF232847)     // Indigo tint (for tabs/pills)
    val PrimaryHero   = Color(0xFFF8FAFC)     // Titanium White (hero CTA background)
    val OnPrimaryHero = Color(0xFF090B0E)     // Deep obsidian (hero CTA text)

    // ── Semantic Risk Tokens ─────────────────────────────────────────────────
    val RiskHigh   = Color(0xFFF43F5E)        // Rose Crimson — critical risk
    val RiskMedium = Color(0xFFF59E0B)        // Amber Gold — moderate risk
    val RiskLow    = Color(0xFF10B981)        // Emerald Mint — safe/verified
}

// Singleton local — dark only
val LocalLGColors = staticCompositionLocalOf { LGColorsDark }
