package com.example.ui.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MatchedRedFlag
import com.example.engine.NegotiationTemplateEngine
import com.example.ui.primitives.*

@Composable
fun RedFlagCard(
    flag: MatchedRedFlag,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalLGColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val sevColor = severityColor(flag.severity)
    val sevLabel = severityLabel(flag.severity)
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "ChevronRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .background(colors.Surface, RoundedCornerShape(14.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (expanded) sevColor.copy(alpha = 0.4f) else colors.Border
                ),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(start = 6.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.Accent,
                        uncheckedColor = colors.TextTertiary
                    )
                )
                Text(
                    text = flag.displayName,
                    style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LGBadge(text = sevLabel, color = sevColor, showDot = true)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.TextTertiary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 44.dp, end = 0.dp, top = 6.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = flag.explanation,
                        style = LGType.BodyMuted.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = colors.TextSecondary
                    )

                    if (flag.matchedSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "DETECTED IN CONTRACT",
                            style = LGType.Caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = colors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.SurfaceSubdued, RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "\"${flag.matchedSnippet}\"",
                                style = LGType.ClauseQuote,
                                color = colors.TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Counter-measure & Copy action ─────────────────────────
                    val draftText = remember(flag.displayName, flag.matchedSnippet) {
                        NegotiationTemplateEngine.generateDraft(
                            category = flag.displayName,
                            clauseText = flag.matchedSnippet.ifBlank { flag.explanation },
                            isPro = true
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.AccentMuted.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, colors.Accent.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = colors.Accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RECOMMENDED COUNTER-MEASURE",
                                        style = LGType.Caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                                        color = colors.Accent
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clickable {
                                            clipboard.setText(AnnotatedString(draftText))
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            Toast.makeText(context, "Counter-proposal copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = colors.Accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy",
                                        style = LGType.Caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.Accent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = draftText.lines().take(4).joinToString("\n").trim() + "...",
                                style = LGType.Caption.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                color = colors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
