package com.example.ui.primitives

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Dark-Only Color Token System
// ═══════════════════════════════════════════════════════════════════════════════
// Flat object, no interface, no light variant. App is dark-only by design.
// Elevation is expressed through surface lightness steps, not shadow/glow.
// One accent color (desaturated cobalt), used sparingly.
// Risk semantics use desaturated, non-neon hues — reads as credible, not alarming.

object LGColorsDark {
    // ── Surfaces — lightness steps replace shadow-based elevation ────────────
    val Background      = Color(0xFF0A0B0D)   // Near-black canvas
    val Surface         = Color(0xFF121317)   // Base card / panel surface
    val SurfaceElevated = Color(0xFF1A1C21)   // Elevated surface (modals, expanded)
    val Border          = Color(0xFF262932)   // 1dp hairline — universal border token

    // ── Text ─────────────────────────────────────────────────────────────────
    val TextPrimary   = Color(0xFFEDEDEF)     // Near-white
    val TextSecondary = Color(0xFF9498A3)     // Subdued
    val TextTertiary  = Color(0xFF5C6070)     // De-emphasised (metadata, timestamps)

    // ── Accent — desaturated cobalt, ONE accent across the entire app ────────
    val Accent      = Color(0xFF5B7CFA)       // Primary interactive / active
    val AccentMuted = Color(0xFF2A3352)       // Accent bg tint (e.g. active nav tab)

    // ── Risk semantics — desaturated, not neon ───────────────────────────────
    val RiskHigh   = Color(0xFFD9695A)        // Terracotta red — high risk
    val RiskMedium = Color(0xFFD1A24D)        // Muted amber — moderate risk
    val RiskLow    = Color(0xFF6FA383)        // Sage green — low/safe
}

// Singleton local — dark only, no branching
val LocalLGColors = staticCompositionLocalOf { LGColorsDark }
