package com.example.ui.primitives

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Custom Typography Tokens
// ═══════════════════════════════════════════════════════════════════════════════
// Used by custom primitives (LGTopBar, LGBadge, etc.) that reference LGType
// directly instead of MaterialTheme.typography.
// CRITICAL: FontFamily.SansSerif is hardcoded — never use FontFamily.Default
// because it can resolve to Serif on some OEM ROMs.

object LGType {

    // ── Headline: Screen titles, hero text ────────────────────────────────
    val Headline = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    )

    // ── Title: Card headers, section titles ───────────────────────────────
    val Title = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // ── Subtitle: Grouping headers, item titles ───────────────────────────
    val Subtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    // ── Body: Descriptions, explanations ──────────────────────────────────
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    )

    // ── Mono: Raw contract text snippets (intentionally Monospace) ────────
    val Mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // ── Label: Badges, captions, metadata ─────────────────────────────────
    val Label = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}
