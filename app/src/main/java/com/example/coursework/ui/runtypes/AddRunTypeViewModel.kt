package com.example.coursework.ui.runtypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

// Tiny VM scoped to the Add Run Type bottom sheet. Kept separate from the dashboard VM
// so the sheet can be opened from anywhere (dashboard, picker) without dragging in the
// dashboard's broader state.
@HiltViewModel
class AddRunTypeViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    // Fire-and-forget insert. Result isn't surfaced to the UI because the sheet closes
    // immediately on save and the new type appears via the active-run-types Flow.
    fun addRunType(name: String, distance: Float) {
        viewModelScope.launch {
            repository.addRunType(name, distance)
        }
    }
}
