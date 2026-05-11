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
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


const val FILTER_ALL = "All"

data class DashboardUiState(
    val metrics: DashboardMetrics = DashboardMetrics(
        avgPaceSecPerKM = null,
        bestTimeSeconds = null,
        weeklyDistanceMeters = 0f,
        trendPct = null
    )
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RunTypeRepository,
    private val runRepository: RunRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // Source of truth for available run types from Room via repository.
    private val _runTypes = MutableStateFlow<List<RunType>>(emptyList())
    val runTypes: StateFlow<List<RunType>> = _runTypes

    private val initialSelected: String = runBlocking {
        preferencesRepository.lastSelectedRunTypeName.first().orEmpty()
    }

    // Selected run type name:
    // 1) restores last saved selection when it still exists,
    // 2) otherwise falls back to first available run type.
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

    private val _dashboardFilter = MutableStateFlow(FILTER_ALL)
    val dashboardFilter: StateFlow<String> = _dashboardFilter

    val uiState: StateFlow<DashboardUiState> = combine(
        _dashboardFilter,
        runTypes
    ) { filter, types ->
        val id = if (filter == FILTER_ALL) null else types.firstOrNull { it.name == filter }?.id
        val targets = types.associate { it.id to it.targetDistanceMeters }
        id to targets
    }
        .flatMapLatest { (runTypeId, targets) ->
            runRepository.observeRuns(runTypeId).map { runs ->
                DashboardUiState(metrics = DashboardMetricsCalculator.calculate(runs, targets))
            }
        } //  cancels prior query on filter changes
        .catch { emit(DashboardUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState()) // keeps flow alive across config changes but releases when screen gone.

    init {
        // Starts observing run types immediately so dashboard content stays current.
        viewModelScope.launch {
            repository.observeActive()
                .catch { _runTypes.update { emptyList() }}
                .collect { list -> _runTypes.update { list }}
        }
    }

    // Persists the user's run type choice for future app sessions.
    fun onRunTypeSelected(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveLastSelectedRunType(name)
        }
    }

    fun onDashboardFilterSelected(name: String) {
        _dashboardFilter.update { name }
    }
}
