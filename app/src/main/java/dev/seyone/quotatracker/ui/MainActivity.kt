package dev.seyone.quotatracker.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.data.backup.DataBackupManager
import dev.seyone.quotatracker.ui.dashboard.DashboardScreen
import dev.seyone.quotatracker.ui.dashboard.QuotaDashboardViewModel
import dev.seyone.quotatracker.ui.history.HistoryScreen
import dev.seyone.quotatracker.ui.history.HistoryViewModel
import dev.seyone.quotatracker.ui.onboarding.OnboardingScreen
import dev.seyone.quotatracker.ui.onboarding.OnboardingViewModel
import dev.seyone.quotatracker.ui.settings.AboutSettingsScreen
import dev.seyone.quotatracker.ui.settings.AdjustBaselineScreen
import dev.seyone.quotatracker.ui.settings.DataStorageSettingsScreen
import dev.seyone.quotatracker.ui.settings.SettingsScreen
import dev.seyone.quotatracker.ui.settings.SettingsViewModel
import dev.seyone.quotatracker.ui.theme.QuotaTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: QuotaDashboardViewModel by viewModels {
        QuotaDashboardViewModel.Factory(
            (application as QuotaApplication).repository,
            (application as QuotaApplication).settingsRepository
        )
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(
            (application as QuotaApplication).database,
            (application as QuotaApplication).repository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            (application as QuotaApplication).settingsRepository,
            (application as QuotaApplication).repository
        )
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModel.Factory(
            (application as QuotaApplication).settingsRepository,
            (application as QuotaApplication).repository
        )
    }

    private val backupManager by lazy {
        DataBackupManager(this, (application as QuotaApplication).database)
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsRepository = (application as QuotaApplication).settingsRepository
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "SYSTEM")
            val hasCompletedOnboarding by settingsRepository.hasCompletedOnboarding.collectAsState(initial = true)

            QuotaTrackerTheme(themeMode = themeMode) {
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                val subPageRoutes = setOf("onboarding", "adjust_baseline", "data_storage", "about")
                val adaptiveInfo = currentWindowAdaptiveInfo()
                val windowSizeClass = adaptiveInfo.windowSizeClass
                val isLandscape = windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT
                val isMediumWidth = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
                val isExpandedWidth = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

                val navSuiteType = when {
                    currentRoute in subPageRoutes -> NavigationSuiteType.None
                    isLandscape -> NavigationSuiteType.NavigationRail
                    isMediumWidth -> NavigationSuiteType.NavigationRail
                    isExpandedWidth -> NavigationSuiteType.NavigationDrawer
                    else -> NavigationSuiteType.NavigationBar
                }

                val exportJsonLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val result = backupManager.exportBackup(it)
                            if (result.isSuccess) {
                                Toast.makeText(this@MainActivity, "JSON backup saved successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "JSON export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val importJsonLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val result = backupManager.importBackup(it)
                            if (result.isSuccess) {
                                Toast.makeText(this@MainActivity, "JSON backup restored!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "JSON restore failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val exportCsvLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val result = backupManager.exportCsv(it)
                            if (result.isSuccess) {
                                Toast.makeText(this@MainActivity, "CSV exported successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "CSV export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val exportCsvAnalyticsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val result = backupManager.generateCsvAnalyticsReport(it)
                            if (result.isSuccess) {
                                Toast.makeText(this@MainActivity, "CSV Analytics exported!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "CSV Analytics export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                NavigationSuiteScaffold(
                    layoutType = navSuiteType,
                    navigationSuiteItems = {
                        item(
                            selected = currentRoute == "dashboard",
                            onClick = {
                                if (currentRoute != "dashboard") {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        item(
                            selected = currentRoute == "history",
                            onClick = {
                                if (currentRoute != "history") {
                                    navController.navigate("history") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Outlined.History, contentDescription = "History") },
                            label = { Text("History") }
                        )
                        item(
                            selected = currentRoute == "settings",
                            onClick = {
                                if (currentRoute != "settings") {
                                    navController.navigate("settings") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Outlined.MoreHoriz, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (!hasCompletedOnboarding) "onboarding" else "dashboard"
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    viewModel = onboardingViewModel,
                                    onOnboardingFinished = {
                                        navController.navigate("dashboard") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("dashboard") {
                                DashboardScreen(viewModel = dashboardViewModel)
                            }
                            composable("history") {
                                HistoryScreen(viewModel = historyViewModel, topBarActions = {})
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onAdjustBaselineClick = { navController.navigate("adjust_baseline") },
                                    onDataStorageClick = { navController.navigate("data_storage") },
                                    onAboutClick = { navController.navigate("about") },
                                    onForceWearSyncClick = {
                                        (application as QuotaApplication).wearSyncBroadcaster.startSync()
                                        Toast.makeText(this@MainActivity, "Wear OS state sync triggered!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            composable(
                                route = "adjust_baseline",
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                            ) {
                                AdjustBaselineScreen(
                                    viewModel = settingsViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "data_storage",
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                            ) {
                                DataStorageSettingsScreen(
                                    onExportJsonClick = { exportJsonLauncher.launch("quota_tracker_backup.json") },
                                    onImportJsonClick = { importJsonLauncher.launch(arrayOf("application/json")) },
                                    onExportCsvClick = { exportCsvLauncher.launch("quota_tracker_logs.csv") },
                                    onExportCsvAnalyticsClick = { exportCsvAnalyticsLauncher.launch("quota_tracker_analytics.csv") },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "about",
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                            ) {
                                AboutSettingsScreen(
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
