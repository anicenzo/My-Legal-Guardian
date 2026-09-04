package com.example.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

// ── AnimatedPrimaryButton — delegates to LGButton with press scale ─────────────
@Composable
fun AnimatedPrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LGButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp)
    )
}

// ── Section Header ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
        color = color
    )
}

// ── Cost Row ───────────────────────────────────────────────────────────────────
@Composable
fun CostRow(label: String, value: String) {
    val colors = LocalLGColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = LGType.Body.copy(fontWeight = FontWeight.Normal), color = colors.TextSecondary)
        Text(value, style = LGType.Body.copy(fontWeight = FontWeight.Medium), color = colors.TextPrimary)
    }
}

// ── StudioFooter — kept as composable but only used in Settings ───────────────
@Composable
fun StudioFooter() {
    Text(
        text = "Legal AI — Contract Scanner",
        style = LGType.Caption.copy(color = LocalLGColors.current.TextTertiary),
        modifier = Modifier.fillMaxWidth().padding(vertical = LGSpacing.lg),
        textAlign = TextAlign.Center
    )
}

// ── PrivacyBanner — stub kept for call-site compat; renders nothing ────────────
// Trust is stated once, in Settings → Privacy & Security.
@Composable
fun PrivacyBanner() {
    // Intentionally empty — removed from Scan and Result screens per redesign brief.
    // Trust copy lives in Settings only.
}

fun formatAmount(amount: Double): String = String.format("%,.2f", amount)
