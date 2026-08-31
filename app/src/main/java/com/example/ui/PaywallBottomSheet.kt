package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

// ═══════════════════════════════════════════════════════════════════════════════
// My Legal Guardian — Premium Paywall Bottom Sheet
// ═══════════════════════════════════════════════════════════════════════════════
// All Qonversion purchase logic is preserved byte-for-byte.
// Only UI styling has been updated to match the premium design system.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        PaywallContent(
            viewModel = viewModel,
            onDismiss = onDismiss
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// PAYWALL CONTENT — Feature list, pricing, and purchase CTA
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PaywallContent(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val proPrice by viewModel.proProductPrice.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    val colors = com.example.ui.primitives.LocalLGColors.current
    val isDark = colors == com.example.ui.primitives.LGColorsDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ═════════════════════════════════════════════════════════════════
        // Shield Icon — Subtle Navy-to-Indigo Gradient Background
        // ═════════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isDark) {
                        Brush.linearGradient(listOf(Color(0xFF1E2638), Color(0xFF161E2E)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF0A192F), Color(0xFF1E3A5F)))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Pro",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // ═════════════════════════════════════════════════════════════════
        // Headline (Strict Sans-Serif)
        // ═════════════════════════════════════════════════════════════════
        Text(
            text = "Unlock Pro for\nUnlimited Scans",
            style = com.example.ui.primitives.LGType.Headline.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            ),
            color = colors.TextPrimary,
            textAlign = TextAlign.Center
        )

        // ═════════════════════════════════════════════════════════════════
        // Feature List — Emerald green checkmarks & generous spacing
        // ═════════════════════════════════════════════════════════════════
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "Unlimited offline scans")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "Export professional PDF reports")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "AI counter-proposal email drafting")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "100% private & on-device")
        }

        // ═════════════════════════════════════════════════════════════════
        // Price Disclosure (Plain text, no card chrome per spec)
        // ═════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = proPrice ?: "$4.99/month",
                style = com.example.ui.primitives.LGType.Title.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Cancel anytime · No commitment",
                style = com.example.ui.primitives.LGType.Caption.copy(
                    fontSize = 12.sp
                ),
                color = colors.TextSecondary
            )
        }

        // ═════════════════════════════════════════════════════════════════
        // CTA Button — Navy/Dark, 24dp radius, 56dp touch target
        // ═════════════════════════════════════════════════════════════════
        Button(
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
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (isDark) Color(0xFF2E7D32) else Color(0xFF0A192F),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 1.dp
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Unlock Pro",
                    style = com.example.ui.primitives.LGType.Title.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// REUSABLE: Feature Bullet — Emerald green accent checkmark
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FeatureBullet(icon: ImageVector, text: String) {
    val colors = com.example.ui.primitives.LocalLGColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Green accent checkmark container ─────────────────────────
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.AccentSafe.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.AccentSafe,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = com.example.ui.primitives.LGType.Body.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = colors.TextPrimary
        )
    }
}
