package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors

// ═══════════════════════════════════════════════════════════════════════════════
// Idle Scan Screen — Flat editorial layout
// ═══════════════════════════════════════════════════════════════════════════════
// Removed: centered hero icon box, green reassurance banner, dot progress bar,
// "Powered by Anixium Studios" footer.
// Trust claims are stated once — in Settings → Privacy & Security.

@Composable
fun IdleScanScreen(
    isProUser: Boolean,
    freeScansRemaining: Int,
    onScanClick: () -> Unit,
    onImportPdfClick: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    val colors = LocalLGColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Editorial header — left-aligned, no icon chrome ──────────────────
        Text(
            text = "Contract Scanner",
            style = LGType.Display,
            color = colors.TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scan a lease, offer letter, or contract to flag risk.",
            style = LGType.Body,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        // ── Scan quota — factual text, no theatrics ───────────────────────────
        if (!isProUser) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Free scans today",
                    style = LGType.Caption,
                    color = colors.TextTertiary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$freeScansRemaining / 3",
                    style = LGType.Mono,
                    color = if (freeScansRemaining > 0) colors.TextSecondary else colors.RiskHigh
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Primary CTA ───────────────────────────────────────────────────────
        if (!isProUser && freeScansRemaining == 0) {
            LGButton(
                text = "Unlock Unlimited Scans",
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LGButton(
                text = "Scan New Contract",
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Secondary CTA — Import PDF ────────────────────────────────────────
        LGButton(
            text = "Import PDF",
            onClick = onImportPdfClick,
            variant = LGButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
