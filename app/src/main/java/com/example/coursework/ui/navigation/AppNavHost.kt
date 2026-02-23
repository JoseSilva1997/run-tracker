package com.example.coursework.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coursework.ui.dashboard.DashboardScreen
import com.example.coursework.ui.dashboard.DashboardViewModel
import androidx.compose.runtime.getValue


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "dashboard") {
        composable("dashboard") {
            val vm: DashboardViewModel = hiltViewModel()
            val runTypes by vm.runTypes.collectAsState()

            val filterOptions = listOf("All") + runTypes.map {it.name}
            val startRunOptions = runTypes.map { it.name } + "Add new"

            DashboardScreen(
                filterOptions = filterOptions,
                startRunOptions = startRunOptions,
                onStartRunSelected = {},
                onFilterSelected = {},
                onAddNewRunType = {}
            )
        }
    }
}