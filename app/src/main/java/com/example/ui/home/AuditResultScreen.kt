package com.example.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
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
    val checkedClauses by viewModel.checkedClauses.collectAsState()

    val colors = LocalLGColors.current
    val score = audit.overallRiskScore
    val isHighRisk     = score > 60
    val isModerateRisk = score in 31..60

    // Risk semantics using new token names
    val riskColor = when {
        isHighRisk     -> colors.RiskHigh
        isModerateRisk -> colors.RiskMedium
        else           -> colors.RiskLow
    }
    val riskBannerBg = when {
        isHighRisk     -> colors.RiskHigh.copy(alpha = 0.10f)
        isModerateRisk -> colors.RiskMedium.copy(alpha = 0.10f)
        else           -> colors.RiskLow.copy(alpha = 0.10f)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Risk status banner — flat, hairline border ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(riskBannerBg, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, riskColor.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = bannerIcon, contentDescription = null, tint = riskColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = bannerText,
                    style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                    color = riskColor
                )
            }
        }

        // ── Risk gauge ────────────────────────────────────────────────────────
        if (audit.overallRiskScore > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Surface, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LGRiskGauge(score = audit.overallRiskScore)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Overall Risk Assessment", style = LGType.Caption, color = colors.TextSecondary)
                }
            }
        }

        // ── Predatory Clauses ─────────────────────────────────────────────────
        val predatoryHeaderColor = if (audit.matchedRedFlags.isNotEmpty()) colors.RiskHigh else colors.RiskLow
        SectionHeader(title = "Predatory Clauses Found: ${audit.matchedRedFlags.size}", color = predatoryHeaderColor)

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
                        onCheckedChange = { checked -> viewModel.toggleClauseSelection(flag.displayName, checked) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.RiskLow.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, colors.RiskLow.copy(alpha = 0.20f)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, "Safe", tint = colors.RiskLow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("No predatory clauses detected.", style = LGType.Body, color = colors.RiskLow)
                }
            }
        }

        // ── Missing Safeguards ────────────────────────────────────────────────
        val safeguardsPresent = audit.missingMandatoryClauses.isEmpty()
        SectionHeader(
            title = "Missing Mandatory Safeguards",
            color = if (safeguardsPresent) colors.RiskLow else colors.TextPrimary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(
                    if (safeguardsPresent) colors.RiskLow.copy(alpha = 0.08f) else colors.Surface,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    BorderStroke(1.dp,
                        if (safeguardsPresent) colors.RiskLow.copy(alpha = 0.20f) else colors.Border
                    ), RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            if (safeguardsPresent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, "All present", tint = colors.RiskLow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("All standard safeguards present", style = LGType.Body, color = colors.RiskLow)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    audit.missingMandatoryClauses.forEach { clause ->
                        SafeguardExpandableItem(clause = clause)
                    }
                }
            }
        }

        // ── Cost Impact ───────────────────────────────────────────────────────
        if (audit.realCostBreakdown != null) {
            val cost = audit.realCostBreakdown
            SectionHeader(title = "Cost Impact Breakdown", color = colors.TextPrimary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Surface, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    CostRow("Base Amount", "${cost.currencySymbol}${formatAmount(cost.baseAmount)}")
                    CostRow("Maintenance", "${cost.currencySymbol}${formatAmount(cost.maintenanceAmount)}")
                    CostRow("Tax", "${cost.currencySymbol}${formatAmount(cost.taxAmount)}")
                    HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold), color = colors.TextPrimary)
                        Text("${cost.currencySymbol}${formatAmount(cost.totalAmount)}", style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold), color = colors.RiskHigh)
                    }
                }
            }
        }

        // ── Fight These Clauses ───────────────────────────────────────────────
        if (audit.matchedRedFlags.isNotEmpty()) {
            val hasSelection = checkedClauses.isNotEmpty()
            SectionHeader(title = "Fight These Clauses", color = colors.TextPrimary)
            Text("Select clauses above, then draft a combined negotiation email.", style = LGType.Caption, color = colors.TextSecondary)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Surface, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
            ) {
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
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(flag.displayName, style = LGType.Body.copy(fontWeight = FontWeight.Medium), color = colors.TextPrimary, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Filled.Email, contentDescription = null, tint = colors.TextTertiary, modifier = Modifier.size(18.dp))
                    }
                    if (index < audit.matchedRedFlags.lastIndex) {
                        HorizontalDivider(color = colors.Border, thickness = 1.dp)
                    }
                }
            }

            // Combined email button
            if (hasSelection) {
                LGButton(
                    text = "Draft Combined Email (${checkedClauses.size})",
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // PDF Export
            LGButton(
                text = if (isProUser) "Export PDF Report" else "Export PDF Report (Pro)",
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
                                Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to generate PDF Report", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                variant = LGButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── Scan another / unlock CTA ─────────────────────────────────────────
        if (!isProUser && freeScansRemaining == 0) {
            LGButton(text = "Unlock Unlimited Scans", onClick = onPurchaseClick, modifier = Modifier.fillMaxWidth())
        } else {
            LGButton(text = "Scan Another Contract", onClick = onReset, variant = LGButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
