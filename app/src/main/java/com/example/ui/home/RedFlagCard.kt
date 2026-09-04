package com.example.ui.home

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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MatchedRedFlag
import com.example.ui.primitives.LGBadge
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.severityColor
import com.example.ui.primitives.severityLabel

@Composable
fun RedFlagCard(
    flag: MatchedRedFlag,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalLGColors.current
    val sevColor = severityColor(flag.severity)
    val sevLabel = severityLabel(flag.severity)
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "ChevronRotation"
    )

    // Flat card — border only, no elevation
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .background(colors.Surface, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
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
                    style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                    color = colors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LGBadge(text = sevLabel, color = sevColor)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.TextTertiary,
                    modifier = Modifier.size(20.dp).rotate(chevronRotation)
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 44.dp, end = 0.dp, top = 6.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = flag.explanation,
                        style = LGType.Caption.copy(lineHeight = 20.sp),
                        color = colors.TextSecondary
                    )
                    if (flag.matchedSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.SurfaceElevated, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "\"${flag.matchedSnippet}\"",
                                style = LGType.Mono,
                                color = colors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
