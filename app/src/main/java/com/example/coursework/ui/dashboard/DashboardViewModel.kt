package com.example.coursework.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    private val _runTypes = MutableStateFlow<List<RunType>>(emptyList())
    val runTypes: StateFlow<List<RunType>> = _runTypes

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch { _runTypes.update { emptyList() }}
                .collect { list -> _runTypes.update { list }}
        }
    }
}