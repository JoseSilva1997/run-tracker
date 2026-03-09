package com.example.coursework.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coursework.ui.dashboard.DashboardScreen
import com.example.coursework.ui.dashboard.DashboardViewModel
import com.example.coursework.ui.runtypes.AddRunTypeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursework.ui.liveruns.LiveRunScreen
import com.example.coursework.ui.summary.SummaryScreen
import com.example.coursework.ui.theme.BgDark
import androidx.navigation.NavType
import androidx.navigation.navArgument


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = "dashboard",
        modifier = Modifier
            .background(BgDark)
            .padding(top = 16.dp)
            .safeDrawingPadding()
    ) {
        composable("dashboard") {
            val vm: DashboardViewModel = hiltViewModel()
            val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
            val runTypes by vm.runTypes.collectAsState()
            val selectedRunType by vm.selectedRunType.collectAsState()

            val filterOptions = listOf("All") + runTypes.map { it.name }

            DashboardScreen(
                filterOptions = filterOptions,
                startRunOptions = runTypes,
                selectedRunTypeName = selectedRunType,
                onStartRunSelected = { runType ->
                    val encodedName = Uri.encode(runType.name)
                    navController.navigate("liveRun/${runType.id}/$encodedName")
                },
                onFilterSelected = {},// Placeholder until filtering is implemented
                onRunTypeSelected = { vm.onRunTypeSelected(it) },
                onAddNewRunType = { name, distance -> 
                    addRunTypeVm.addRunType(name, distance.toFloat())
                }
            )
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
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

    }
}
