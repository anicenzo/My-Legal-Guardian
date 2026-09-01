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

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ui.MainViewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "scan"
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
                            selectedRoute = currentRoute,
                            onNavigate = { route ->
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
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
                        NavHost(
                            navController = navController,
                            startDestination = "scan"
                        ) {
                            composable("scan") {
                                HomeScreen(
                                    viewModel = viewModel
                                )
                            }
                            composable("vault") {
                                VaultScreen(
                                    viewModel = viewModel,
                                    onOpenDocument = {
                                        navController.navigate("scan")
                                    },
                                    onScanClick = {
                                        navController.navigate("scan")
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
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
}
