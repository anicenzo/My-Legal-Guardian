package com.example.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NegotiationTemplateEngine
import com.example.ui.AuditState
import com.example.ui.MainViewModel
import com.example.ui.primitives.LGRiskGauge
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

@Composable
fun AuditResultScreen(
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Global Status Banner
        val score = audit.overallRiskScore
        val isHighRisk = score > 60
        val isModerateRisk = score in 31..60

        val colors = LocalLGColors.current

        val bannerColor = when {
            isHighRisk     -> colors.AccentDanger
            isModerateRisk -> colors.AccentWarning
            else           -> colors.AccentSafe
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

        // Risk Score Gauge
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

        // Predatory Clauses Found
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

        // Missing Safeguards
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

        // Cost Impact Card
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

        // Fight These Clauses
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp)
                                .clickable {
                                    val draft = audit.negotiationDrafts[flag.displayName]
                                        ?: NegotiationTemplateEngine.generateDraft(
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

            // Combined email button
            if (hasSelection) {
                Button(
                    onClick = {
                        val selectedFlags = checkedClauses.mapNotNull { clauseName ->
                            audit.matchedRedFlags.find { it.displayName == clauseName }
                        }
                        val email = NegotiationTemplateEngine.generateCombinedEmail(
                            selectedFlags = selectedFlags,
                            isPro = isProUser
                        )

                        clipboardManager.setText(AnnotatedString(email))
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        Toast.makeText(context, "Email Copied to Clipboard!", Toast.LENGTH_SHORT).show()

                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Proposed Amendments to Contract Draft")
                                putExtra(Intent.EXTRA_TEXT, email)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Negotiation Draft"))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = colors.PrimaryAccent,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draft Combined Email (${checkedClauses.size})",
                        style = LGType.Title.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            // PDF Export Button
            OutlinedButton(
                onClick = {
                    if (!isProUser) {
                        onPurchaseClick()
                    } else {
                        val pdfUri = viewModel.exportCurrentAuditPdf()
                        if (pdfUri != null) {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share PDF Audit Report"))
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
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
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

        // Scan Another / Unlock CTA
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
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
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
