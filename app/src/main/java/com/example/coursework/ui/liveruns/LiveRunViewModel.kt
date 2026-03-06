package com.example.coursework.ui.liveruns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.locationtracker.LocationTracker
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveRunViewModel @Inject constructor(
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()
    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    // Holds the continuous path of the runner
    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints = _pathPoints.asStateFlow()

    private var locationJob: Job? = null

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    fun onLocationPermissionResult(isGranted: Boolean) {
        _hasLocationPermission.value = isGranted
        // If granted and we haven't started tracking, get initial location
        if (isGranted && locationJob == null) {
            startLocationUpdates()
        }
    }

    fun toggleTracking() {
        _isTracking.value = !_isTracking.value
    }

    private fun startLocationUpdates() {
        locationJob = viewModelScope.launch {
            locationTracker.getLocationUpdates(3000L).collect { location ->
                val latLng = LatLng(location.latitude, location.longitude)

                // Always update current location so the map can center itself
                _currentLocation.value = latLng

                // Only add to the drawn path if the user has hit "Start"
                if (_isTracking.value) {
                    _pathPoints.value += latLng
                }
            }
        }
    }
}
