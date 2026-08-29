package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Premium Design System Color Palette
// ═══════════════════════════════════════════════════════════════════════════════

// ── Light Theme: Off-White & Navy ──────────────────────────────────────────────
// Background uses off-white to make cards pop with natural contrast.
val OffWhiteBackground = Color(0xFFF8F9FA)
val NavyPrimary = Color(0xFF0B132B)        // Deep, rich Navy Blue — primary action color
val DeepSlateText = Color(0xFF1C2541)      // High-readability text — never pure #000
val WhiteCard = Color(0xFFFFFFFF)          // Pure white cards for maximum elevation contrast

// ── Dark Theme: Charcoal & Soft White ─────────────────────────────────────────
val CharcoalCanvas = Color(0xFF0D1117)     // GitHub-style deep dark background
val ElevatedCardDark = Color(0xFF161B22)   // Slightly lifted card surface
val SoftWhiteText = Color(0xFFE6EDF3)      // Soft white text for dark mode readability

// ── Accent: Risk & Status ─────────────────────────────────────────────────────
// These colors are used for risk badges, banners, and severity indicators.
val CrimsonRisk = Color(0xFFD90429)        // Vivid Crimson Red — alert/risk
val EmeraldSafe = Color(0xFF2A9D8F)        // Emerald Green — success/safe
val AmberWarning = Color(0xFFE9A319)       // Rich Amber — moderate risk/warning

// ── Neutral: Borders, Subtexts ────────────────────────────────────────────────
val SlateBorderLight = Color(0xFFDDE1E6)   // Subtle light mode border
val SlateBorderDark = Color(0xFF30363D)    // Subtle dark mode border
val SubtextLight = Color(0xFF5A6677)       // Muted secondary text — light mode
val SubtextDark = Color(0xFF8B949E)        // Muted secondary text — dark mode

// ── M3 Container Colors for Banners ───────────────────────────────────────────
// Used inside status banners (risk detected, all-clear, etc.)
val CrimsonContainerLight = Color(0xFFFDE8EB)   // Soft pink tint for error containers
val CrimsonContainerDark = Color(0xFF3D0711)     // Deep crimson tint for dark error containers
val CrimsonOnContainerLight = Color(0xFFB80320)  // Readable text on light error container
val CrimsonOnContainerDark = Color(0xFFFFB4AB)   // Readable text on dark error container

val EmeraldContainerLight = Color(0xFFE0F5F1)    // Soft green tint for success containers
val EmeraldContainerDark = Color(0xFF0A2E28)      // Deep green tint for dark success containers
val EmeraldOnContainerLight = Color(0xFF1A7A6E)   // Readable text on light success container
val EmeraldOnContainerDark = Color(0xFFA0DDD3)    // Readable text on dark success container

// ── Primary Container (for icon backgrounds, etc.) ────────────────────────────
val NavyContainerLight = Color(0xFFE2E5EB)        // Light navy tint
val NavyContainerDark = Color(0xFF1C2541)          // Dark navy tint
val NavyOnContainerLight = Color(0xFF0B132B)       // Text on light navy container
val NavyOnContainerDark = Color(0xFFCCD3E0)        // Text on dark navy container
