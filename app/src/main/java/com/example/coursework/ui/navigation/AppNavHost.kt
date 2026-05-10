package com.example.coursework.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.coursework.ui.dashboard.DashboardScreen
import com.example.coursework.ui.dashboard.DashboardViewModel
import com.example.coursework.ui.history.HistoryScreen
import com.example.coursework.ui.liveruns.LiveRunScreen
import com.example.coursework.ui.runtypes.AddRunTypeViewModel
import com.example.coursework.ui.runtypes.RunTypePickerBottomSheet
import com.example.coursework.ui.summary.SummaryScreen
import com.example.coursework.ui.theme.BgDark

private const val MAIN_GRAPH_ROUTE = "main"
private const val DASHBOARD_ROUTE = "dashboard"
private const val HISTORY_ROUTE = "history"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTabRoute = currentRoute == DASHBOARD_ROUTE || currentRoute == HISTORY_ROUTE

    // Resolve once; main graph entry persists across summary/liveRun thanks to saveState.
    val parentEntry: NavBackStackEntry? = remember(backStackEntry) {
        runCatching { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }.getOrNull()
    }
    val sharedVm: DashboardViewModel? = parentEntry?.let { hiltViewModel(it) }

    val runTypes by (sharedVm?.runTypes?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) })
    val selectedRunType by (sharedVm?.selectedRunType?.collectAsState()
        ?: remember { mutableStateOf("") })

    var showPicker by remember { mutableStateOf(false) }
    var pickerRequestAddRunType by remember { mutableStateOf(false) }

    MainShell(
        showChrome = isTabRoute,
        currentRoute = currentRoute,
        selectedRunTypeName = selectedRunType,
        canStart = runTypes.isNotEmpty() && selectedRunType.isNotBlank(),
        onTabSelected = { tab ->
            if (currentRoute == tab.route) return@MainShell
            navController.navigate(tab.route) {
                popUpTo(MAIN_GRAPH_ROUTE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSelectRunType = { showPicker = true },
        onStartRun = {
            runTypes.firstOrNull { it.name == selectedRunType }?.let { rt ->
                navController.navigate("liveRun/${rt.id}/${Uri.encode(rt.name)}")
            }
        }
    ) { shellPadding ->
        NavHost(
            navController = navController,
            startDestination = MAIN_GRAPH_ROUTE,
            modifier = Modifier
                .background(BgDark)
                .padding(top = 16.dp)
                .safeDrawingPadding()
        ) {
            navigation(route = MAIN_GRAPH_ROUTE, startDestination = DASHBOARD_ROUTE) {
                composable(DASHBOARD_ROUTE) {
                    val parentEntry = remember(it) { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }
                    val vm: DashboardViewModel = hiltViewModel(parentEntry)
                    val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
                    val rts by vm.runTypes.collectAsState()
                    val filterOptions = listOf("All") + rts.map { it.name }

                    DashboardScreen(
                        filterOptions = filterOptions,
                        onFilterSelected = {},
                        onAddNewRunType = { name, distance ->
                            addRunTypeVm.addRunType(name, distance)
                        },
                        contentPadding = shellPadding
                    )
                }
                composable(HISTORY_ROUTE) {
                    HistoryScreen(
                        contentPadding = shellPadding,
                        onRunClick = { runId -> navController.navigate("summary/$runId") }
                    )
                }
            }

            composable(
                route = "liveRun/{runTypeId}/{runTypeName}",
                arguments = listOf(
                    navArgument("runTypeId") { type = NavType.LongType },
                    navArgument("runTypeName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val runTypeName = backStackEntry.arguments?.getString("runTypeName") ?: ""
                LiveRunScreen(
                    runTypeName = runTypeName,
                    onClose = { navController.popBackStack() },
                    onRunFinished = { runId ->
                        navController.navigate("summary/$runId")
                    }
                )
            }

            composable(
                route = "summary/{runId}",
                arguments = listOf(
                    navArgument("runId") { type = NavType.LongType }
                )
            ) {
                SummaryScreen(
                    onDone = {
                        navController.navigate(DASHBOARD_ROUTE) {
                            popUpTo(MAIN_GRAPH_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    if (showPicker && sharedVm != null) {
        RunTypePickerBottomSheet(
            options = runTypes,
            selectedRunTypeName = selectedRunType,
            onRunTypeSelected = {
                sharedVm.onRunTypeSelected(it)
                showPicker = false
            },
            onAddRunType = {
                showPicker = false
                pickerRequestAddRunType = true
            },
            onDismiss = { showPicker = false }
        )
    }

    // Forward "add run type" request from picker into the dashboard's add sheet flow.
    if (pickerRequestAddRunType) {
        val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
        com.example.coursework.ui.runtypes.AddRunTypeBottomSheet(
            onSave = { name, distance ->
                addRunTypeVm.addRunType(name, distance)
            },
            onDismiss = { pickerRequestAddRunType = false }
        )
    }
}
