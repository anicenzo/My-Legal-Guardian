package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.home.StudioFooter
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.util.findActivity

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS SCREEN — Soft UI, 100% Offline Management, Pro Subscription
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val colors = LocalLGColors.current
    val isDark = colors == LGColorsDark

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
    ) {
        // ── Top App Bar ──────────────────────────────────────────────────────
        Surface(
            color = colors.HeaderBackground,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = LGSpacing.lg, vertical = LGSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = colors.HeaderContent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(LGSpacing.sm))
                Text(
                    text = "Settings",
                    style = LGType.Title.copy(
                        color = colors.HeaderContent,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.HeaderContent.copy(alpha = 0.12f))
                        .clickable { viewModel.toggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = colors.HeaderContent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Main Content ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LGSpacing.lg, vertical = LGSpacing.md),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section 1: Membership & Subscription Card ────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isProUser) colors.AccentSafe.copy(alpha = 0.12f)
                                    else colors.Border.copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isProUser) Icons.Filled.Star else Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (isProUser) colors.AccentSafe else colors.TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isProUser) "Pro Member" else "Free Plan",
                                style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                                color = colors.TextPrimary
                            )
                            Text(
                                text = if (isProUser) "Unlimited scans & PDF reports active"
                                       else "$freeScansRemaining / 3 daily free scans remaining",
                                style = LGType.BodySmall,
                                color = colors.TextSecondary
                            )
                        }
                    }

                    if (!isProUser) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onPurchaseClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = colors.PrimaryAccent,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upgrade to Pro Unlimited",
                                style = LGType.Title.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            if (activity != null) {
                                viewModel.restorePurchases(
                                    context = activity,
                                    onSuccess = { Toast.makeText(context, "Purchases restored successfully!", Toast.LENGTH_SHORT).show() },
                                    onError = { err -> Toast.makeText(context, "Restore failed: $err", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Restore Purchases",
                            style = LGType.Label.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.TextSecondary
                            )
                        )
                    }
                }
            }

            // ── Section 2: Privacy & Offline Guarantee ───────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "PRIVACY & SECURITY",
                        style = LGType.Label.copy(
                            color = colors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsFeatureRow(
                        icon = Icons.Filled.Lock,
                        title = "100% Offline Engine",
                        subtitle = "OCR and AI risk analysis run completely on your device with zero internet transmission.",
                        iconTint = colors.AccentSafe
                    )

                    HorizontalDivider(
                        color = colors.Border.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    SettingsFeatureRow(
                        icon = Icons.Filled.Security,
                        title = "Zero Cloud Tracking",
                        subtitle = "No analytics, no ad tracking, and no external document uploading.",
                        iconTint = colors.AccentSafe
                    )
                }
            }

            // ── Section 3: App Information & Credits ─────────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "ABOUT",
                        style = LGType.Label.copy(
                            color = colors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Version",
                            style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = "1.0.0 (Offline Build)",
                            style = LGType.BodySmall,
                            color = colors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Developer",
                            style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = "Anixium Studios",
                            style = LGType.BodySmall,
                            color = colors.TextSecondary
                        )
                    }
                }
            }

            StudioFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color
) {
    val colors = LocalLGColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LGType.Subtitle.copy(fontWeight = FontWeight.SemiBold),
                color = colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = LGType.BodySmall,
                color = colors.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
