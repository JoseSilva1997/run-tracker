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

/**
 * The navigation map of the whole app, kept in one place so the screen flow
 * is easy to follow. The navController and shell padding are passed in rather
 * than created here, since the shell around this host is what actually owns them.
 */
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
        // Dashboard and History live inside one "main" graph so that jumping
        // out to a Live Run or Summary and coming back remembers which tab the
        // user was on, along with its scroll position and filter.
        navigation(route = MAIN_GRAPH_ROUTE, startDestination = DASHBOARD_ROUTE) {
            composable(DASHBOARD_ROUTE) {
                val parentEntry = remember(it) { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }
                val vm: DashboardViewModel = hiltViewModel(parentEntry)

                // Adding a run type is a one-off action, so its ViewModel is
                // scoped only to this screen and not shared.
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

        // Live Run gets both the id and the name of the run type. The id is what
        // the screen actually works with, but passing the name too lets the title
        // show up instantly without waiting on a database lookup.
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
                    // The Live Run is popped off as we move to the Summary so
                    // that pressing Back from the Summary doesn't return the
                    // user into a run they've already finished.
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
