package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
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

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var scannerEngine: ScannerEngine
    private lateinit var legalAuditEngine: LegalAuditEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        scannerEngine = ScannerEngine(this)
        legalAuditEngine = LegalAuditEngine(this)
        val preferenceManager = com.example.data.PreferenceManager(applicationContext)
        viewModel = MainViewModel(application, scannerEngine, legalAuditEngine, preferenceManager)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            var selectedRoute by remember { mutableStateOf("scan") }
            var showPaywall by remember { mutableStateOf(false) }

            MyLegalGuardianTheme(darkTheme = isDarkMode) {
                val colors = LocalLGColors.current

                if (showPaywall) {
                    PaywallBottomSheet(
                        onDismiss = { showPaywall = false },
                        viewModel = viewModel
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        LGBottomNav(
                            selectedRoute = selectedRoute,
                            onNavigate = { route ->
                                selectedRoute = route
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.Background)
                            .padding(innerPadding)
                    ) {
                        when (selectedRoute) {
                            "vault" -> VaultScreen(
                                viewModel = viewModel,
                                onOpenDocument = { selectedRoute = "scan" },
                                onScanClick = { selectedRoute = "scan" }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onPurchaseClick = { showPaywall = true }
                            )
                            else -> HomeScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
