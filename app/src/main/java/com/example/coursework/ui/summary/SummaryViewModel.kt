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

// Single UI state for the summary screen. isLoading starts true so the screen shows
// the loading text until the init block populates the fields.
data class SummaryUiState(
    val isLoading: Boolean = true,
    val runSession: RunSession? = null,
    val targetDistanceMeters: Float? = 0f,
    val runType: RunType? = null,
    val pathPoints: List<LatLng> = emptyList(),
    val error: String? = null
)

// Backs the summary screen for a single saved run.
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val runTypeRepository: RunTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Pulled from SavedStateHandle so the id survives process death (Android can drop
    // the screen and restore it later, but the navigation arg is restored too).
    private val runId: Long = checkNotNull(savedStateHandle["runId"])

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState = _uiState.asStateFlow()

    // One-shot event fired after a successful delete. Channel rather than StateFlow so
    // the screen doesn't re-trigger navigation on recomposition or after rotation.
    private val _runDeletedEvent = Channel<Unit>(Channel.BUFFERED)
    val runDeletedEvent = _runDeletedEvent.receiveAsFlow()

    // Deletes the run, then signals the screen to leave once the DB write completes.
    fun deleteRun() {
        viewModelScope.launch {
            runRepository.deleteRun(runId)
            _runDeletedEvent.send(Unit)
        }
    }

    // Loads everything the summary needs in one pass at construction time. The run is
    // already saved by the Live Run screen before navigation, so this is a plain read.
    init {
        viewModelScope.launch {
            try {
                val (session, points) = runRepository.getRunDetails(runId)
                val runType = runTypeRepository.getRunTypeById(session.runTypeId)
                val targetDistanceMeters = runType.targetDistanceMeters
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    runSession = session,
                    runType = runType,
                    targetDistanceMeters = targetDistanceMeters,
                    pathPoints = points.map { LatLng(it.latitude, it.longitude) }
                )
            } catch (e: Exception) {
                // Broad catch so a missing run or a failed lookup falls through to a
                // visible error state instead of crashing the screen.
                _uiState.value = SummaryUiState(
                    isLoading = false,
                    error = "Failed to load run summary."
                )
            }
        }
    }
}
