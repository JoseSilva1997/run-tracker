package com.example.coursework.ui.runtypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RunTypeViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunTypeUiState())
    val uiState: StateFlow<RunTypeUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load run types."
                        )
                    }
                }
                .collect { runTypes ->
                    _uiState.update { current ->
                        current.copy(
                            runTypes = runTypes,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun addRunType(name: String, targetDistanceMeters: Int) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank() || targetDistanceMeters <= 0) {
            _uiState.update { current ->
                current.copy(errorMessage = "Enter a name and a positive distance.")
            }
            return
        }

        viewModelScope.launch {
            val result = repository.addRunType(trimmedName, targetDistanceMeters)
            if (result.isSuccess) {
                _uiState.update { current ->
                    current.copy(
                        errorMessage = null,
                        clearInputsToken = current.clearInputsToken + 1
                    )
                }
            } else {
                _uiState.update { current ->
                    current.copy(
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Failed to add run type."
                    )
                }
            }
        }
    }
}
