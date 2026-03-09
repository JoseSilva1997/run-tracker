package com.example.coursework.ui.runtypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.repository.RunTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AddRunTypeViewModel @Inject constructor(
    private val repository: RunTypeRepository
) : ViewModel() {

    fun addRunType(name: String, distance: Float) {
        viewModelScope.launch {
            repository.addRunType(name, distance)
        }
    }
}
