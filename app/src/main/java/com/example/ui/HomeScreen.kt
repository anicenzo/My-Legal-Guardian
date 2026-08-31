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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.primitives.LGBadge
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGColorsLight
import com.example.ui.primitives.LGRiskGauge
import com.example.ui.primitives.LGType
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
// REUSABLE: Primary CTA Button with press-scale animation (Soft UI 24dp)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedPrimaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLGColors.current
    val isDark = colors == LGColorsDark
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
            .defaultMinSize(minHeight = 56.dp)   // 56dp touch target
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(24.dp),       // 24dp Soft UI radius
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isDark) Color(0xFF2E7D32) else Color(0xFF0A192F),
            contentColor = Color.White
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp,
            hoveredElevation = 5.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.sm))
        }
        Text(
            text = text,
            style = LGType.Title.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Privacy Banner — High Contrast 100% Offline Badge
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PrivacyBanner() {
    val colors = LocalLGColors.current

    Surface(
        color = colors.BannerSafeBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = com.example.ui.primitives.LGSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Secure",
                tint = colors.BannerSafeContent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.sm))
            Text(
                text = "100% Offline & Secure. Your data never leaves your device.",
                style = LGType.BodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.BannerSafeContent
                )
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
        style = LGType.Caption.copy(
            color = LocalLGColors.current.TextSecondary,
            fontWeight = FontWeight.Medium
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = com.example.ui.primitives.LGSpacing.lg),
        textAlign = TextAlign.Center
    )
}


