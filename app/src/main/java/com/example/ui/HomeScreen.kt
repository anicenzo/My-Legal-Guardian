package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.OcrTextExtractor
import com.example.ui.home.*
import com.example.ui.primitives.LGSpacing
import com.example.ui.primitives.LGType
import com.example.ui.primitives.LocalLGColors
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val freeScansRemaining by viewModel.freeScansRemaining.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()

    // System Back button: return to Idle scan screen if currently in Result/Status/Error
    BackHandler(enabled = state !is AuditState.Idle) {
        viewModel.reset()
    }

    var showPaywall by remember { mutableStateOf(false) }

    // Camera Document Scanner launcher (Google Play services Document Scanner API)
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

    // PDF Document Picker (ML Kit offline PDF OCR)
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.attemptScan(
                onSuccess = {
                    coroutineScope.launch {
                        try {
                            val text = OcrTextExtractor.extractTextFromPdf(context, uri)
                            viewModel.processExtractedText(text, "Imported PDF Contract")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to read PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onLimitReached = {
                    showPaywall = true
                }
            )
        }
    }

    if (showPaywall) {
        PaywallBottomSheet(
            onDismiss = { showPaywall = false },
            viewModel = viewModel
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalLGColors.current.Background)
    ) {
        // Top App Bar — Dynamic Navigation with Back Button on Results Screen
        val colors = LocalLGColors.current
        val isResultState = state is AuditState.Result

        Surface(
            color = colors.HeaderBackground,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = LGSpacing.lg, vertical = LGSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isResultState) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.reset() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to scan",
                            tint = colors.TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(LGSpacing.sm))
                }

                Text(
                    text = if (isResultState) "Audit Report" else "Contract Scanner",
                    style = LGType.Title.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "About",
                    tint = colors.TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            Toast.makeText(context, "AI Contract Scanner v1.5.0", Toast.LENGTH_SHORT).show()
                        }
                )
            }
        }

        // Body — Animated screen state transitions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LGSpacing.lg)
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
                        onImportPdfClick = {
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        },
                        onPurchaseClick = { showPaywall = true }
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
                    is AuditState.Error     -> ScanErrorScreen(currentState.message, viewModel::reset)
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}