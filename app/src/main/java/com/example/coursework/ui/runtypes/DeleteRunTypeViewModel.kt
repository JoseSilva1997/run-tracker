package com.example.coursework.ui.runtypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.repository.DeleteRunTypeResult
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Handles the delete-run-type action. Owned at the shell level so the resulting toast
// still fires even if the picker that triggered the delete has already been dismissed.
@HiltViewModel
class DeleteRunTypeViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    // One-shot events use a Channel rather than a StateFlow so a toast isn't replayed
    // on every recomposition or after a config change.
    private val _events = Channel<DeleteRunTypeEvent>(Channel.BUFFERED)
    val events: Flow<DeleteRunTypeEvent> = _events.receiveAsFlow()

    // Branches on the repository's sealed result so the UI can distinguish a true hard
    // delete from an archive (where the type still exists but is hidden from pickers).
    fun deleteRunType(id: Long) {
        viewModelScope.launch {
            repository.deleteRunType(id).fold(
                onSuccess = { result ->
                    val event = when (result) {
                        is DeleteRunTypeResult.HardDelete -> DeleteRunTypeEvent.Deleted
                        is DeleteRunTypeResult.Archive -> DeleteRunTypeEvent.Archived(result.runCount)
                    }
                    _events.send(event)
                },
                onFailure = { t ->
                    _events.send(DeleteRunTypeEvent.Error(t.message ?: "Delete failed"))
                }
            )
        }
    }
}

// Sealed so the shell collector can exhaustively map each outcome to a toast message
// without a default branch silently swallowing a new case later.
sealed class DeleteRunTypeEvent {
    data object Deleted : DeleteRunTypeEvent()
    data class Archived(val runCount: Int) : DeleteRunTypeEvent()
    data class Error(val message: String) : DeleteRunTypeEvent()
}
