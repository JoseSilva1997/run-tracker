package com.example.coursework.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val isLoading: Boolean = true,
    val runSession: RunSession? = null,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val runId: Long = checkNotNull(savedStateHandle["runId"])

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { runRepository.getRunDetails(runId).first }
                .onSuccess { session ->
                    _uiState.value = SummaryUiState(
                        isLoading = false,
                        runSession = session
                    )
                }
                .onFailure {
                    _uiState.value = SummaryUiState(
                        isLoading = false,
                        error = "Failed to load run summary."
                    )
                }
        }
    }
}
