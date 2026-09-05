package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.home.*
import com.example.ui.primitives.LGButton
import com.example.ui.primitives.LGButtonVariant
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.example.ui.primitives.LGMonolineInfoIcon
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()

    BackHandler(enabled = state !is AuditState.Idle) {
        viewModel.reset()
    }

    var showPaywall by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }

    // Physical Camera Scanner launcher (Google Play Services ML Kit)
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.map { it.imageUri }?.let { uris ->
                viewModel.processScannedDocuments(uris)
            }
        }
    }

    // Modern Android Photo Picker (permissionless multi-image import)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.attemptScan(
                onSuccess = { viewModel.processScannedDocuments(uris) },
                onLimitReached = { showPaywall = true }
            )
        }
    }

    // PDF document picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.attemptScan(
                onSuccess = { viewModel.processPdfUri(uri) },
                onLimitReached = { showPaywall = true }
            )
        }
    }

    if (showPaywall) {
        PaywallBottomSheet(
            onDismiss = { showPaywall = false },
            viewModel = viewModel
        )
    }

    val colors = LocalLGColors.current
    val isResultState = state is AuditState.Result

    // ── Scaffold Architecture to Eliminate Top Bar Clipping Bug ──────────────
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.Background,
        contentColor = colors.TextPrimary,
        topBar = {
            // Top Bar with solid opaque background, statusBarsPadding, and hairline border
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.Background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isResultState) {
                        IconButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to scan",
                                tint = colors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(LGSpacing.sm))
                    }
                    Text(
                        text = if (isResultState) "Audit Report" else "Contract Scanner",
                        style = LGType.Title.copy(color = colors.TextPrimary),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        LGMonolineInfoIcon(
                            tint = colors.TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.Border)
                )
            }
        }
    ) { innerPadding ->
        // Animated screen body placed strictly inside Scaffold innerPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "ScreenTransition"
            ) { currentState ->
                when (currentState) {
                    is AuditState.Idle -> IdleScanScreen(
                        isProUser = isProUser,
                        freeScansRemaining = freeScansRemaining,
                        onScanClick = {
                            activity?.let { act ->
                                viewModel.attemptScan(
                                    onSuccess = {
                                        viewModel.getScannerClient().getStartScanIntent(act)
                                            .addOnSuccessListener { intentSender ->
                                                scannerLauncher.launch(
                                                    IntentSenderRequest.Builder(intentSender).build()
                                                )
                                            }
                                    },
                                    onLimitReached = { showPaywall = true }
                                )
                            }
                        },
                        onImportClick = { showImportSheet = true },
                        onPurchaseClick = { showPaywall = true },
                        onSampleScanClick = { sampleText, sampleTitle ->
                            viewModel.attemptScan(
                                onSuccess = {
                                    viewModel.processExtractedText(sampleText, sampleTitle)
                                },
                                onLimitReached = { showPaywall = true }
                            )
                        }
                    )
                    is AuditState.Scanning  -> ScanStatusScreen("Extracting Document Text...")
                    is AuditState.Analyzing -> ScanStatusScreen("Running AI Risk Analysis...")
                    is AuditState.Result    -> AuditResultScreen(
                        audit = currentState,
                        isProUser = isProUser,
                        freeScansRemaining = freeScansRemaining,
                        onReset = viewModel::reset,
                        onPurchaseClick = { showPaywall = true },
                        viewModel = viewModel
                    )
                    is AuditState.Error -> ScanErrorScreen(currentState.message, viewModel::reset)
                }
            }
        }
    }

    // ── Import Document Action Sheet (Choose Photos vs Choose PDF) ───────────
    if (showImportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImportSheet = false },
            containerColor = colors.SurfaceElevated,
            contentColor = colors.TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Import Contract",
                    style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                    color = colors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select photos from your gallery or choose a PDF agreement.",
                    style = LGType.Caption,
                    color = colors.TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Choose Photos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.Surface)
                        .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                        .clickable {
                            showImportSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.AccentMuted, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Choose Photos",
                            tint = colors.Accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose Photos",
                            style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select single or multi-page images of contracts",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Choose PDF
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.Surface)
                        .border(BorderStroke(1.dp, colors.Border), RoundedCornerShape(12.dp))
                        .clickable {
                            showImportSheet = false
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.AccentMuted, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Choose PDF",
                            tint = colors.Accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose PDF Document",
                            style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Import and render multi-page PDF agreements",
                            style = LGType.Caption,
                            color = colors.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Info Sheet: "What Legal AI Monitors" ─────────────────────────────────
    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            containerColor = colors.SurfaceElevated,
            contentColor = colors.TextPrimary,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "What Legal AI Monitors",
                        style = LGType.Title.copy(fontWeight = FontWeight.Bold),
                        color = colors.TextPrimary
                    )
                    IconButton(onClick = { showInfoSheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.TextSecondary)
                    }
                }
                Text(
                    text = "On-device AI rules detect hidden liabilities, predatory terms, and missing protections before you sign.",
                    style = LGType.Caption,
                    color = colors.TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                LEGAL_AI_MONITORS_LIST.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.RiskLow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.RiskLow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = LGType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = LGType.Caption,
                                color = colors.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LGButton(
                    text = "Got It",
                    onClick = { showInfoSheet = false },
                    variant = LGButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity      -> this
    is ContextWrapper -> baseContext.findActivity()
    else             -> null
}