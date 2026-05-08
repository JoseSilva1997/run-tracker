package com.example.coursework.ui.liveruns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.domain.locationtracker.LocationTracker
import com.example.coursework.domain.model.RunPoint
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.WeatherSnapshot
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOCATION_UPDATE_INTERVAL_MS = 3000L
private const val TIMER_TICK_MS = 1000L

@HiltViewModel
class LiveRunViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val runRepository: RunRepository,
    private val weatherRepository: WeatherRepository,
    private val runTypeRepository: RunTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the ID passed from the Dashboard navigation
    private val runTypeId: Long = checkNotNull(savedStateHandle["runTypeId"])

    // Core state
    private val _targetDistanceMeters = MutableStateFlow(0f)
    val targetDistanceMeters = _targetDistanceMeters.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters = _distanceMeters.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds = _elapsedTimeSeconds.asStateFlow()

    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints = _pathPoints.asStateFlow()

    // Data to save
    private val runPointsToSave = mutableListOf<RunPoint>()
    private var weatherSnapshot: WeatherSnapshot? = null
    private var hasFetchedWeather = false

    // Last point used for distance accumulation. Independent of _pathPoints
    // so the first segment after Start isn't dropped or measured against stale data.
    private var lastTrackedPoint: LatLng? = null

    // Navigation trigger
    private val _savedRunId = MutableStateFlow<Long?>(null)
    val savedRunId = _savedRunId.asStateFlow()

    private var locationJob: Job? = null
    private var timerJob: Job? = null

    // Location tracking state
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    init {
        // Fetch the target distances as soon as the ViewModel is created
        viewModelScope.launch {
            val runType = runTypeRepository.getRunTypeById(runTypeId)
            _targetDistanceMeters.value = runType?.targetDistanceMeters ?: 0f
        }
    }

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
            // Seed distance baseline with the most recent fix so the first recorded
            // segment is measured from where the user actually pressed Start.
            lastTrackedPoint = _currentLocation.value
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }

    private fun startLocationUpdates() {
        locationJob = viewModelScope.launch {
            locationTracker.getLocationUpdates(LOCATION_UPDATE_INTERVAL_MS).collect { locationPoint ->
                processLocationUpdate(locationPoint)
            }
        }
    }

    private fun processLocationUpdate(runPoint: RunPoint) {
        val latLng = LatLng(runPoint.latitude, runPoint.longitude)

        // 1. Always update current location so the map can center
        _currentLocation.value = latLng

        // 2. Only record metrics if the user has hit "Start"
        if (_isTracking.value) {
            handleInitialWeatherFetch(runPoint.latitude, runPoint.longitude)

            runPointsToSave.add(runPoint)

            updateDistanceAndCheckTarget(latLng)
            _pathPoints.value += latLng
        }
    }

    private fun handleInitialWeatherFetch(lat: Double, lng: Double) {
        if (!hasFetchedWeather) {
            hasFetchedWeather = true
            fetchWeatherAsync(lat, lng)
        }
    }

    private fun updateDistanceAndCheckTarget(newLatLng: LatLng) {
        val previous = lastTrackedPoint
        lastTrackedPoint = newLatLng

        if (previous == null) return // First fix after Start: nothing to measure yet.

        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            previous.latitude, previous.longitude,
            newLatLng.latitude, newLatLng.longitude,
            results
        )

        val newDistance = _distanceMeters.value + results[0]

        // Check if target is reached and cap the distance
        if (_targetDistanceMeters.value > 0 && newDistance >= _targetDistanceMeters.value) {
            _distanceMeters.value = _targetDistanceMeters.value
            finishAndSaveRun()
        } else {
            _distanceMeters.value = newDistance
        }
    }

    private fun fetchWeatherAsync(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Fails silently and leaves weatherSnapshot as null if offline
            weatherSnapshot = weatherRepository.getWeatherAtLocation(lat, lon)

        }
    }

    fun finishAndSaveRun() {
        // Stop tracking
        _isTracking.value = false
        locationJob?.cancel()
        timerJob?.cancel()

        // Save to Room
        viewModelScope.launch {
            val session = RunSession(
                runTypeId = runTypeId,
                durationSeconds = _elapsedTimeSeconds.value,
                totalDistanceMeters = _distanceMeters.value,
                timestamp = System.currentTimeMillis(),
                weatherSnapshot = weatherSnapshot
            )

            val newRunId = runRepository.saveRun(session, runPointsToSave)

            // Trigger navigation to summary screen
            _savedRunId.value = newRunId
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
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
