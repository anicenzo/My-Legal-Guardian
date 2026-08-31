package com.example.ui.primitives

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Custom Typography Scale Tokens (Strict Sans-Serif)
// ═══════════════════════════════════════════════════════════════════════════════
// Enforce modern, clean geometric Sans-Serif system-wide for every single style.
// All styles enforce deliberate ~1.3–1.4x line-height ratios.

object LGType {

    // ── Display: Hero titles ("Ready to Scan", "Unlock Pro") ──────────────
    val Display = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    )

    // ── Headline: Section headers, major banners ──────────────────────────
    val Headline = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    )

    // ── Title: Card titles, primary section headers ───────────────────────
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
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    // ── Body: Descriptions, clause explanations ───────────────────────────
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.15.sp
    )

    // ── BodySmall: Secondary descriptions, metadata lines ────────────────
    val BodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp
    )

    // ── Label: Badges, button labels, uppercase indicators ────────────────
    val Label = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    )

    // ── Caption: Small helper notes, timestamp text ───────────────────────
    val Caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )

    // ── Mono: Raw contract text snippets ──────────────────────────────────
    val Mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp
    )
}
