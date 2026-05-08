package com.example.coursework.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val isLoading: Boolean = true,
    val runSession: RunSession? = null,
    val runType: RunType? = null,
    val pathPoints: List<LatLng> = emptyList(),
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

    private val _runDeletedEvent = Channel<Unit>(Channel.BUFFERED)
    val runDeletedEvent = _runDeletedEvent.receiveAsFlow()

    fun deleteRun() {
        viewModelScope.launch {
            runRepository.deleteRun(runId)
            _runDeletedEvent.send(Unit)
        }
    }

    init {
        viewModelScope.launch {
            try {
                val (session, points) = runRepository.getRunDetails(runId)
                val runType = runTypeRepository.getRunTypeById(session.runTypeId)
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    runSession = session,
                    runType = runType,
                    pathPoints = points.map { LatLng(it.latitude, it.longitude) }
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
