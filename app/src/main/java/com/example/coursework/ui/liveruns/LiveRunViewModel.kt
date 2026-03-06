package com.example.coursework.ui.liveruns

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LiveRunViewModel @Inject constructor() : ViewModel() {

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    fun onLocationPermissionResult(isGranted: Boolean) {
        _hasLocationPermission.value = isGranted
    }
}
