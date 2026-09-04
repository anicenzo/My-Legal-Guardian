package com.example.ui.home

import androidx.compose.animation.core.*
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.primitives.*

// ═══════════════════════════════════════════════════════════════════════════════
// IDLE SCAN SCREEN — Modern Scanner Home Hub
// ═══════════════════════════════════════════════════════════════════════════════

const val SAMPLE_LEASE_CONTRACT = """RESIDENTIAL LEASE AGREEMENT

This Agreement is entered into between Northview Property Management ("Landlord") and Tenant.

1. SECURITY DEPOSIT AND FEES
Tenant shall provide a security deposit of ${'$'}2,400.00 upon execution. The deposit is strictly non-refundable under all circumstances and shall be retained by Landlord upon move-out as a mandatory reconditioning fee.

2. LEASE TERM AND AUTOMATIC RENEWAL
The term commences on September 1, 2026. This Agreement automatically renews indefinitely for successive 12-month terms unless Tenant delivers certified notice exactly 90 days prior to expiration. Failure to provide exact notice results in an early termination fee equal to 3 months rent.

3. INDEMNIFICATION AND LIABILITY
Tenant agrees to indemnify and hold harmless Landlord, its agents and contractors, against all claims, liabilities, damages, and costs arising out of any occurrence on the premises, regardless of negligence. Tenant waives all claims against Landlord.

4. UNILATERAL MODIFICATIONS
Landlord reserves the right to modify rules without notice, alter common areas, and adjust utility surcharges at its sole discretion at any time.

5. LATE CHARGES AND PENALTIES
Rent is due on the 1st. A late fee of ${'$'}150 plus a daily late charge of ${'$'}25 per day shall apply after the 2nd calendar day of the month."""

@Composable
fun IdleScanScreen(
    isProUser: Boolean,
    freeScansRemaining: Int,
    onScanClick: () -> Unit,
    onImportPdfClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onSampleScanClick: (sampleText: String, sampleTitle: String) -> Unit
) {
    val colors = LocalLGColors.current

    // Animated scanner beam in the viewfinder card
    val infiniteTransition = rememberInfiniteTransition(label = "scannerBeam")
    val beamOffsetFraction by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beamOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top Status Pill / Quota ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Contract Auditor",
                    style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                    color = colors.TextPrimary
                )
                Text(
                    text = "Offline AI Document Safeguard",
                    style = LGType.Caption,
                    color = colors.TextTertiary
                )
            }

            if (isProUser) {
                LGBadge(text = "PRO UNLIMITED", color = colors.Accent, showDot = true)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(colors.Surface)
                        .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (freeScansRemaining > 0) colors.RiskLow else colors.RiskHigh,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$freeScansRemaining / 3 Free",
                        style = LGType.Caption.copy(fontWeight = FontWeight.SemiBold),
                        color = if (freeScansRemaining > 0) colors.TextSecondary else colors.RiskHigh
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Interactive Viewfinder Card ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(colors.SurfaceElevated, colors.Surface)
                    )
                )
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(18.dp))
                .clickable { onScanClick() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Document outline & corner brackets inside card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Moving scan beam line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.015f)
                        .align(Alignment.TopCenter)
                        .offset(y = (beamOffsetFraction * 140).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    colors.Accent.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Corner decorative marks
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopStart)
                        .border(BorderStroke(2.dp, colors.Accent), RoundedCornerShape(topStart = 4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .border(BorderStroke(2.dp, colors.Accent), RoundedCornerShape(topEnd = 4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomStart)
                        .border(BorderStroke(2.dp, colors.Accent), RoundedCornerShape(bottomStart = 4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .border(BorderStroke(2.dp, colors.Accent), RoundedCornerShape(bottomEnd = 4.dp))
                )

                // Center Icon + Prompts
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(colors.AccentMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DocumentScanner,
                            contentDescription = "Scan Document",
                            tint = colors.Accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Align Contract to Scan",
                        style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Flags hidden liabilities, non-competes, & traps",
                        style = LGType.Caption,
                        color = colors.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Primary & Secondary Action CTAs ──────────────────────────────────
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
                text = "Scan Physical Document",
                icon = Icons.Filled.CameraAlt,
                onClick = onScanClick,
                variant = LGButtonVariant.PrimaryHero,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LGButton(
            text = "Import PDF Contract",
            icon = Icons.Filled.PictureAsPdf,
            onClick = onImportPdfClick,
            variant = LGButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ── Quick Demo / Instant Sample Contract Card ────────────────────────
        LGSurface(
            modifier = Modifier.fillMaxWidth(),
            elevated = false,
            borderColor = colors.Accent.copy(alpha = 0.35f),
            onClick = {
                onSampleScanClick(SAMPLE_LEASE_CONTRACT, "Sample Residential Lease")
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors.AccentMuted, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = colors.Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Try Sample Contract Audit",
                            style = LGType.Subheading.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LGBadge(text = "DEMO", color = colors.Accent)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Instant analysis of a high-liability apartment lease",
                        style = LGType.Caption,
                        color = colors.TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Run demo",
                    tint = colors.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ── Protection Capabilities Grid ─────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "WHAT LEGAL AI MONITORS",
                style = LGType.Caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = colors.TextTertiary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val items = listOf(
                Pair("Auto-Renewal & Lock-Ins", "Flags indefinite extensions without written notice"),
                Pair("Uncapped Liability", "Catches one-sided indemnity and damage waivers"),
                Pair("Non-Refundable Deposits", "Detects unfair security deposit forfeiture clauses"),
                Pair("Surprise Termination Fees", "Identifies multi-month penalty charges")
            )

            items.forEach { (title, subtitle) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.RiskLow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = LGType.BodyMedium.copy(fontSize = 13.sp),
                            color = colors.TextPrimary
                        )
                        Text(
                            text = subtitle,
                            style = LGType.Caption.copy(fontSize = 11.sp),
                            color = colors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Offline Privacy Assurance Badge ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.SurfaceSubdued, RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, colors.BorderSubtle), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Confidential",
                tint = colors.RiskLow,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "100% On-Device & Private. Documents never leave your phone.",
                style = LGType.Caption.copy(fontSize = 11.sp),
                color = colors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
