package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Defined Color Tokens (Navy & White Rebrand)
// ═══════════════════════════════════════════════════════════════════════════════

// ── Core Brand Palette ──────────────────────────────────────────────────────
val ScannerBlue = Color(0xFF0066FF)         // Primary Accent — buttons, active states
val DeepNavy = Color(0xFF0A142F)            // Deep Navy — dark mode BG, light mode text
val ElevatedNavy = Color(0xFF11224D)        // Slightly Elevated Navy — dark mode surfaces

// ── Light Theme: Pure White & Navy ──────────────────────────────────────────
val PureWhiteBackground = Color(0xFFFFFFFF) // Pure White background
val NavyPrimary = Color(0xFF0066FF)         // Scanner Blue primary
val DeepSlateText = Color(0xFF0A142F)       // Deep Navy text
val WhiteCard = Color(0xFFFFFFFF)           // Pure white card surface

// ── Dark Theme: Deep Navy & White ───────────────────────────────────────────
val NavyCanvas = Color(0xFF0A142F)          // Deep Navy background
val ElevatedCardDark = Color(0xFF11224D)    // Elevated Navy card surface
val SoftWhiteText = Color(0xFFFFFFFF)       // Pure White text

// ── Accent: Risk & Status ───────────────────────────────────────────────────
val CrimsonRisk = Color(0xFFFF4B4B)         // Soft Coral/Red #FF4B4B
val EmeraldSafe = Color(0xFF2E7D32)         // Green #2E7D32
val AmberWarning = Color(0xFFF9A825)        // Amber #F9A825

// ── Neutral: Borders, Subtexts ──────────────────────────────────────────────
val SlateBorderLight = Color(0xFFE2E8F0)    // Cool-gray border
val SlateBorderDark = Color(0xFF1E3A5F)     // Navy border
val SubtextLight = Color(0xFF4A5568)        // Muted navy-gray
val SubtextDark = Color(0xFF94A3B8)         // Muted slate-blue

// ── M3 Container Colors for Banners ─────────────────────────────────────────
val CrimsonContainerLight = Color(0xFFFF4B4B).copy(alpha = 0.08f)
val CrimsonContainerDark = Color(0xFFFF4B4B).copy(alpha = 0.12f)
val CrimsonOnContainerLight = Color(0xFFFF4B4B)
val CrimsonOnContainerDark = Color(0xFFFF7878)

val EmeraldContainerLight = Color(0xFFE8F5E9)
val EmeraldContainerDark = Color(0xFF1B3B1D)
val EmeraldOnContainerLight = Color(0xFF2E7D32)
val EmeraldOnContainerDark = Color(0xFF81C784)

// ── Primary Container ───────────────────────────────────────────────────────
val NavyContainerLight = Color(0xFFDCE6FF)  // Light blue container
val NavyContainerDark = Color(0xFF162B50)   // Deep navy container
val NavyOnContainerLight = Color(0xFF0A142F)
val NavyOnContainerDark = Color(0xFFFFFFFF)
