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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)  // Eliminates manual Spacers
    ) {
        // ═════════════════════════════════════════════════════════════════
        // Shield Icon — Gradient Navy background
        // ═════════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Pro",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        // ═════════════════════════════════════════════════════════════════
        // Headline
        // ═════════════════════════════════════════════════════════════════
        Text(
            text = "Unlock Pro for\nUnlimited Scans",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // ═════════════════════════════════════════════════════════════════
        // Feature List — Emerald green checkmarks
        // ═════════════════════════════════════════════════════════════════
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),  // Perfect spacing
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "Unlimited offline scans")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "Export professional PDF reports")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "AI counter-proposal email drafting")
            FeatureBullet(icon = Icons.Filled.CheckCircle, text = "100% private & on-device")
        }

        // ═════════════════════════════════════════════════════════════════
        // Price Card
        // ═════════════════════════════════════════════════════════════════
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = proPrice ?: "$4.99/month",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cancel anytime · No commitment",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ═════════════════════════════════════════════════════════════════
        // CTA Button — Navy Blue, 12dp radius, 48dp+ touch target
        // ═════════════════════════════════════════════════════════════════
        // Business logic (Qonversion purchase flow) is UNCHANGED.
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
                .defaultMinSize(minHeight = 48.dp)   // Accessibility touch target
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),         // 12dp radius per design spec
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary  // Deep Navy Blue
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Unlock Pro",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),  // Accessible touch target
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Emerald green icon container ─────────────────────────────────
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,  // Emerald Green
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
