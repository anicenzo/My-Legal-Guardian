package com.example.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

@Composable
fun IdleScanScreen(
    isProUser: Boolean,
    freeScansRemaining: Int,
    onScanClick: () -> Unit,
    onImportPdfClick: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    val colors = LocalLGColors.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Action Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colors.Surface
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Hero Graphic — Flat Document Icon (Navy + Scanner Blue)
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background document body: Deep Navy rounded rect
                    Box(
                        modifier = Modifier
                            .size(76.dp, 88.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.TrustBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        // Document lines (white) to suggest text content
                        Column(
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                        ) {
                            Box(Modifier.size(width = 38.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.6f)))
                            Box(Modifier.size(width = 30.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.4f)))
                            Box(Modifier.size(width = 34.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.4f)))
                            Box(Modifier.size(width = 26.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.3f)))
                        }
                    }
                    // Scanner Blue accent tab in top-right corner (scan badge)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(bottomStart = 12.dp, topEnd = 14.dp))
                            .background(colors.PrimaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DocumentScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ready to Scan",
                    style = LGType.Display.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = colors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Analyze contracts instantly for hidden traps,\npredatory clauses, and missing safeguards.",
                    style = LGType.Body.copy(
                        color = colors.TextSecondary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Free scans 3-dot indicator
                if (!isProUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = LGSpacing.md)
                    ) {
                        Text(
                            text = "Daily free scans:",
                            style = LGType.Label.copy(
                                color = colors.TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..3) {
                                val isRemaining = i <= freeScansRemaining
                                Box(
                                    modifier = Modifier
                                        .size(width = 18.dp, height = 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isRemaining) colors.AccentSafe else colors.Border.copy(alpha = 0.6f)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$freeScansRemaining/3",
                            style = LGType.Label.copy(
                                color = if (freeScansRemaining > 0) colors.TextPrimary else colors.AccentDanger,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Primary CTA
                if (!isProUser && freeScansRemaining == 0) {
                    AnimatedPrimaryButton(
                        text = "Unlock Unlimited Scans",
                        icon = Icons.Filled.Star,
                        onClick = onPurchaseClick
                    )
                } else {
                    AnimatedPrimaryButton(
                        text = "Scan New Contract",
                        icon = Icons.Filled.DocumentScanner,
                        onClick = onScanClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary CTA: Import PDF
                OutlinedButton(
                    onClick = onImportPdfClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = "Import PDF",
                        tint = colors.PrimaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import PDF Contract",
                        style = LGType.Button,
                        color = colors.TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrivacyBanner()
        StudioFooter()
    }
}
