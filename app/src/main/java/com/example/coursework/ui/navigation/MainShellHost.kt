package com.example.coursework.ui.navigation

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coursework.domain.model.RunType
import com.example.coursework.ui.dashboard.DashboardViewModel
import com.example.coursework.ui.runtypes.AddRunTypeBottomSheet
import com.example.coursework.ui.runtypes.AddRunTypeViewModel
import com.example.coursework.ui.runtypes.DeleteRunTypeEvent
import com.example.coursework.ui.runtypes.DeleteRunTypeViewModel
import com.example.coursework.ui.runtypes.RunTypePickerBottomSheet

@Composable
fun MainShellHost() {
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
    var pendingDelete by remember { mutableStateOf<RunType?>(null) }

    val deleteRunTypeVm: DeleteRunTypeViewModel = hiltViewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        deleteRunTypeVm.events.collect { event ->
            val message = when (event) {
                is DeleteRunTypeEvent.Deleted -> "Run type deleted"
                is DeleteRunTypeEvent.Archived -> "Run type deleted."
                is DeleteRunTypeEvent.Error -> event.message
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
                navController.navigate(liveRunRoute(rt.id, rt.name))
            }
        }
    ) { shellPadding ->
        AppNavHost(navController = navController, shellPadding = shellPadding)
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
            onDeleteRequest = { pendingDelete = it },
            onDismiss = { showPicker = false }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${target.name}?") },
            text = { Text("Delete this run type? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteRunTypeVm.deleteRunType(target.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Forward "add run type" request from picker into the dashboard's add sheet flow.
    if (pickerRequestAddRunType) {
        val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
        AddRunTypeBottomSheet(
            onSave = { name, distance -> addRunTypeVm.addRunType(name, distance) },
            onDismiss = { pickerRequestAddRunType = false }
        )
    }
}
