package com.example.ui.primitives

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Premium UI Primitives
// ═══════════════════════════════════════════════════════════════════════════════


// ── Bottom Navigation ─────────────────────────────────────────────────────────
@Composable
fun LGBottomNav(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("vault", "Vault", Icons.Filled.Folder),
        Triple("scan", "Scan", Icons.Filled.DocumentScanner),
        Triple("settings", "Settings", Icons.Filled.Settings)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (route, label, icon) ->
                    val isSelected = selectedRoute == route
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { role = Role.Tab }
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(route) }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) activeColor.copy(alpha = 0.14f) else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) activeColor else inactiveColor
                        )
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// RISK GAUGE — Animated Circular Indicator
// ═══════════════════════════════════════════════════════════════════════════════
// The gauge animates from 0 to the target score over 1500ms with a
// FastOutSlowInEasing curve. Uses StrokeCap.Round for a polished look
// and a subtle shadow beneath the circle for depth.

@Composable
fun LGRiskGauge(score: Int, modifier: Modifier = Modifier) {
    // ── Animated fill: starts at 0, eases to target score / full complete sweep
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "RiskGaugeAnimation"
    )

    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "RiskScoreNumberAnimation"
    )

    // ── Dynamic color: Crimson for high, Amber for moderate, Emerald for low
    val gaugeColor = when {
        score > 60 -> Color(0xFFD90429)    // Vivid Crimson — high risk
        score > 30 -> Color(0xFFE9A319)    // Rich Amber — moderate risk
        else       -> Color(0xFF2A9D8F)    // Emerald Green — low risk
    }

    val riskLabel = when {
        score > 60 -> "HIGH RISK"
        score > 30 -> "MODERATE RISK"
        else       -> "LOW RISK · SAFE"
    }

    // ── Track background color (adapts to theme) ─────────────────────────
    val trackColor = LocalLGColors.current.Border.copy(alpha = 0.35f)

    // ── Outer container with subtle shadow for depth ─────────────────────
    Box(
        modifier = modifier
            .size(160.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = gaugeColor.copy(alpha = 0.15f),
                spotColor = gaugeColor.copy(alpha = 0.10f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Arc Canvas ───────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 14.dp.toPx()

            // Background track — full arc sweep
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Filled score arc — For low scores (<=30), sweeps complete 270f green circle for a sound, confident seal
            val effectiveSweep = if (score <= 30) {
                270f * animatedProgress
            } else {
                270f * (animatedScore / 100f).coerceIn(0.1f, 1f)
            }

            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = effectiveSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // ── Score Text ───────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                color = gaugeColor
            )
            Text(
                text = riskLabel,
                style = MaterialTheme.typography.labelSmall,
                color = LocalLGColors.current.TextSecondary,
                letterSpacing = 1.2.sp
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// SEVERITY UTILITIES — Colors & Labels
// ═══════════════════════════════════════════════════════════════════════════════
// Used by RedFlagCard and LGBadge to dynamically style risk indicators.

fun severityColor(severity: Int): Color = when (severity) {
    3    -> Color(0xFFD90429)    // Vivid Crimson — HIGH
    2    -> Color(0xFFE9A319)    // Rich Amber — MEDIUM
    else -> Color(0xFF2A9D8F)   // Emerald Green — LOW
}

fun severityLabel(severity: Int): String = when (severity) {
    3    -> "HIGH"
    2    -> "MEDIUM"
    else -> "LOW"
}


// ═══════════════════════════════════════════════════════════════════════════════
// PILL BADGE — Dynamic Risk Level Indicator
// ═══════════════════════════════════════════════════════════════════════════════
// Fully pill-shaped badge (50% corner radius) with solid vibrant background
// and pure white bold text. Color is dynamically linked to risk level.

@Composable
fun LGBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(percent = 50))  // Pill shape
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// PRESS-SCALE MODIFIER — Micro-interaction for all tappable elements
// ═══════════════════════════════════════════════════════════════════════════════

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
// LG SURFACE — Elevated card container with theme-adaptive styling
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGSurface(
    modifier: Modifier = Modifier,
    padding: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalLGColors.current
    val isDark = colors == LGColorsDark
    val shape = RoundedCornerShape(16.dp)

    val borderModifier = if (isDark) {
        Modifier.border(1.dp, colors.Border, shape)
    } else {
        Modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.08f),
            spotColor = Color.Black.copy(alpha = 0.08f)
        )
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .background(colors.Surface, shape)
            .lgPressClickable(onClick = onClick)
            .padding(padding)
    ) {
        content()
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// LG BUTTON — Primary action button with press-scale micro-interaction
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    color: Color = LocalLGColors.current.TextPrimary,
    textColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = LocalLGColors.current
    val shape = RoundedCornerShape(12.dp)  // 12dp radius per design spec

    val buttonModifier = if (filled) {
        Modifier.background(color, shape)
    } else {
        Modifier.border(1.dp, color, shape)
    }

    Box(
        modifier = modifier
            .semantics { role = Role.Button }
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)  // Accessibility touch target
            .then(buttonModifier)
            .lgPressClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val resolvedTextColor = textColor ?: if (filled) {
                if (color == colors.TextPrimary) colors.Background else Color.White
            } else {
                color
            }

            CompositionLocalProvider(
                LocalContentColor provides resolvedTextColor
            ) {
                content()
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// LG TEXT FIELD — Themed input field with focus border animation
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
    val isDark = colors == LGColorsDark
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor = if (isDark) Color(0xFF21262D) else colors.Surface
    val shape = RoundedCornerShape(12.dp)

    val borderStroke = if (isFocused) {
        BorderStroke(1.5.dp, colors.TrustBlue)
    } else {
        BorderStroke(1.dp, colors.Border)
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = label }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .onFocusChanged { isFocused = it.isFocused }
            .background(backgroundColor, shape)
            .border(borderStroke, shape)
            .padding(16.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = label,
                style = LGType.Body.copy(color = colors.TextSecondary)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LGType.Body.copy(color = colors.TextPrimary),
            cursorBrush = SolidColor(colors.TrustBlue),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// LG TOP BAR — Minimal app header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LGTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = LocalLGColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(colors.Background)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon != null) {
            navigationIcon()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = LGType.Headline.copy(color = colors.TextPrimary),
            modifier = Modifier.weight(1f)
        )
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}