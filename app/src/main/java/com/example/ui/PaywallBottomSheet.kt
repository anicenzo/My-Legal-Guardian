package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionProductsCallback

import com.example.util.findActivity
import com.example.ui.primitives.LGBadge
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.clickableNoRipple

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Paywall Bottom Sheet (visual rebuild, logic preserved byte-for-byte)
// ═══════════════════════════════════════════════════════════════════════════════
// Removed: glowing squircle shield icon, green circular checkmarks, feature list.
// Replaced with: product proof surface showing a real scan result example.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LocalLGColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.Background,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .background(colors.Border, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    ) {
        PaywallContent(viewModel = viewModel, onDismiss = onDismiss)
    }
}

@Composable
private fun PaywallContent(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val proPrice by viewModel.proProductPrice.collectAsState()
    val colors = LocalLGColors.current
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header — editorial, left-aligned ─────────────────────────────────
        Text("Unlock Pro", style = LGType.Title, color = colors.TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Unlimited scans, exportable reports, and negotiation drafts.",
            style = LGType.Body,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Product proof — real output example, not a checkmark list ─────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.SurfaceElevated, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LGBadge(text = "HIGH RISK", color = colors.RiskHigh)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Non-refundable deposit clause",
                    style = LGType.Caption,
                    color = colors.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\"Tenant forfeits full deposit for any lease break, regardless of notice given.\"",
                style = LGType.Body.copy(fontStyle = FontStyle.Italic),
                color = colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pro drafts a counter-proposal email for this in one tap.",
                style = LGType.Caption.copy(fontWeight = FontWeight.Medium),
                color = colors.Accent
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Price ─────────────────────────────────────────────────────────────
        Text(
            text = proPrice ?: "$4.99/month",
            style = LGType.Title,
            color = colors.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text("Cancel anytime", style = LGType.Caption, color = colors.TextTertiary)

        Spacer(modifier = Modifier.height(20.dp))

        // ── CTA — flat LGButton, no glow ─────────────────────────────────────
        LGButton(
            text = if (isLoading) "Processing…" else "Continue",
            onClick = {
                val activity = context.findActivity()
                activity?.let { act ->
                    isLoading = true
                    // Qonversion purchase logic preserved byte-for-byte
                    Qonversion.shared.products(object : QonversionProductsCallback {
                        override fun onSuccess(products: Map<String, QProduct>) {
                            val qProduct = products[com.example.Constants.PRO_PRODUCT_ID]
                            if (qProduct != null) {
                                Qonversion.shared.purchase(act, qProduct, object : QonversionEntitlementsCallback {
                                    override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                                        isLoading = false
                                        if (entitlements[com.example.Constants.PRO_ENTITLEMENT_ID]?.isActive == true) {
                                            viewModel.isProUser.value = true
                                            onDismiss()
                                        }
                                    }
                                    override fun onError(error: QonversionError) {
                                        isLoading = false
                                        Toast.makeText(context, "Purchase failed: ${error.description}", Toast.LENGTH_LONG).show()
                                    }
                                })
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onError(error: QonversionError) {
                            isLoading = false
                            Toast.makeText(context, "Failed to load products: ${error.description}", Toast.LENGTH_LONG).show()
                        }
                    })
                } ?: run {
                    Toast.makeText(context, "Activity context required for billing", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Restore purchases — plain text link ───────────────────────────────
        Text(
            text = "Restore purchases",
            style = LGType.Caption,
            color = colors.TextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple {
                    viewModel.restorePurchases(
                        context = context,
                        onSuccess = { onDismiss() },
                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                    )
                }
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center
        )
    }
}
