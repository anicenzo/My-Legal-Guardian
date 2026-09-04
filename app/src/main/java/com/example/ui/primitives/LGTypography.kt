package com.example.ui.primitives

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Typography Scale (Direction A: Single Serif Voice)
// ═══════════════════════════════════════════════════════════════════════════════
// One font family for all text — evokes document, legal, guardian.
// Controlled weights create hierarchy without family mixing.
// Swap `FontFamily.Serif` → `FontFamily.SansSerif` for Direction B (fintech).

object LGType {
    private val family = FontFamily.Serif

    // ── Canonical scale (use these in new code) ───────────────────────────────
    val Display = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    )
    val Title = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
    val Heading = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp
    )
    val Body = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )
    val Caption = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
    val Mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    // ── Backward-compat aliases (prevent compilation errors in existing callers) ─
    // These map old names → closest new canonical style. Delete after full cleanup.
    val Headline  get() = Title    // old: 22sp Bold → Title: 22sp SemiBold Serif
    val Subtitle  get() = Heading  // old: 15sp SemiBold → Heading: 17sp Medium Serif
    val BodySmall get() = Caption  // old: 13sp Normal → Caption: 13sp Normal Serif
    val Button    get() = Heading  // old: 15sp Bold → Heading: 17sp Medium Serif
    val Label     get() = Caption  // old: 11sp SemiBold → Caption: 13sp Normal Serif
}
