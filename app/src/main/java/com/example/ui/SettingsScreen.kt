package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.home.LEGAL_AI_MONITORS_LIST
import com.example.ui.home.StudioFooter
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.util.findActivity

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Settings Screen (dark-only, biometric toggle, privacy policy)
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val fragmentActivity = context as? androidx.fragment.app.FragmentActivity ?: (activity as? androidx.fragment.app.FragmentActivity)
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val isBiometricEnabled by viewModel.biometricLock.collectAsState(initial = false)
    val colors = LocalLGColors.current

    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showSafeguards by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
    ) {
        // ── Flat top bar — no shadow, solid background ─────────────────────────
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
                            imageVector = if (isProUser) Icons.Filled.Verified else Icons.Filled.Shield,
                            contentDescription = "Membership status",
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

            // ── Section 2: Privacy & Security ─────────────────────────────────
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

                // Biometric Vault Lock Row with functional Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.Accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Biometric Lock",
                            tint = colors.Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biometric Vault Lock",
                            style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBiometricEnabled) "Enabled: prompt required to open Vault"
                                   else "Disabled: tap to protect contracts with biometric unlock",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (fragmentActivity != null && com.example.util.BiometricAuthHelper.canAuthenticate(context)) {
                                    com.example.util.BiometricAuthHelper.promptBiometric(
                                        activity = fragmentActivity,
                                        title = "Enable Vault Lock",
                                        subtitle = "Verify your biometric credential to activate vault protection",
                                        onSuccess = { viewModel.setBiometricLock(true) },
                                        onError = { err -> Toast.makeText(context, "Could not enable: $err", Toast.LENGTH_SHORT).show() }
                                    )
                                } else {
                                    Toast.makeText(context, "Biometrics or device credential not set up", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                viewModel.setBiometricLock(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.TextPrimary,
                            checkedTrackColor = colors.Accent,
                            uncheckedThumbColor = colors.TextSecondary,
                            uncheckedTrackColor = colors.Border
                        )
                    )
                }

                HorizontalDivider(
                    color = colors.Border,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // Privacy Policy Interactive Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPrivacyPolicy = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.RiskLow.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Policy,
                            contentDescription = "Privacy Policy",
                            tint = colors.RiskLow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Policy",
                            style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Read our strict 100% on-device zero-data-collection policy",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open policy",
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Section 3: About ──────────────────────────────────────────────
            SettingsSectionHeader("ABOUT")
            SettingsSection {
                SettingsInfoRow("App", "Legal AI — Contract Scanner")
                HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                SettingsInfoRow("Version", "1.6.0 (Offline Build)")
                HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                SettingsInfoRow("Developer", "Anixium Studios")
                HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                // What we check for row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSafeguards = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "What We Check For",
                            style = LGType.Body.copy(fontWeight = FontWeight.Normal),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = "View all audited clause categories & protections",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "View safeguards",
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            StudioFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Privacy Policy Bottom Sheet ──────────────────────────────────────────
    if (showPrivacyPolicy) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacyPolicy = false },
            containerColor = colors.SurfaceElevated,
            contentColor = colors.TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                        color = colors.TextPrimary
                    )
                    IconButton(onClick = { showPrivacyPolicy = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.TextSecondary)
                    }
                }
                Text(
                    text = "Effective Date: September 2026 · Legal AI Offline Contract Scan",
                    style = LGType.Caption,
                    color = colors.TextTertiary
                )

                Spacer(modifier = Modifier.height(16.dp))

                PolicyBlock(
                    title = "1. 100% On-Device Processing",
                    content = "Legal AI is engineered as an offline-first contract auditor. Optical Character Recognition (OCR), clause extraction, and AI risk analysis run entirely on your device's local processors using local ML Kit and TensorFlow Lite runtimes. No contract text or images ever leave your device."
                )

                PolicyBlock(
                    title = "2. No Data Collection or Cloud Sharing",
                    content = "We do not collect, store, transmit, or sell any personal data, document content, audit scores, or device identifiers. There are no tracking scripts, third-party advertising SDKs, or analytics trackers."
                )

                PolicyBlock(
                    title = "3. Camera & Storage Access",
                    content = "Camera access is used strictly in real-time by the Google Document Scanner component to photograph physical contract pages. Document and image imports utilize the modern Android Photo Picker, which is permissionless and grants access only to the explicit files you select."
                )

                PolicyBlock(
                    title = "4. Biometric Data Security",
                    content = "When Biometric Vault Lock is enabled, authentication is handled directly by Android's hardware-backed BiometricPrompt system. The app never accesses, records, or transmits your fingerprint or facial recognition data."
                )

                PolicyBlock(
                    title = "5. In-App Purchases",
                    content = "Pro subscriptions are processed through Google Play Billing and verified anonymously via Qonversion. Financial details are handled solely by Google Play."
                )

                Spacer(modifier = Modifier.height(20.dp))

                LGButton(
                    text = "Close",
                    onClick = { showPrivacyPolicy = false },
                    variant = LGButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── What We Check For Bottom Sheet ───────────────────────────────────────
    if (showSafeguards) {
        ModalBottomSheet(
            onDismissRequest = { showSafeguards = false },
            containerColor = colors.SurfaceElevated,
            contentColor = colors.TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "What Legal AI Checks For",
                        style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                        color = colors.TextPrimary
                    )
                    IconButton(onClick = { showSafeguards = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.TextSecondary)
                    }
                }
                Text(
                    text = "The app audits contracts against pre-trained on-device rules for predatory terms and essential safeguards:",
                    style = LGType.Caption,
                    color = colors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LEGAL_AI_MONITORS_LIST.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.RiskLow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.RiskLow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = LGType.Caption,
                                color = colors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LGButton(
                    text = "Close",
                    onClick = { showSafeguards = false },
                    variant = LGButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PolicyBlock(title: String, content: String) {
    val colors = LocalLGColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(title, style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = colors.TextPrimary)
        Spacer(modifier = Modifier.height(3.dp))
        Text(content, style = LGType.Caption.copy(lineHeight = 18.sp), color = colors.TextSecondary)
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
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = LGType.Body.copy(fontWeight = FontWeight.Medium), color = colors.TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = LGType.Caption, color = colors.TextSecondary)
        }
    }
}

