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

@HiltViewModel
class DeleteRunTypeViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    private val _events = Channel<DeleteRunTypeEvent>(Channel.BUFFERED)
    val events: Flow<DeleteRunTypeEvent> = _events.receiveAsFlow()

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

sealed class DeleteRunTypeEvent {
    data object Deleted : DeleteRunTypeEvent()
    data class Archived(val runCount: Int) : DeleteRunTypeEvent()
    data class Error(val message: String) : DeleteRunTypeEvent()
}
