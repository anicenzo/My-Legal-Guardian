package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.sp
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionProductsCallback

import com.example.util.findActivity
import com.example.ui.primitives.*

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
        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Legal AI Pro", style = LGType.Title.copy(fontWeight = FontWeight.Bold), color = colors.TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "UNLIMITED",
                style = LGType.Caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = colors.Accent
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Full offline protection against hidden liabilities, fees, & traps.",
            style = LGType.BodyMuted,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Product Proof Box ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.SurfaceElevated, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LGBadge(text = "HIGH RISK CAUGHT", color = colors.RiskHigh, showDot = true)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Deposit Forfeiture",
                    style = LGType.Caption,
                    color = colors.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "\"Security deposit is strictly non-refundable under all circumstances and shall be retained as a mandatory reconditioning fee.\"",
                style = LGType.ClauseQuote.copy(fontSize = 13.sp),
                color = colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.RiskLow,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Caught $2,400 potential loss & generated negotiation amendment",
                    style = LGType.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.RiskLow
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Value Props ───────────────────────────────────────────────────────
        val benefits = listOf(
            "Unlimited contract & lease scans",
            "Instant counter-measure draft generator",
            "Export professional PDF audit reports",
            "100% On-Device & confidential audit engine"
        )

        benefits.forEach { benefit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.RiskLow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = benefit,
                    style = LGType.Body.copy(fontSize = 13.sp),
                    color = colors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Price Card ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.Surface, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pro Membership",
                        style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.TextPrimary
                    )
                    Text("Cancel anytime · No commitments", style = LGType.Caption, color = colors.TextTertiary)
                }
                Text(
                    text = proPrice ?: "$4.99/mo",
                    style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                    color = colors.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Primary Action CTA ────────────────────────────────────────────────
        LGButton(
            text = if (isLoading) "Connecting to Store…" else "Upgrade to Pro",
            onClick = {
                val activity = context.findActivity()
                activity?.let { act ->
                    isLoading = true
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
            variant = LGButtonVariant.PrimaryHero,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Restore purchases ─────────────────────────────────────────────────
        Text(
            text = "Restore purchases",
            style = LGType.Caption.copy(fontWeight = FontWeight.Medium),
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
