package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.primitives.LGBadge
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGRiskGauge
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.severityColor
import com.example.ui.primitives.severityLabel
import com.example.util.findActivity
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Premium Home Screen
// ═══════════════════════════════════════════════════════════════════════════════
// All business logic is preserved byte-for-byte from the original.
// Only UI components, styling, and micro-interactions have been overhauled.


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Primary CTA Button with press-scale animation
// ═══════════════════════════════════════════════════════════════════════════════
// Navy Blue background, bold white text, 12dp radius, 48dp minimum height,
// native ripple effect, and a subtle bounce-scale on press.

@Composable
fun AnimatedPrimaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)  // Accessibility: minimum touch target
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),       // 12dp radius per design spec
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary  // Deep Navy Blue
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Privacy Banner
// ═══════════════════════════════════════════════════════════════════════════════
// TASK 3 FIX: Dark mode uses translucent emerald (#2A9D8F @ 15% alpha) with
// bright green text/icons for a modern glowing aesthetic instead of the muddy
// secondaryContainer default.

@Composable
fun PrivacyBanner() {
    val isDark = LocalLGColors.current == LGColorsDark

    // Dark mode: translucent emerald glow. Light mode: soft green container.
    val bannerBackground = if (isDark) {
        Color(0xFF2A9D8F).copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val bannerContentColor = if (isDark) {
        Color(0xFF5EDECF)  // Bright, readable green for dark backgrounds
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = bannerBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Secure",
                tint = bannerContentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "100% Offline & Secure. Your data never leaves your device.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = bannerContentColor
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Studio Footer
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun StudioFooter() {
    Text(
        text = "Powered by Anixium Studios",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        textAlign = TextAlign.Center
    )
}


// ═══════════════════════════════════════════════════════════════════════════════
// ROOT COMPOSABLE — State machine drives screen transitions
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()

    // Paywall state
    var showPaywall by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.map { it.imageUri }?.let { uris ->
                viewModel.processScannedDocuments(uris)
            }
        }
    }

    // ── Paywall Bottom Sheet ──
    if (showPaywall) {
        PaywallBottomSheet(
            onDismiss = { showPaywall = false },
            viewModel = viewModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ═══════════════════════════════════════════════════════════════════
        // Top App Bar — Navy Blue with shield icon
        // ═══════════════════════════════════════════════════════════════════
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "My Legal Guardian",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.SansSerif  // Explicit SansSerif guarantee
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                // ── Theme Toggle ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                        .clickable { viewModel.toggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // Body — Animated screen transitions
        // ═══════════════════════════════════════════════════════════════════
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "ScreenTransition"
            ) { currentState ->
                when (currentState) {
                    is AuditState.Idle -> IdleScreen(
                        isProUser = isProUser,
                        freeScansRemaining = freeScansRemaining,
                        onScanClick = {
                            activity?.let { act ->
                                viewModel.attemptScan(
                                    onSuccess = {
                                        viewModel.getScannerClient().getStartScanIntent(act)
                                            .addOnSuccessListener { intentSender ->
                                                scannerLauncher.launch(
                                                    IntentSenderRequest.Builder(intentSender).build()
                                                )
                                            }
                                    },
                                    onLimitReached = { showPaywall = true }
                                )
                            }
                        },
                        onPurchaseClick = { showPaywall = true }
                    )
                    is AuditState.Scanning  -> StatusScreen("Extracting Document Text...")
                    is AuditState.Analyzing -> StatusScreen("Running AI Risk Analysis...")
                    is AuditState.Result    -> ResultScreen(
                        audit = currentState,
                        isProUser = isProUser,
                        freeScansRemaining = freeScansRemaining,
                        onReset = viewModel::reset,
                        onPurchaseClick = { showPaywall = true },
                        viewModel = viewModel
                    )
                    is AuditState.Error     -> ErrorScreen(currentState.message, viewModel::reset)
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// IDLE / LANDING SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun IdleScreen(
    isProUser: Boolean,
    freeScansRemaining: Int,
    onScanClick: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Main Action Card ─────────────────────────────────────────────
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            shape = RoundedCornerShape(16.dp),      // Rounded corners per spec
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp              // Soft modern depth
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(36.dp)
            ) {
                // ── Scan Icon ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ready to Scan",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Analyze contracts instantly for hidden traps,\npredatory clauses, and missing safeguards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Free scans counter ───────────────────────────────────
                if (!isProUser) {
                    Text(
                        text = "Free scans remaining today: $freeScansRemaining/3",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── Primary CTA ──────────────────────────────────────────
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrivacyBanner()
        StudioFooter()
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// PROGRESS / STATUS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StatusScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// ERROR SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ErrorScreen(message: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AnimatedPrimaryButton(
                        text = "Try Again",
                        onClick = onReset
                    )
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// RESULT SCREEN — The main audit report
// ═══════════════════════════════════════════════════════════════════════════════
// Contains: Risk banner, animated gauge, predatory clauses (expandable cards
// with checkboxes), missing safeguards, cost breakdown, and consolidated
// negotiation email CTA with haptic feedback on copy.

@Composable
private fun ResultScreen(
    audit: AuditState.Result,
    isProUser: Boolean,
    freeScansRemaining: Int,
    onReset: () -> Unit,
    onPurchaseClick: () -> Unit,
    viewModel: MainViewModel
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Track which clauses are checked for combined email via hoisted state
    val checkedClauses by viewModel.checkedClauses.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)  // Mathematically perfect spacing
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ═════════════════════════════════════════════════════════════════
        // Global Status Banner
        // ═════════════════════════════════════════════════════════════════
        val score = audit.overallRiskScore
        val isHighRisk = score > 60
        val isModerateRisk = score in 31..60

        val bannerColor = when {
            isHighRisk     -> MaterialTheme.colorScheme.error      // Vivid Crimson
            isModerateRisk -> Color(0xFFE9A319)                    // Rich Amber
            else           -> Color(0xFF2A9D8F)                    // Emerald Green
        }
        val bannerContainer = when {
            isHighRisk     -> MaterialTheme.colorScheme.errorContainer
            isModerateRisk -> Color(0xFFFFF8E1)
            else           -> Color(0xFFE0F5F1)
        }
        val bannerOnContainer = when {
            isHighRisk     -> MaterialTheme.colorScheme.onErrorContainer
            isModerateRisk -> Color(0xFFC68300)
            else           -> Color(0xFF1A7A6E)
        }
        val bannerIcon = when {
            isHighRisk     -> Icons.Filled.GppBad
            isModerateRisk -> Icons.Filled.Warning
            else           -> Icons.Filled.GppGood
        }
        val bannerText = when {
            isHighRisk     -> "HIGH RISK DETECTED"
            isModerateRisk -> "MODERATE RISK"
            else           -> "LOW RISK"
        }

        Surface(
            color = bannerContainer,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, bannerColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = bannerIcon,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = bannerOnContainer
                )
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Risk Score Gauge — Animated circular indicator
        // ═════════════════════════════════════════════════════════════════
        if (audit.overallRiskScore > 0) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    LGRiskGauge(score = audit.overallRiskScore)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Overall Risk Assessment",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Predatory Clauses — Expandable Accordion Cards with Checkboxes
        // ═════════════════════════════════════════════════════════════════
        val predatoryHeaderColor = if (audit.matchedRedFlags.isNotEmpty()) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.secondary
        }

        SectionHeader(
            title = "Predatory Clauses Found: ${audit.matchedRedFlags.size}",
            color = predatoryHeaderColor
        )

        if (audit.matchedRedFlags.isNotEmpty()) {
            audit.matchedRedFlags.forEachIndexed { index, flag ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = index * 80)) +
                            slideInVertically(tween(300, delayMillis = index * 80)) { it / 4 }
                ) {
                    RedFlagCard(
                        flag = flag,
                        isChecked = checkedClauses.contains(flag.displayName),
                        onCheckedChange = { checked ->
                            viewModel.toggleClauseSelection(flag.displayName, checked)
                        }
                    )
                }
            }
        } else {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Safe",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No predatory clauses detected.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Missing Safeguards
        // ═════════════════════════════════════════════════════════════════
        val safeguardsPresent = audit.missingMandatoryClauses.isEmpty()

        SectionHeader(
            title = "Missing Mandatory Safeguards",
            color = if (safeguardsPresent) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (safeguardsPresent) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (safeguardsPresent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "All present",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All standard safeguards present",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        audit.missingMandatoryClauses.forEach { clause ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Missing",
                                    tint = Color(0xFFE9A319),  // Rich Amber
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = clause,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Conditional "Cost Impact" Card
        // ═════════════════════════════════════════════════════════════════
        if (audit.realCostBreakdown != null) {
            val cost = audit.realCostBreakdown
            SectionHeader(
                title = "Cost Impact Breakdown",
                color = MaterialTheme.colorScheme.onBackground
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    CostRow(label = "Base Amount", value = "${cost.currencySymbol}${formatAmount(cost.baseAmount)}")
                    CostRow(label = "Maintenance", value = "${cost.currencySymbol}${formatAmount(cost.maintenanceAmount)}")
                    CostRow(label = "Tax", value = "${cost.currencySymbol}${formatAmount(cost.taxAmount)}")
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${cost.currencySymbol}${formatAmount(cost.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // "Fight These Clauses" — Consolidated Negotiation Email CTA
        // ═════════════════════════════════════════════════════════════════
        // Sleek minimalist list replaces the old stacked purple buttons.
        // Each clause row: title on left, copy icon on right.
        // Haptic feedback on copy action.

        if (audit.matchedRedFlags.isNotEmpty()) {
            val hasSelection = checkedClauses.isNotEmpty()

            SectionHeader(
                title = "Fight These Clauses",
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Select the clauses above, then draft a combined negotiation email.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Individual clause copy rows ──────────────────────────────
            // Sleek list: title left, copy icon right. Replaces the old
            // massive stacked buttons.
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    audit.matchedRedFlags.forEachIndexed { index, flag ->
                        // ── Minimalist clause row ────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable {
                                    // Copy individual clause negotiation draft
                                    val draft = audit.negotiationDrafts[flag.displayName]
                                        ?: com.example.engine.NegotiationTemplateEngine.generateDraft(
                                            category = flag.displayName,
                                            clauseText = flag.matchedSnippet.ifBlank { flag.explanation },
                                            isPro = isProUser
                                        )
                                    clipboardManager.setText(AnnotatedString(draft))
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    Toast.makeText(context, "${flag.displayName} draft copied!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = flag.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy draft for ${flag.displayName}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Divider between rows (not after the last one)
                        if (index < audit.matchedRedFlags.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            // ── Combined email CTA button (Surfaced only when clauses are selected) ──
            if (hasSelection) {
                Button(
                    onClick = {
                        val selectedFlags = checkedClauses.mapNotNull { clauseName ->
                            audit.matchedRedFlags.find { it.displayName == clauseName }
                        }
                        val email = com.example.engine.NegotiationTemplateEngine.generateCombinedEmail(
                            selectedFlags = selectedFlags,
                            isPro = isProUser
                        )

                        clipboardManager.setText(AnnotatedString(email))
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        Toast.makeText(context, "Email Copied to Clipboard!", Toast.LENGTH_SHORT).show()

                        try {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Proposed Amendments to Contract Draft")
                                putExtra(android.content.Intent.EXTRA_TEXT, email)
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Negotiation Draft"))
                        } catch (e: Exception) {
                            // Clipboard copy already succeeded
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draft Combined Email (${checkedClauses.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // ── PDF Export Button (Secondary Action) ──────────────────────
            OutlinedButton(
                onClick = {
                    if (!isProUser) {
                        onPurchaseClick()
                    } else {
                        val pdfUri = viewModel.exportCurrentAuditPdf()
                        if (pdfUri != null) {
                            try {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, pdfUri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share PDF Audit Report"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Share sheet failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to generate PDF Report", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isProUser) MaterialTheme.colorScheme.primary else Color(0xFFE9A319)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isProUser) MaterialTheme.colorScheme.primary else Color(0xFFE9A319)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isProUser) "Export PDF Report" else "Export PDF Report (Pro)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                if (!isProUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE9A319)
                    )
                }
            }
        }

        // ── Scan Another / Unlock CTA ────────────────────────────────────
        if (!isProUser && freeScansRemaining == 0) {
            AnimatedPrimaryButton(
                text = "Unlock Unlimited Scans",
                icon = Icons.Filled.Star,
                onClick = onPurchaseClick
            )
        } else {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Another Contract",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrivacyBanner()
        StudioFooter()
        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Section Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Red Flag Detail Card (Expandable Accordion + Checkbox)
// ═══════════════════════════════════════════════════════════════════════════════
// Elevated card with 16dp corners, 4dp elevation. Pill-shaped severity badge
// with solid color background and white text.

@Composable
private fun RedFlagCard(
    flag: com.example.engine.MatchedRedFlag,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
        shape = RoundedCornerShape(16.dp),        // 16dp rounded corners
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp                // Modern depth
        )
    ) {
        Column(modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)) {
            // ── Collapsed Header: Checkbox + Title + Pill Badge + Chevron ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = flag.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // ── Pill-shaped severity badge ───────────────────────────
                // Solid vibrant background with pure white bold text.
                LGBadge(
                    text = sevLabel,
                    color = sevColor
                )

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation)
                )
            }

            // ── Expanded Content: Explanation + Contract Quote ────────────
            if (expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 48.dp, end = 0.dp,
                        top = 8.dp, bottom = 8.dp
                    )
                ) {
                    Text(
                        text = flag.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE
                    )
                    if (flag.matchedSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "\"${flag.matchedSnippet}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Cost Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CostRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// UTILITY
// ═══════════════════════════════════════════════════════════════════════════════

private fun formatAmount(amount: Double): String {
    return String.format("%,.2f", amount)
}