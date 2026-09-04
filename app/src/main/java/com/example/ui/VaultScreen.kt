package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DocumentEntity
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.lgPressClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════════════════════
// Legal AI — Vault Screen (dark-only, flat cards, no theme toggle)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    onOpenDocument: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val isBiometricEnabled by viewModel.biometricLock.collectAsState(initial = false)
    var isUnlocked by remember(isBiometricEnabled) { mutableStateOf(!isBiometricEnabled) }
    var authError by remember { mutableStateOf<String?>(null) }

    fun triggerAuth() {
        if (activity != null && com.example.util.BiometricAuthHelper.canAuthenticate(context)) {
            com.example.util.BiometricAuthHelper.promptBiometric(
                activity = activity,
                title = "Unlock Vault",
                subtitle = "Authenticate to access confidential contracts",
                onSuccess = { isUnlocked = true; authError = null },
                onError = { err -> authError = err }
            )
        } else {
            isUnlocked = true
        }
    }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && !isUnlocked) triggerAuth()
    }

    val savedDocs by viewModel.savedDocuments.collectAsState(initial = emptyList())
    val colors = LocalLGColors.current

    // ── Locked state ──────────────────────────────────────────────────────────
    if (isBiometricEnabled && !isUnlocked) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.Background)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.Accent.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, colors.Accent.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = colors.Accent,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Vault Locked", style = LGType.Title, color = colors.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Biometric protection is enabled for your confidential contract history.",
                style = LGType.Body,
                color = colors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError ?: "",
                    style = LGType.Caption,
                    color = colors.RiskHigh,   // AccentDanger → RiskHigh
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            LGButton(
                text = "Unlock Vault",
                onClick = { triggerAuth() },
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    // ── Unlocked vault ────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
    ) {
        // Flat top bar — hairline border, no shadow, no theme toggle
        Column(modifier = Modifier.fillMaxWidth().background(colors.Background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vault", style = LGType.Title.copy(color = colors.TextPrimary), modifier = Modifier.weight(1f))
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.Border))
        }

        // ── Main Content ──────────────────────────────────────────────────────
        if (savedDocs.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.Surface, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.Border),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = colors.TextTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Vault is Empty", style = LGType.Heading.copy(fontWeight = FontWeight.SemiBold), color = colors.TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Audited contracts are securely saved here offline for easy reference.",
                        style = LGType.Body,
                        color = colors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    LGButton(text = "Scan New Contract", onClick = onScanClick, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            // Document list
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "PAST AUDITS  ·  ${savedDocs.size}",
                        style = LGType.Caption.copy(fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp),
                        color = colors.TextTertiary,
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                    )
                }

                items(savedDocs, key = { it.id }) { doc ->
                    VaultDocumentCard(
                        document = doc,
                        onClick = {
                            viewModel.loadSavedDocument(doc.id)
                            onOpenDocument()
                        },
                        onDelete = { viewModel.deleteSavedDocument(doc.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VaultDocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalLGColors.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(document.dateScanned) { dateFormat.format(Date(document.dateScanned)) }

    // Flat card — border replaces elevation
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.Surface, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
            .lgPressClickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.SurfaceElevated)
                .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = colors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.title,
                style = LGType.Body.copy(fontWeight = FontWeight.Medium),
                color = colors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "$formattedDate  ·  ${document.pageCount} ${if (document.pageCount == 1) "page" else "pages"}",
                style = LGType.Caption,
                color = colors.TextTertiary
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "Delete",
                tint = colors.TextTertiary
            )
        }
    }
}
