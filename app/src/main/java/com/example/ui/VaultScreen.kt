package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DocumentEntity
import com.example.ui.primitives.LGColorsDark
import com.example.ui.primitives.LGColorsLight
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.lgPressClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                onSuccess = {
                    isUnlocked = true
                    authError = null
                },
                onError = { err ->
                    authError = err
                }
            )
        } else {
            isUnlocked = true
        }
    }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && !isUnlocked) {
            triggerAuth()
        }
    }

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val savedDocs by viewModel.savedDocuments.collectAsState(initial = emptyList())
    val colors = LocalLGColors.current
    val isDark = colors == LGColorsDark

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
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.PrimaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vault Locked",
                style = LGType.Headline,
                color = colors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Biometric protection is enabled for your confidential contract history.",
                style = LGType.BodySmall,
                color = colors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError ?: "",
                    style = LGType.Caption,
                    color = colors.AccentDanger,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = { triggerAuth() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = colors.PrimaryAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock Vault", style = LGType.Button, color = Color.White)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.Background)
    ) {
        // ── Top App Bar ──────────────────────────────────────────────────────
        Surface(
            color = colors.HeaderBackground,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = com.example.ui.primitives.LGSpacing.lg, vertical = com.example.ui.primitives.LGSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderSpecial,
                    contentDescription = "Vault",
                    tint = colors.HeaderContent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(com.example.ui.primitives.LGSpacing.sm))
                Column {
                    Text(
                        text = "Encrypted Vault",
                        style = LGType.Title.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.HeaderContent
                        )
                    )
                    Text(
                        text = "100% On-Device Local History",
                        style = LGType.Caption.copy(
                            color = colors.HeaderContent.copy(alpha = 0.8f)
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.HeaderContent.copy(alpha = 0.12f))
                        .clickable { viewModel.toggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Theme Toggle",
                        tint = colors.HeaderContent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Main Content ─────────────────────────────────────────────────────
        if (savedDocs.isEmpty()) {
            // Empty Vault State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = colors.Surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.PrimaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Vault is Empty",
                            style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Audited contracts are securely saved here offline for easy reference and renegotiation.",
                            style = LGType.BodySmall,
                            color = colors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onScanClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = colors.PrimaryAccent,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan New Contract",
                                style = LGType.Title.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Document List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "PAST AUDITS (${savedDocs.size})",
                        style = LGType.Label.copy(
                            color = colors.TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                items(savedDocs, key = { it.id }) { doc ->
                    VaultDocumentCard(
                        document = doc,
                        onClick = {
                            viewModel.loadSavedDocument(doc.id)
                            onOpenDocument()
                        },
                        onDelete = {
                            viewModel.deleteSavedDocument(doc.id)
                        }
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
    val formattedDate = remember(document.dateScanned) {
        dateFormat.format(Date(document.dateScanned))
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .lgPressClickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.Surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.Border.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = colors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = LGType.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = LGType.Caption,
                        color = colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = LGType.Caption,
                        color = colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${document.pageCount} ${if (document.pageCount == 1) "page" else "pages"}",
                        style = LGType.Caption,
                        color = colors.TextSecondary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Document",
                    tint = colors.TextSecondary
                )
            }
        }
    }
}
