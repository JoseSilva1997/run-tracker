package com.example.coursework.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.BuildConfig
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

data class HistoryItem(
    val session: RunSession,
    val runTypeName: String,
    val pathPoints: List<LatLng>
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<HistoryItem> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    runRepository: RunRepository,
    runTypeRepository: RunTypeRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        runRepository.observeAllWithPoints(),
        runTypeRepository.observeAll()
    ) { runs, runTypes ->
        val typeNamesById = runTypes.associate { it.id to it.name }
        HistoryUiState(
            isLoading = false,
            items = runs.map { (session, points) ->
                HistoryItem(
                    session = session,
                    runTypeName = typeNamesById[session.runTypeId] ?: "Run",
                    pathPoints = points.map { LatLng(it.latitude, it.longitude) }
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )
}
