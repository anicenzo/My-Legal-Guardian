package com.example.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.Surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)) {
            // Collapsed Header: Checkbox + Title + Pill Badge + Chevron
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
                        checkedColor = colors.PrimaryAccent,
                        uncheckedColor = colors.TextSecondary
                    )
                )
                Text(
                    text = flag.displayName,
                    style = LGType.Title.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Pill-shaped severity badge
                LGBadge(
                    text = sevLabel,
                    color = sevColor
                )

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = colors.TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation)
                )
            }

            // Expanded Content: Explanation + Contract Quote
            if (expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 48.dp, end = 0.dp,
                        top = 8.dp, bottom = 8.dp
                    )
                ) {
                    Text(
                        text = flag.explanation,
                        style = LGType.Body.copy(
                            lineHeight = 20.sp
                        ),
                        color = colors.TextSecondary,
                        maxLines = Int.MAX_VALUE
                    )
                    if (flag.matchedSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.ExpandedSurface,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "\"${flag.matchedSnippet}\"",
                                style = LGType.Mono.copy(
                                    lineHeight = 17.sp
                                ),
                                color = colors.TextPrimary,
                                modifier = Modifier.padding(14.dp),
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    }
                }
            }
        }
    }
}
