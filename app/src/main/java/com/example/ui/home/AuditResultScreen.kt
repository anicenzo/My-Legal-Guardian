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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ContractType
import com.example.engine.NegotiationTemplateEngine
import com.example.ui.AuditState
import com.example.ui.MainViewModel
import com.example.ui.primitives.*

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

    val riskColor = when {
        isHighRisk     -> colors.RiskHigh
        isModerateRisk -> colors.RiskMedium
        else           -> colors.RiskLow
    }

    var selectedFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Executive Summary Hero Card ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Surface, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = audit.documentTitle,
                    style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                    color = colors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Detected: ${audit.contractType.displayName}",
                    style = LGType.Caption,
                    color = colors.TextTertiary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Grade pill
                LGBadge(
                    text = riskGrade(score),
                    color = riskColor,
                    showDot = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Circular Risk Gauge
                LGRiskGauge(score = audit.overallRiskScore)

                Spacer(modifier = Modifier.height(18.dp))

                // 3 Metric Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.SurfaceElevated, RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = "${audit.matchedRedFlags.size}",
                            style = LGType.Heading.copy(fontWeight = FontWeight.Bold),
                            color = colors.RiskHigh
                        )
                        Text(
                            text = "Red Flags",
                            style = LGType.Caption.copy(fontSize = 11.sp),
                            color = colors.TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.SurfaceElevated, RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = "${audit.missingMandatoryClauses.size}",
                            style = LGType.Heading.copy(fontWeight = FontWeight.Bold),
                            color = colors.RiskMedium
                        )
                        Text(
                            text = "Missing Safeguards",
                            style = LGType.Caption.copy(fontSize = 11.sp),
                            color = colors.TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.SurfaceElevated, RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (score > 60) "UNFAIR" else if (score > 30) "CAUTION" else "FAVORABLE",
                            style = LGType.Heading.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = riskColor
                        )
                        Text(
                            text = "Verdict",
                            style = LGType.Caption.copy(fontSize = 11.sp),
                            color = colors.TextTertiary
                        )
                    }
                }
            }
        }

        // ── Filter Chips Row ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "All",
                "Red Flags (${audit.matchedRedFlags.size})",
                "Safeguards (${audit.missingMandatoryClauses.size})"
            )

            filters.forEach { filter ->
                val isSelected = selectedFilter.startsWith(filter.take(3))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) colors.Accent else colors.Surface)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) colors.Accent else colors.Border
                            ),
                            RoundedCornerShape(100.dp)
                        )
                        .clickable {
                            selectedFilter = filter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = filter,
                        style = LGType.Caption.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                        color = if (isSelected) Color.White else colors.TextSecondary
                    )
                }
            }
        }

        // ── Predatory Clauses Section ─────────────────────────────────────────
        if (selectedFilter.startsWith("All") || selectedFilter.startsWith("Red")) {
            SectionHeader(
                title = "Predatory Clauses Found (${audit.matchedRedFlags.size})",
                color = if (audit.matchedRedFlags.isNotEmpty()) colors.RiskHigh else colors.RiskLow
            )

            if (audit.matchedRedFlags.isNotEmpty()) {
                audit.matchedRedFlags.forEachIndexed { index, flag ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 60)) +
                                slideInVertically(tween(300, delayMillis = index * 60)) { it / 4 }
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
                        Text("No predatory clauses detected in this agreement.", style = LGType.Body, color = colors.RiskLow)
                    }
                }
            }
        }

        // ── Missing Safeguards Section ────────────────────────────────────────
        if (selectedFilter.startsWith("All") || selectedFilter.startsWith("Safe")) {
            val safeguardsPresent = audit.missingMandatoryClauses.isEmpty()
            SectionHeader(
                title = "Missing Standard Safeguards (${audit.missingMandatoryClauses.size})",
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
                        Text("All standard protective safeguards are present.", style = LGType.Body, color = colors.RiskLow)
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

        // ── Cost Impact Breakdown ─────────────────────────────────────────────
        if (audit.realCostBreakdown != null && (selectedFilter.startsWith("All") || selectedFilter.startsWith("Cost"))) {
            val cost = audit.realCostBreakdown
            SectionHeader(title = "Estimated Cost Exposure", color = colors.TextPrimary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Surface, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    CostRow("Base Amount", "${cost.currencySymbol}${formatAmount(cost.baseAmount)}")
                    CostRow("Maintenance & Surcharges", "${cost.currencySymbol}${formatAmount(cost.maintenanceAmount)}")
                    CostRow("Taxes & Admin Fees", "${cost.currencySymbol}${formatAmount(cost.taxAmount)}")
                    HorizontalDivider(color = colors.Border, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Obligation", style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold), color = colors.TextPrimary)
                        Text("${cost.currencySymbol}${formatAmount(cost.totalAmount)}", style = LGType.Heading.copy(fontWeight = FontWeight.Bold), color = colors.RiskHigh)
                    }
                }
            }
        }

        // ── Batch Negotiation Actions ─────────────────────────────────────────
        if (audit.matchedRedFlags.isNotEmpty()) {
            val hasSelection = checkedClauses.isNotEmpty()
            SectionHeader(title = "Negotiation Actions", color = colors.TextPrimary)

            if (hasSelection) {
                LGButton(
                    text = "Draft Combined Email (${checkedClauses.size} Clauses)",
                    icon = Icons.Filled.Send,
                    onClick = {
                        val selectedFlags = checkedClauses.mapNotNull { clauseName ->
                            audit.matchedRedFlags.find { it.displayName == clauseName }
                        }
                        val email = NegotiationTemplateEngine.generateCombinedEmail(
                            selectedFlags = selectedFlags,
                            isPro = isProUser
                        )
                        clipboardManager.setText(AnnotatedString(email))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Combined negotiation email copied to clipboard!", Toast.LENGTH_SHORT).show()
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Proposed Amendments to Contract Draft")
                                putExtra(Intent.EXTRA_TEXT, email)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Negotiation Draft"))
                        } catch (_: Exception) {}
                    },
                    variant = LGButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LGButton(
                text = if (isProUser) "Export PDF Audit Report" else "Export PDF Audit Report (Pro)",
                icon = Icons.Filled.FileDownload,
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

        // ── Scan another / Unlock CTA ─────────────────────────────────────────
        if (!isProUser && freeScansRemaining == 0) {
            LGButton(
                text = "Unlock Unlimited Scans",
                icon = Icons.Filled.LockOpen,
                onClick = onPurchaseClick,
                variant = LGButtonVariant.PrimaryHero,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LGButton(
                text = "Scan Another Document",
                icon = Icons.Filled.Refresh,
                onClick = onReset,
                variant = LGButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
