package com.example.coursework.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val isLoading: Boolean = true,
    val runSession: RunSession? = null,
    val runType: RunType? = null,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val runTypeRepository: RunTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val runId: Long = checkNotNull(savedStateHandle["runId"])

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val session = runRepository.getRunDetails(runId).first
                val runType = runTypeRepository.getRunTypeById(session.runTypeId)
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    runSession = session,
                    runType = runType
                )
            } catch (e: Exception) {
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    error = "Failed to load run summary."
                )
            }
        }
    }
}