// ═══════════════════════════════════════════════════════════════════════════════
// ROOT COMPOSABLE — State machine drives screen transitions
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.auditState.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()

    // Paywall state
    var showPaywall by remember { mutableStateOf(false) }

    // Scanner launcher callback (Google Play services Document Scanner API)
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
        // Top App Bar — Dynamic Navigation with Back Button on Results Screen
        // ═══════════════════════════════════════════════════════════════════
        val colors = LocalLGColors.current
        val isResultState = state is AuditState.Result

        Surface(
            color = colors.HeaderBackground,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = com.example.ui.primitives.LGSpacing.lg, vertical = com.example.ui.primitives.LGSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isResultState) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.HeaderContent.copy(alpha = 0.12f))
                            .clickable { viewModel.reset() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Scan",
                            tint = colors.HeaderContent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.md))
                    Text(
                        text = "Audit Results",
                        style = LGType.Title.copy(
                            color = colors.HeaderContent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "App Logo",
                        tint = colors.HeaderContent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.sm))
                    Text(
                        text = "My Legal Guardian",
                        style = LGType.Title.copy(
                            color = colors.HeaderContent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Theme Toggle ─────────────────────────────────────────
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
                        contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = colors.HeaderContent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // Body — Animated screen transitions
        // ═══════════════════════════════════════════════════════════════════
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = com.example.ui.primitives.LGSpacing.lg)) {
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
    val colors = LocalLGColors.current
    val isDark = colors == LGColorsDark

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Main Action Card ─────────────────────────────────────────────
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
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
                // ── Hero Scan Icon with subtle Navy-to-Indigo gradient fill ─────
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isDark) {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color(0xFF1E2638), Color(0xFF161E2E))
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(Color(0xFF0A192F), Color(0xFF1E3A5F))
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
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

                // ── Free scans 3-dot/segment indicator ────────────────────
                if (!isProUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = com.example.ui.primitives.LGSpacing.md)
                    ) {
                        Text(
                            text = "Daily free scans:",
                            style = LGType.BodySmall.copy(
                                color = colors.TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.sm))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    val colors = LocalLGColors.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = colors.AccentSafe,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = LGType.Title.copy(
                fontWeight = FontWeight.SemiBold,
                color = colors.TextPrimary
            )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)  // Consistent 12dp spacing
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ═════════════════════════════════════════════════════════════════
        // Global Status Banner
        // ═════════════════════════════════════════════════════════════════
        val score = audit.overallRiskScore
        val isHighRisk = score > 60
        val isModerateRisk = score in 31..60

        val colors = LocalLGColors.current

        val bannerColor = when {
            isHighRisk     -> colors.AccentDanger   // Red #D32F2F
            isModerateRisk -> colors.AccentWarning  // Amber #F9A825
            else           -> colors.AccentSafe     // Green #2E7D32
        }
        val bannerContainer = when {
            isHighRisk     -> colors.BannerDangerBackground
            isModerateRisk -> colors.BannerWarningBackground
            else           -> colors.BannerSafeBackground
        }
        val bannerOnContainer = when {
            isHighRisk     -> colors.BannerDangerContent
            isModerateRisk -> colors.BannerWarningContent
            else           -> colors.BannerSafeContent
        }
        val bannerIcon = when {
            isHighRisk     -> Icons.Filled.GppBad
            isModerateRisk -> Icons.Filled.Warning
            else           -> Icons.Filled.GppGood
        }
        val bannerText = when {
            isHighRisk     -> "HIGH RISK DETECTED"
            isModerateRisk -> "MODERATE RISK"
            else           -> "LOW RISK · SAFE"
        }

        Surface(
            color = bannerContainer,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = bannerIcon,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = bannerText,
                    style = LGType.Title.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    ),
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    LGRiskGauge(score = audit.overallRiskScore)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Overall Risk Assessment",
                        style = LGType.Body.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.TextSecondary
                    )
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Predatory Clauses — Expandable Accordion Cards with Checkboxes
        // ═════════════════════════════════════════════════════════════════
        val predatoryHeaderColor = if (audit.matchedRedFlags.isNotEmpty()) {
            colors.AccentDanger
        } else {
            colors.AccentSafe
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
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.BannerSafeBackground
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Safe",
                        tint = colors.AccentSafe,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No predatory clauses detected.",
                        style = LGType.Body.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.BannerSafeContent
                    )
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // Missing Safeguards — Interactive Expandable Items
        // ═════════════════════════════════════════════════════════════════
        val safeguardsPresent = audit.missingMandatoryClauses.isEmpty()

        SectionHeader(
            title = "Missing Mandatory Safeguards",
            color = if (safeguardsPresent) {
                colors.AccentSafe
            } else {
                colors.TextPrimary
            }
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (safeguardsPresent) {
                    colors.BannerSafeBackground
                } else {
                    colors.Surface
                }
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (safeguardsPresent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "All present",
                            tint = colors.AccentSafe,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All standard safeguards present",
                            style = LGType.Body.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.BannerSafeContent
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        audit.missingMandatoryClauses.forEach { clause ->
                            SafeguardExpandableItem(clause = clause)
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
                color = colors.TextPrimary
            )
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    CostRow(label = "Base Amount", value = "${cost.currencySymbol}${formatAmount(cost.baseAmount)}")
                    CostRow(label = "Maintenance", value = "${cost.currencySymbol}${formatAmount(cost.maintenanceAmount)}")
                    CostRow(label = "Tax", value = "${cost.currencySymbol}${formatAmount(cost.taxAmount)}")
                    HorizontalDivider(
                        color = colors.Border.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = "${cost.currencySymbol}${formatAmount(cost.totalAmount)}",
                            style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                            color = colors.AccentDanger
                        )
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // "Fight These Clauses" — Consolidated Negotiation Email CTA
        // ═════════════════════════════════════════════════════════════════

        if (audit.matchedRedFlags.isNotEmpty()) {
            val hasSelection = checkedClauses.isNotEmpty()

            SectionHeader(
                title = "Fight These Clauses",
                color = colors.TextPrimary
            )

            Text(
                text = "Select the clauses above, then draft a combined negotiation email.",
                style = LGType.BodySmall,
                color = colors.TextSecondary
            )

            // ── Individual clause copy rows ──────────────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    audit.matchedRedFlags.forEachIndexed { index, flag ->
                        // ── Minimalist clause row ────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp)
                                .clickable {
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
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = flag.displayName,
                                style = LGType.Body.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Draft negotiation email for ${flag.displayName}",
                                tint = colors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Divider between rows (not after the last one)
                        if (index < audit.matchedRedFlags.lastIndex) {
                            HorizontalDivider(
                                color = colors.Border.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 20.dp)
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
                        .defaultMinSize(minHeight = 56.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (colors == LGColorsDark) Color(0xFF2E7D32) else Color(0xFF0A192F),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draft Combined Email (${checkedClauses.size})",
                        style = LGType.Title.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                    .defaultMinSize(minHeight = 56.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isProUser) colors.Border else colors.AccentWarning
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.Surface,
                    contentColor = colors.TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = if (isProUser) colors.TextPrimary else colors.AccentWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isProUser) "Export PDF Report" else "Export PDF Report (Pro)",
                    style = LGType.Title.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )
                )
                if (!isProUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(16.dp),
                        tint = colors.AccentWarning
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
                    .defaultMinSize(minHeight = 56.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = colors.Border
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.Surface,
                    contentColor = colors.TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.DocumentScanner,
                    contentDescription = null,
                    tint = colors.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Another Contract",
                    style = LGType.Title.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.TextPrimary
                    )
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
// Legal Jargon Explanations — Plain-English safeguard descriptions
// ═══════════════════════════════════════════════════════════════════════════════

private val safeguardExplanations = mapOf(
    "Notice to Cure / Default Period" to
        "Gives you a grace period to fix any lease violations or overdue rent before the landlord can impose penalties or initiate eviction proceedings.",
    "Right to Quiet Enjoyment" to
        "The landlord cannot enter your property without proper notice or unreasonably disrupt your peaceful and private use of the space.",
    "Mutual Termination Rights" to
        "Allows both you and the landlord to end the agreement under fair conditions, rather than granting unilateral termination power to the landlord.",
    "Security Deposit Return Timeline" to
        "Requires the landlord to return your deposit within a specified statutory timeframe after move-out with an itemized statement of any deductions.",
    "Landlord Maintenance Obligations" to
        "Obligates the landlord to keep the property habitable — maintaining plumbing, electrical, heating, structural integrity, and essential services at their expense.",
    "Habitability Warranty" to
        "Guarantees that the premises comply with local health, safety, and building codes throughout the tenancy.",
    "Limitation of Liability" to
        "Protects the tenant from unreasonable one-sided indemnification clauses and unlimited financial liability for regular wear and tear."
)


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Safeguard Expandable Item — Interactive legal jargon explanation
// ═══════════════════════════════════════════════════════════════════════════════
// Amber warning icon + clause name + trailing info icon.
// Tap to expand/collapse a plain-English explanation with distinct background.

@Composable
private fun SafeguardExpandableItem(clause: String) {
    val colors = LocalLGColors.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        // ── Collapsed header: Amber warning icon + clause name + Info icon ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Missing safeguard",
                tint = colors.AccentWarning,  // Rich Amber #F9A825
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = clause,
                style = LGType.Subtitle.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = if (expanded) "Collapse explanation" else "Show explanation",
                tint = if (expanded) colors.TextPrimary else colors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // ── Expanded explanation with distinct background & md internal padding ──
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(150)) + shrinkVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 4.dp, top = 2.dp, bottom = 8.dp),
                color = colors.ExpandedSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = safeguardExplanations[clause]
                        ?: "$clause: Standard protection clause that must be included to safeguard tenant rights.",
                    style = LGType.BodySmall.copy(
                        lineHeight = 19.sp,
                        color = colors.TextPrimary
                    ),
                    modifier = Modifier.padding(com.example.ui.primitives.LGSpacing.md)
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Red Flag Detail Card (Expandable Accordion + Checkbox)
// ═══════════════════════════════════════════════════════════════════════════════
// Elevated card with 24dp corners, 2dp elevation. Pill-shaped severity badge
// with solid color background and white text.

@Composable
private fun RedFlagCard(
    flag: com.example.engine.MatchedRedFlag,
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
            // ── Collapsed Header: Checkbox + Title + Pill Badge + Chevron ──
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
                        checkedColor = if (colors == LGColorsDark) Color(0xFF2E7D32) else Color(0xFF0A192F),
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

                // ── Pill-shaped severity badge ───────────────────────────
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