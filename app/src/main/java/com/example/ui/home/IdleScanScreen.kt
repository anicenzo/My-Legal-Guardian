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
// IDLE SCAN SCREEN — De-cluttered Working Hub
// ═══════════════════════════════════════════════════════════════════════════════

data class SafeguardCategory(
    val title: String,
    val description: String
)

val LEGAL_AI_MONITORS_LIST = listOf(
    SafeguardCategory("Auto-Renewal & Lock-Ins", "Flags indefinite extensions without written notice"),
    SafeguardCategory("Uncapped Liability", "Catches one-sided indemnity and damage waivers"),
    SafeguardCategory("Non-Refundable Deposits", "Detects unfair security deposit forfeiture clauses"),
    SafeguardCategory("Surprise Termination Fees", "Identifies multi-month penalty charges"),
    SafeguardCategory("Mandatory Arbitration", "Highlights forced arbitration and jury trial waivers"),
    SafeguardCategory("Unilateral Modifications", "Catches clauses allowing terms to change without consent")
)

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
    onImportClick: () -> Unit,
    onPurchaseClick: () -> Unit,
    onSampleScanClick: (sampleText: String, sampleTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalLGColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Block 1: Merged Header Row (Title + Free-Scan Badge) ─────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Contract Auditor",
                style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                color = colors.TextPrimary
            )

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

        // ── Block 2: Flat Scan Preview Frame (Plain bordered rectangle) ───────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.Surface)
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(14.dp))
                .clickable(onClickLabel = "Align contract to scan") { onScanClick() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Align contract to scan",
                    style = LGType.Heading.copy(fontWeight = FontWeight.Medium),
                    color = colors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Block 3: Primary & Secondary Action CTAs ──────────────────────────
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

        Spacer(modifier = Modifier.height(12.dp))

        LGButton(
            text = "Import Document",
            icon = Icons.Filled.FileOpen,
            onClick = onImportClick,
            variant = LGButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Block 4: Try Sample Audit Row (Compact single line) ───────────────
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(colors.AccentMuted, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Sample audit demo",
                            tint = colors.Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Try Sample Audit",
                        style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.AccentMuted)
                            .border(BorderStroke(1.dp, colors.Accent.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TRY",
                            style = LGType.Caption.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = colors.Accent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Run demo",
                    tint = colors.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

