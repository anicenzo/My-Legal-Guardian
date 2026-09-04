package com.example.ui.primitives

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Flat/Editorial UI Primitives
// ═══════════════════════════════════════════════════════════════════════════════
// Design language: flat surfaces, 1dp hairline borders, no shadow/glow.
// Elevation is communicated by surface lightness steps (Surface < SurfaceElevated).
// One accent color (Accent) used sparingly. Corner radius 10–14dp (not 24dp).

// ── Spacing scale (kept for backward-compat) ──────────────────────────────────
object LGSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

// ── Button variant ─────────────────────────────────────────────────────────────
enum class LGButtonVariant { Primary, Secondary }

// ── clickableNoRipple — tap feedback without glow overlay ─────────────────────
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

// ── Press-scale modifier ───────────────────────────────────────────────────────
fun Modifier.lgPressClickable(
    enabled: Boolean = true,
    onClick: (() -> Unit)?
): Modifier = composed {
    if (onClick == null) return@composed this
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1.0f, label = "scale")
    val alpha by animateFloatAsState(if (isPressed) 0.85f else 1.0f, label = "alpha")
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
// LG SURFACE — Flat card with hairline border, no shadow
// ═══════════════════════════════════════════════════════════════════════════════
// Use `elevated = true` for modals, expanded states — switches to SurfaceElevated bg.

@Composable
fun LGSurface(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalLGColors.current
    val shape = RoundedCornerShape(12.dp)
    val bg = if (elevated) colors.SurfaceElevated else colors.Surface

    Column(
        modifier = modifier
            .background(bg, shape)
            .border(BorderStroke(1.dp, colors.Border), shape)
            .then(if (onClick != null) Modifier.lgPressClickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BUTTON — Flat fill, no glow, 10dp radius
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LGButtonVariant = LGButtonVariant.Primary,
    enabled: Boolean = true
) {
    val colors = LocalLGColors.current
    val shape = RoundedCornerShape(10.dp)

    val (bg, fg, borderColor) = when (variant) {
        LGButtonVariant.Primary   -> Triple(colors.Accent, Color.White, null)
        LGButtonVariant.Secondary -> Triple(colors.Surface, colors.TextPrimary, colors.Border)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "btnScale")

    Box(
        modifier = modifier
            .semantics { role = Role.Button }
            .defaultMinSize(minWidth = 64.dp, minHeight = 52.dp)
            .scale(scale)
            .background(if (enabled) bg else colors.Surface, shape)
            .then(
                if (borderColor != null)
                    Modifier.border(BorderStroke(1.dp, borderColor), shape)
                else Modifier
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LGType.Heading,
            color = if (enabled) fg else colors.TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BADGE — Translucent tinted background, flat, no drop shadow
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LGType.Caption.copy(fontWeight = FontWeight.Medium),
            color = color
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// RISK GAUGE — Animated circular arc indicator
// ═══════════════════════════════════════════════════════════════════════════════

fun severityColor(severity: Int): Color = when (severity) {
    3    -> LGColorsDark.RiskHigh
    2    -> LGColorsDark.RiskMedium
    else -> LGColorsDark.RiskLow
}

fun severityLabel(severity: Int): String = when (severity) {
    3    -> "HIGH"
    2    -> "MEDIUM"
    else -> "LOW"
}

fun riskBandColor(score: Int): Color = when {
    score > 60 -> LGColorsDark.RiskHigh
    score > 30 -> LGColorsDark.RiskMedium
    else       -> LGColorsDark.RiskLow
}

@Composable
fun LGRiskGauge(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "RiskScoreAnimation"
    )
    val gaugeColor = riskBandColor(score)
    val riskLabel = when {
        score > 60 -> "HIGH RISK"
        score > 30 -> "MODERATE RISK"
        else       -> "LOW RISK"
    }
    val trackColor = LGColorsDark.Border

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val strokeWidth = 10.dp.toPx()
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
                style = LGType.Display.copy(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
                color = gaugeColor
            )
            Text(
                text = riskLabel,
                style = LGType.Caption,
                color = LGColorsDark.TextSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG BOTTOM NAV — Hairline top border, flat active indicator
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLGColors.current
    val items = listOf(
        Triple("vault",    "Vault",    Icons.Filled.Folder),
        Triple("scan",     "Scan",     Icons.Filled.DocumentScanner),
        Triple("settings", "Settings", Icons.Filled.Settings)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.Surface)
            .navigationBarsPadding()
    ) {
        // Hairline divider — replaces shadow elevation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, icon) ->
                val isSelected = selectedRoute == route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
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
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) colors.Accent else colors.TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        style = LGType.Caption.copy(fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal),
                        color = if (isSelected) colors.Accent else colors.TextTertiary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG TOP BAR — Flat header, hairline bottom border, no shadow elevation
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
        // Hairline bottom border instead of shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.Border)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LG TEXT FIELD — Flat input with focus border animation
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
    val shape = RoundedCornerShape(10.dp)

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