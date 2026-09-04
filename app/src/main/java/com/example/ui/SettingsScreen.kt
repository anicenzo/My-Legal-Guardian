package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.util.findActivity

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Settings Screen (dark-only, no theme toggle, trust stated once)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val colors = LocalLGColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
    ) {
        // ── Flat top bar — no shadow, no dark mode toggle ─────────────────────
        Column(modifier = Modifier.fillMaxWidth().background(colors.Background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = LGType.Title.copy(color = colors.TextPrimary)
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.Border))
        }

        // ── Main Content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = LGSpacing.lg, vertical = LGSpacing.md),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Section 1: Membership & Subscription ─────────────────────────
            SettingsSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isProUser) colors.RiskLow.copy(alpha = 0.12f)
                                else colors.Border
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProUser) Icons.Filled.Star else Icons.Filled.Shield,
                            contentDescription = null,
                            tint = if (isProUser) colors.RiskLow else colors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isProUser) "Pro Member" else "Free Plan",
                            style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = if (isProUser) "Unlimited scans & PDF reports active"
                                   else "$freeScansRemaining / 3 daily free scans remaining",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                }

                if (!isProUser) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LGButton(
                        text = "Upgrade to Pro",
                        onClick = onPurchaseClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
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
                        style = LGType.Caption.copy(fontWeight = FontWeight.Medium),
                        color = colors.TextSecondary
                    )
                }
            }

            // ── Section 2: Privacy & Security (trust stated HERE once) ────────
            SettingsSectionHeader("PRIVACY & SECURITY")
            SettingsSection {
                SettingsFeatureRow(
                    icon = Icons.Filled.Lock,
                    title = "100% Offline Engine",
                    subtitle = "OCR and AI risk analysis run completely on your device with zero internet transmission.",
                    iconTint = colors.RiskLow
                )
                HorizontalDivider(
                    color = colors.Border,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
                SettingsFeatureRow(
                    icon = Icons.Filled.Security,
                    title = "Zero Cloud Tracking",
                    subtitle = "No analytics, no ad tracking, and no external document uploading.",
                    iconTint = colors.RiskLow
                )
                HorizontalDivider(
                    color = colors.Border,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
                SettingsFeatureRow(
                    icon = Icons.Filled.Fingerprint,
                    title = "Biometric Vault Lock",
                    subtitle = "Saved contracts are protected by biometric authentication when enabled.",
                    iconTint = colors.Accent
                )
            }

            // ── Section 3: About ──────────────────────────────────────────────
            SettingsSectionHeader("ABOUT")
            SettingsSection {
                SettingsInfoRow("App", "Legal AI — Contract Scanner")
                HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                SettingsInfoRow("Version", "1.5.0 (Offline Build)")
                HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                SettingsInfoRow("Developer", "Anixium Studios")
            }

            StudioFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Section container — flat card with hairline border ────────────────────────
@Composable
private fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalLGColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Surface, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
            .padding(20.dp),
        content = content
    )
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = LocalLGColors.current
    Text(
        text = title,
        style = LGType.Caption.copy(fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp),
        color = colors.TextTertiary
    )
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    val colors = LocalLGColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = LGType.Body.copy(fontWeight = FontWeight.Normal), color = colors.TextPrimary)
        Text(value, style = LGType.Caption, color = colors.TextSecondary)
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
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = LGType.Body.copy(fontWeight = FontWeight.Medium), color = colors.TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = LGType.Caption, color = colors.TextSecondary)
        }
    }
}
