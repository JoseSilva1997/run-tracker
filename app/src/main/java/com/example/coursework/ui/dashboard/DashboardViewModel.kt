package com.example.coursework.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel for Dashboard UI state related to run type selection/filtering.
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RunTypeRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Source of truth for available run types from Room via repository.
    private val _runTypes = MutableStateFlow<List<RunType>>(emptyList())
    val runTypes: StateFlow<List<RunType>> = _runTypes

    // Selected run type name:
    // 1) restores last saved selection when it still exists,
    // 2) otherwise falls back to first available run type.
    val selectedRunType: StateFlow<String> = combine(
        runTypes,
        preferencesRepository.lastSelectedRunTypeName
    ) { types, lastSelected ->
        if (!lastSelected.isNullOrEmpty() && types.any { it.name == lastSelected }) {
            lastSelected
        } else {
            types.firstOrNull()?.name.orEmpty()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "")

    init {
        // Starts observing run types immediately so dashboard content stays current.
        viewModelScope.launch {
            repository.observeAll()
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
}
