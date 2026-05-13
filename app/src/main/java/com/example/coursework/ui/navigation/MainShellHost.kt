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

// Wraps the navigation host with the persistent chrome (bottom bar, Start Run button)
// and the dialogs and sheets that need to outlive individual screens, like the run
// type picker and the delete confirmation.
@Composable
fun MainShellHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTabRoute = currentRoute == DASHBOARD_ROUTE || currentRoute == HISTORY_ROUTE

    // Wrapped in runCatching because on the very first composition the main graph hasn't
    // been built yet, and asking for its entry would crash. Returning null until it
    // exists is fine, the shell just falls back to empty values for one frame.
    val parentEntry: NavBackStackEntry? = remember(backStackEntry) {
        runCatching { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }.getOrNull()
    }
    // Deliberately the same instance the Dashboard screen uses. That way the run type the user
    // picks in the bottom sheet stays in sync with the Dashboard's filter and metrics.
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
    // The toast is collected here, at the shell level, so the confirmation message still shows up
    // even if the user closes the picker before the delete finishes.
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
        // The bottom bar and Start Run button only appear on Dashboard and History.
        // Live Run and Summary hide them so those screens feel more focused.
        showChrome = isTabRoute,
        currentRoute = currentRoute,
        selectedRunTypeName = selectedRunType,
        canStart = runTypes.isNotEmpty() && selectedRunType.isNotBlank(),
        onTabSelected = { tab ->
            if (currentRoute == tab.route) return@MainShell
            navController.navigate(tab.route) {
                // saveState and restoreState keep each tab's scroll position and filters intact when
                // the user switches between them, and popUpTo stops tab switches from piling up on the back stack.
                popUpTo(MAIN_GRAPH_ROUTE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSelectRunType = { showPicker = true },
        // The shell only knows the name of the selected run type, so we look up the full object here
        // to grab its id for the Live Run route.
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

    // Two-step on purpose: the picker closes first and sets this flag, then the add sheet opens on the
    // next composition. This stops the two bottom sheets from overlapping during their open/close animations.
    if (pickerRequestAddRunType) {
        val addRunTypeVm: AddRunTypeViewModel = hiltViewModel()
        AddRunTypeBottomSheet(
            onSave = { name, distance -> addRunTypeVm.addRunType(name, distance) },
            onDismiss = { pickerRequestAddRunType = false }
        )
    }
}
