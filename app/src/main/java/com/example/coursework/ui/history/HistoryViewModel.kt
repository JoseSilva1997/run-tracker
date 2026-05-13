package com.example.coursework.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Flat row used by the history list. Joining run + type + points here keeps the cards
// stateless, the composable just renders whatever is on the item.
data class HistoryItem(
    val session: RunSession,
    val runTypeName: String,
    val targetDistanceMeters: Float,
    val pathPoints: List<LatLng>
)

// isLoading starts true so the screen shows the loading placeholder until the first
// emission arrives, rather than briefly flashing the empty state.
data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<HistoryItem> = emptyList()
)

// Backs the history screen. Joins runs with their run type and route points so the
// list can be rendered in one pass without each card querying the DB.
@HiltViewModel
class HistoryViewModel @Inject constructor(
    runRepository: RunRepository,
    runTypeRepository: RunTypeRepository
) : ViewModel() {

    // Stream of fully-resolved history items. Recomputes whenever a new run is saved
    // or a run type is renamed/archived so the list stays in sync without a refresh.
    val uiState: StateFlow<HistoryUiState> = combine(
        runRepository.observeAllWithPoints(),
        // observeAll (including archived) rather than observeActive, so a run whose type
        // has since been deleted still shows the type's name on its card.
        runTypeRepository.observeAll()
    ) { runs, runTypes ->
        // Build a single id lookup so the map below is O(1) per run instead of scanning
        // the type list for every row.
        val typesById = runTypes.associateBy { it.id }
        HistoryUiState(
            isLoading = false,
            items = runs.map { (session, points) ->
                val type = typesById[session.runTypeId]
                HistoryItem(
                    session = session,
                    // Fallbacks for runs whose type was hard-deleted (no rows pointing
                    // at it left). Shouldn't happen with the archive flow but keeps the
                    // UI safe if it ever does.
                    runTypeName = type?.name ?: "Run",
                    targetDistanceMeters = type?.targetDistanceMeters ?: 0f,
                    pathPoints = points.map { LatLng(it.latitude, it.longitude) }
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed(5s) keeps the flow alive across rotation but releases the
        // Room subscription shortly after the screen leaves the back stack.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )
}
