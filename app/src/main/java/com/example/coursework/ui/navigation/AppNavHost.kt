package com.example.coursework.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coursework.ui.theme.BgDark


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = "dashboard",
        modifier = Modifier
            .background(BgDark)
            .padding(vertical = 16.dp)
            .safeDrawingPadding()
    ) {
        composable("dashboard") {
            val vm: DashboardViewModel = hiltViewModel()
            val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
            val runTypes by vm.runTypes.collectAsState()
            val selectedRunType by vm.selectedRunType.collectAsState()

            val filterOptions = listOf("All") + runTypes.map { it.name }
            val startRunOptions = runTypes.map { it.name }

            DashboardScreen(
                filterOptions = filterOptions,
                startRunOptions = startRunOptions,
                selectedRunType = selectedRunType,
                onStartRunSelected = {},
                onFilterSelected = {},
                onRunTypeSelected = { vm.onRunTypeSelected(it) },
                onAddNewRunType = { name, distance -> 
                    addRunTypeVm.addRunType(name, distance)
                }
            )
        }
    }
}