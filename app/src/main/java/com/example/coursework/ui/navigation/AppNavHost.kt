package com.example.coursework.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.coursework.ui.dashboard.DashboardScreen
import com.example.coursework.ui.dashboard.DashboardViewModel
import com.example.coursework.ui.dashboard.FILTER_ALL
import com.example.coursework.ui.history.HistoryScreen
import com.example.coursework.ui.liveruns.LiveRunScreen
import com.example.coursework.ui.runtypes.AddRunTypeViewModel
import com.example.coursework.ui.summary.SummaryScreen
import com.example.coursework.ui.theme.BgDark

@Composable
fun AppNavHost(
    navController: NavHostController,
    shellPadding: PaddingValues
) {
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
                val filterOptions = listOf(FILTER_ALL) + rts.map { it.name }
                val selectedFilter by vm.dashboardFilter.collectAsState()
                val uiState by vm.uiState.collectAsState()

                DashboardScreen(
                    filterOptions = filterOptions,
                    selectedFilter = selectedFilter,
                    metrics = uiState.metrics,
                    onFilterSelected = vm::onDashboardFilterSelected,
                    onAddNewRunType = { name, distance ->
                        addRunTypeVm.addRunType(name, distance)
                    },
                    contentPadding = shellPadding
                )
            }
            composable(HISTORY_ROUTE) {
                HistoryScreen(
                    contentPadding = shellPadding,
                    onRunClick = { runId -> navController.navigate(summaryRoute(runId)) }
                )
            }
        }

        composable(
            route = LIVE_RUN_ROUTE,
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
                    navController.navigate(summaryRoute(runId)) {
                        popUpTo(LIVE_RUN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = SUMMARY_ROUTE,
            arguments = listOf(navArgument("runId") { type = NavType.LongType })
        ) {
            SummaryScreen(onDone = { navController.popBackStack() })
        }
    }
}
