package com.example.ui.primitives

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Modern Technical Editorial UI Primitives
// ═══════════════════════════════════════════════════════════════════════════════

// ── Spacing scale ─────────────────────────────────────────────────────────────
object LGSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

// ── Button variants ────────────────────────────────────────────────────────────
enum class LGButtonVariant {
    PrimaryHero, // Crisp Titanium White with deep obsidian text
    Primary,     // Electric Indigo
    Secondary,   // Dark bordered card surface
    Ghost        // Transparent with border
}

// ── Tap feedback without ripple glow ──────────────────────────────────────────
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

// ── Bouncy tactile press modifier ─────────────────────────────────────────────
fun Modifier.lgPressClickable(
    enabled: Boolean = true,
    onClick: (() -> Unit)?
): Modifier = composed {
    if (onClick == null) return@composed this
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        label = "pressAlpha"
    )
    this
        .scale(scale)
        .alpha(alpha)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG SURFACE — Stepped luminance card with hairline border
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGSurface(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    subdued: Boolean = false,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalLGColors.current
    val shape = RoundedCornerShape(14.dp)
    val bg = when {
        subdued  -> colors.SurfaceSubdued
        elevated -> colors.SurfaceElevated
        else     -> colors.Surface
    }
    val border = borderColor ?: colors.Border

    Column(
        modifier = modifier
            .background(bg, shape)
            .border(BorderStroke(1.dp, border), shape)
            .then(if (onClick != null) Modifier.lgPressClickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BUTTON — Tactile high-contrast button with spring physics
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: LGButtonVariant = LGButtonVariant.PrimaryHero,
    enabled: Boolean = true
) {
    val colors = LocalLGColors.current
    val shape = RoundedCornerShape(12.dp)

    val (bg, fg, borderStroke) = when (variant) {
        LGButtonVariant.PrimaryHero -> Triple(colors.PrimaryHero, colors.OnPrimaryHero, null)
        LGButtonVariant.Primary     -> Triple(colors.Accent, Color.White, null)
        LGButtonVariant.Secondary   -> Triple(colors.Surface, colors.TextPrimary, BorderStroke(1.dp, colors.Border))
        LGButtonVariant.Ghost       -> Triple(Color.Transparent, colors.TextPrimary, BorderStroke(1.dp, colors.Border))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btnScale"
    )

    Box(
        modifier = modifier
            .semantics { role = Role.Button }
            .defaultMinSize(minWidth = 64.dp, minHeight = 50.dp)
            .scale(scale)
            .background(if (enabled) bg else colors.SurfaceSubdued, shape)
            .then(
                if (borderStroke != null)
                    Modifier.border(borderStroke, shape)
                else Modifier
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) fg else colors.TextTertiary,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) fg else colors.TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BADGE — Pill badge with optional status dot indicator
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = false
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(100.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.25f)), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = LGType.Caption.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// RISK SCORING & GAUGE
// ═══════════════════════════════════════════════════════════════════════════════

fun severityColor(severity: Int): Color = when (severity) {
    3    -> LGColorsDark.RiskHigh
    2    -> LGColorsDark.RiskMedium
    else -> LGColorsDark.RiskLow
}

fun severityLabel(severity: Int): String = when (severity) {
    3    -> "CRITICAL RISK"
    2    -> "MODERATE RISK"
    else -> "SAFE CLAUSE"
}

fun riskBandColor(score: Int): Color = when {
    score > 60 -> LGColorsDark.RiskHigh
    score > 30 -> LGColorsDark.RiskMedium
    else       -> LGColorsDark.RiskLow
}

fun riskGrade(score: Int): String = when {
    score > 75 -> "GRADE F · SEVERE RISK"
    score > 60 -> "GRADE D · HIGH RISK"
    score > 30 -> "GRADE C · MODERATE RISK"
    score > 15 -> "GRADE B · LOW RISK"
    else       -> "GRADE A · FAVORABLE"
}

@Composable
fun LGRiskGauge(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "RiskScoreAnimation"
    )
    val gaugeColor = riskBandColor(score)
    val trackColor = LocalLGColors.current.Border

    Box(
        modifier = modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 11.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val effectiveSweep = (animatedScore / 100f)
                .coerceIn(if (score == 0) 0f else 0.04f, 1f) * 360f
            drawArc(
                color = gaugeColor,
                startAngle = -90f,
                sweepAngle = effectiveSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}",
                style = LGType.DisplayLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                color = gaugeColor
            )
            Text(
                text = "RISK SCORE",
                style = LGType.Caption.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                color = LocalLGColors.current.TextTertiary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BOTTOM NAV — Sleek docked bar with clean indicator
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLGColors.current
    val items = listOf(
        Triple("vault",    "Vault",    Icons.Filled.FolderOpen),
        Triple("scan",     "Scanner",  Icons.Filled.DocumentScanner),
        Triple("settings", "Settings", Icons.Filled.Settings)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.Surface)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, icon) ->
                val isSelected = selectedRoute == route
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { role = Role.Tab }
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(route) }
                        .background(
                            if (isSelected) colors.AccentMuted else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) colors.Accent else colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = LGType.Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG TOP BAR — Clean flat header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = LocalLGColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = LGType.Title.copy(color = colors.TextPrimary),
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                trailingIcon()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.Border)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG TEXT FIELD — Modern input with focus transition
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val colors = LocalLGColors.current
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    val borderStroke = if (isFocused) {
        BorderStroke(1.5.dp, colors.Accent)
    } else {
        BorderStroke(1.dp, colors.Border)
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = label }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .onFocusChanged { isFocused = it.isFocused }
            .background(colors.Surface, shape)
            .border(borderStroke, shape)
            .padding(16.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = label,
                style = LGType.Body.copy(color = colors.TextTertiary)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LGType.Body.copy(color = colors.TextPrimary),
            cursorBrush = SolidColor(colors.Accent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}