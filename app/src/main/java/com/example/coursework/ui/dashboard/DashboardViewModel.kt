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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RunTypeRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _runTypes = MutableStateFlow<List<RunType>>(emptyList())
    val runTypes: StateFlow<List<RunType>> = _runTypes

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
        viewModelScope.launch {
            repository.observeAll()
                .catch { _runTypes.update { emptyList() }}
                .collect { list -> _runTypes.update { list }}
        }
    }

    fun onRunTypeSelected(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveLastSelectedRunType(name)
        }
    }
}