package com.example.ui.primitives

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Modern Technical Editorial Typography Scale
// ═══════════════════════════════════════════════════════════════════════════════
// Modern crisp Sans-Serif for high-density UI chrome, controls, metrics & badges.
// Refined Serif reserved for legal document headers and formal clause text.

object LGType {
    private val sans = FontFamily.SansSerif
    private val serif = FontFamily.Serif
    private val mono = FontFamily.Monospace

    // ── Canonical scale ───────────────────────────────────────────────────────
    val DisplayLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    )

    val Display = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
    )

    val Title = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp
    )

    val Heading = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    )

    val Subheading = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val Body = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )

    val BodyMuted = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    )

    val Caption = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp
    )

    val Mono = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    // Legal / Editorial Serif for document titles & formal quoted clauses
    val DocumentTitle = TextStyle(
        fontFamily = serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    )

    val ClauseQuote = TextStyle(
        fontFamily = serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    )

    // ── Backward-compat aliases ───────────────────────────────────────────────
    val Headline  get() = Title
    val Subtitle  get() = Heading
    val BodySmall get() = Caption
    val Button    get() = Heading
    val Label     get() = Caption
}
