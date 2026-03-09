package com.example.coursework.ui.liveruns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.locationtracker.LocationTracker
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveRunViewModel @Inject constructor(
    private val locationTracker: LocationTracker // <-- FIX: Inject the Interface
) : ViewModel() {

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    // Map specific states
    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints = _pathPoints.asStateFlow()
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    // Run metrics states (Required for coursework)
    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters = _distanceMeters.asStateFlow()
    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds = _elapsedTimeSeconds.asStateFlow()

    private var locationJob: Job? = null
    private var timerJob: Job? = null

    fun onLocationPermissionResult(isGranted: Boolean) {
        _hasLocationPermission.value = isGranted
        if (isGranted && locationJob == null) {
            startLocationUpdates()
        }
    }

    fun toggleTracking() {
        val willTrack = !_isTracking.value
        _isTracking.value = willTrack

        if (willTrack) {
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }

    private fun startLocationUpdates() {
        locationJob = viewModelScope.launch {
            // Using 3000ms (3 seconds) interval
            locationTracker.getLocationUpdates(3000L).collect { locationPoint ->
                val latLng = LatLng(locationPoint.latitude, locationPoint.longitude)
                _currentLocation.value = latLng

                if (_isTracking.value) {
                    val currentPoints = _pathPoints.value

                    // Calculate distance if we have a previous point
                    if (currentPoints.isNotEmpty()) {
                        val lastPoint = currentPoints.last()
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            lastPoint.latitude, lastPoint.longitude,
                            latLng.latitude, latLng.longitude,
                            results
                        )
                        _distanceMeters.update { it + results[0] }
                    }

                    _pathPoints.value = currentPoints + latLng
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _elapsedTimeSeconds.update { it + 1 }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        timerJob?.cancel()
    }
}
