package com.example.coursework.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.DashboardMetrics
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// Sentinel value used when the user wants metrics across every run type rather
// than a specific one. Kept as a constant so screen and VM agree on the spelling.
const val FILTER_ALL = "All"

// UI state for the dashboard. Kept as a single data class so the screen only has to
// observe one flow and recomposition is driven by a single value change.
data class DashboardUiState(
    val metrics: DashboardMetrics = DashboardMetrics(
        avgPaceSecPerKM = null,
        bestTimeSeconds = null,
        weeklyDistanceMeters = 0f,
        trendPct = null
    )
)

// Backs the dashboard screen and the shared start-run flow. Owns the active run-type list,
// the persisted "last selected" preference, and the metrics derived from observed runs.
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RunTypeRepository,
    private val runRepository: RunRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // Source of truth for available run types from Room via the repository.
    private val _runTypes = MutableStateFlow<List<RunType>>(emptyList())
    val runTypes: StateFlow<List<RunType>> = _runTypes

    // Read the persisted selection synchronously once at construction so the StateFlow
    // below has a sensible initial value on the very first composition. runBlocking is
    // acceptable here because DataStore reads off-disk are fast and this only happens once.
    private val initialSelected: String = runBlocking {
        preferencesRepository.lastSelectedRunTypeName.first().orEmpty()
    }

    // Currently selected run type name. Restores the last saved selection when it still
    // exists in the run-type list, otherwise falls back to the first available type so the
    // user is never stuck on a deleted/archived selection.
    val selectedRunType: StateFlow<String> = combine(
        runTypes,
        preferencesRepository.lastSelectedRunTypeName
    ) { types, lastSelected ->
        val pref = lastSelected.orEmpty().ifEmpty { initialSelected }
        when {
            types.isEmpty() -> pref
            pref.isNotEmpty() && types.any { it.name == pref } -> pref
            else -> types.firstOrNull()?.name.orEmpty()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialSelected)

    // Dashboard's own filter, separate from the start-run selection above so the user can
    // browse stats for "All" without losing the run type they were about to start.
    private val _dashboardFilter = MutableStateFlow(FILTER_ALL)
    val dashboardFilter: StateFlow<String> = _dashboardFilter

    // Metrics for the currently selected dashboard filter. Reacts to both filter changes
    // and underlying run inserts/updates so the cards stay live without manual refresh.
    val uiState: StateFlow<DashboardUiState> = combine(
        _dashboardFilter,
        runTypes
    ) { filter, types ->
        val id = if (filter == FILTER_ALL) null else types.firstOrNull { it.name == filter }?.id
        val targets = types.associate { it.id to it.targetDistanceMeters }
        id to targets
    }
        // flatMapLatest so changing filter cancels the in-flight Room query for the old
        // filter, rather than letting a stale emission race ahead of the new one.
        .flatMapLatest { (runTypeId, targets) ->
            runRepository.observeRuns(runTypeId).map { runs ->
                DashboardUiState(metrics = DashboardMetricsCalculator.calculate(runs, targets))
            }
        }
        // Fall back to an empty state if the upstream flow errors, so the screen renders
        // its empty placeholders instead of crashing.
        .catch { emit(DashboardUiState()) }
        // WhileSubscribed(5s) keeps the flow alive across config changes (rotation) but
        // releases the Room subscription shortly after the screen is gone.
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        // Start observing active run types eagerly so the start-run sheet and dashboard
        // chips are populated by the time the user first opens either of them.
        viewModelScope.launch {
            repository.observeActive()
                .catch { _runTypes.update { emptyList() }}
                .collect { list -> _runTypes.update { list }}
        }
    }

    // Persists the user's run type choice so the next app session opens on the same one.
    fun onRunTypeSelected(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveLastSelectedRunType(name)
        }
    }

    // Updates the dashboard's filter chip selection.
    fun onDashboardFilterSelected(name: String) {
        _dashboardFilter.update { name }
    }
}
