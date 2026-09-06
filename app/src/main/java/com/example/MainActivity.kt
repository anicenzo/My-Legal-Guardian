package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.engine.LegalAuditEngine
import com.example.engine.ScannerEngine
import com.example.ui.HomeScreen
import com.example.ui.MainViewModel
import com.example.ui.PaywallBottomSheet
import com.example.ui.SettingsScreen
import com.example.ui.VaultScreen
import com.example.ui.primitives.LGBottomNav
import com.example.ui.primitives.LocalLGColors
import com.example.ui.theme.MyLegalGuardianTheme
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MainViewModelFactory

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var scannerEngine: ScannerEngine
    private lateinit var legalAuditEngine: LegalAuditEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scannerEngine = ScannerEngine(this)
        legalAuditEngine = LegalAuditEngine(this)
        val preferenceManager = com.example.data.PreferenceManager(applicationContext)
        val factory = MainViewModelFactory(application, scannerEngine, legalAuditEngine, preferenceManager)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            var currentRoute by rememberSaveable { mutableStateOf("scan") }
            var showPaywall by remember { mutableStateOf(false) }

            MyLegalGuardianTheme {
                val colors = LocalLGColors.current

                if (showPaywall) {
                    PaywallBottomSheet(
                        onDismiss = { showPaywall = false },
                        viewModel = viewModel
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colors.Background,
                    contentColor = colors.TextPrimary,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        LGBottomNav(
                            selectedRoute = currentRoute,
                            onNavigate = { route -> currentRoute = route }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.Background)
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .consumeWindowInsets(innerPadding)
                    ) {
                        when (currentRoute) {
                            "scan" -> HomeScreen(viewModel = viewModel)
                            "vault" -> VaultScreen(
                                viewModel = viewModel,
                                onOpenDocument = { currentRoute = "scan" },
                                onScanClick = { currentRoute = "scan" }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onPurchaseClick = { showPaywall = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

