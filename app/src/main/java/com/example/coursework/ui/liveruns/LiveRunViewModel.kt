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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private const val LOCATION_UPDATE_INTERVAL_MS = 3000L
private const val TIMER_TICK_MS = 1000L
private const val PACE_DISPLAY_MIN_KM = 0.05f
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600
private const val METERS_PER_KM = 1000f

data class LiveRunUiState(
    val hasLocationPermission: Boolean = false,
    val isTracking: Boolean = false,
    val targetDistanceMeters: Float = 0f,
    val distanceMeters: Float = 0f,
    val elapsedTimeSeconds: Long = 0L,
    val currentLocation: LatLng? = null,
    val pathPoints: List<LatLng> = emptyList()
) {
    // Derived display strings. Pure functions of canonical fields; not part of
    // equals/hashCode/copy, so they don't affect Compose recomposition keys.
    val timeString: String
        get() {
            val hours = elapsedTimeSeconds / SECONDS_PER_HOUR
            val minutes = (elapsedTimeSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
            val seconds = elapsedTimeSeconds % SECONDS_PER_MINUTE
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }

    val distanceKm: Float
        get() = distanceMeters / METERS_PER_KM

    val distanceString: String
        get() = String.format(Locale.getDefault(), "%.2f", distanceKm)

    val paceString: String
        get() {
            // Below the gate the divisor is too small to give meaningful pace.
            if (distanceKm <= PACE_DISPLAY_MIN_KM) return "0:00"
            val paceMinutes = (elapsedTimeSeconds / SECONDS_PER_MINUTE.toFloat()) / distanceKm
            val pMins = paceMinutes.toInt()
            val pSecs = ((paceMinutes - pMins) * SECONDS_PER_MINUTE).toInt()
            return String.format(Locale.getDefault(), "%d:%02d", pMins, pSecs)
        }
}

@HiltViewModel
class LiveRunViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val runRepository: RunRepository,
    private val weatherRepository: WeatherRepository,
    private val runTypeRepository: RunTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val runTypeId: Long = checkNotNull(savedStateHandle["runTypeId"])

    private val _uiState = MutableStateFlow(LiveRunUiState())
    val uiState = _uiState.asStateFlow()

    // One-shot navigation event. Channel (not StateFlow) so the value isn't
    // re-delivered after process death / screen recomposition.
    private val _runFinishedEvent = Channel<Long>(Channel.BUFFERED)
    val runFinishedEvent = _runFinishedEvent.receiveAsFlow()

    // Internal-only state (not part of UI contract).
    private val runPointsToSave = mutableListOf<RunPoint>()
    private var weatherSnapshot: WeatherSnapshot? = null
    private var hasFetchedWeather = false
    private var lastTrackedPoint: LatLng? = null

    private var locationJob: Job? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val runType = runTypeRepository.getRunTypeById(runTypeId)
            _uiState.update { it.copy(targetDistanceMeters = runType?.targetDistanceMeters ?: 0f) }
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = isGranted) }
        if (isGranted && locationJob == null) {
            startLocationUpdates()
        }
    }

    fun toggleTracking() {
        val willTrack = !_uiState.value.isTracking
        _uiState.update { it.copy(isTracking = willTrack) }
        if (willTrack) {
            // Seed distance baseline with the most recent fix so the first recorded
            // segment is measured from where the user actually pressed Start.
            lastTrackedPoint = _uiState.value.currentLocation
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

        if (_uiState.value.isTracking) {
            handleInitialWeatherFetch(runPoint.latitude, runPoint.longitude)
            runPointsToSave.add(runPoint)

            val newDistance = computeNewDistance(latLng)
            val target = _uiState.value.targetDistanceMeters
            val targetReached = target > 0 && newDistance >= target
            val cappedDistance = if (targetReached) target else newDistance

            // Atomic update: location, path, and distance move in one emission.
            _uiState.update { state ->
                state.copy(
                    currentLocation = latLng,
                    pathPoints = state.pathPoints + latLng,
                    distanceMeters = cappedDistance
                )
            }

            if (targetReached) finishAndSaveRun()
        } else {
            _uiState.update { it.copy(currentLocation = latLng) }
        }
    }

    private fun computeNewDistance(newLatLng: LatLng): Float {
        val previous = lastTrackedPoint
        lastTrackedPoint = newLatLng
        if (previous == null) return _uiState.value.distanceMeters

        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            previous.latitude, previous.longitude,
            newLatLng.latitude, newLatLng.longitude,
            results
        )
        return _uiState.value.distanceMeters + results[0]
    }

    private fun handleInitialWeatherFetch(lat: Double, lng: Double) {
        if (!hasFetchedWeather) {
            hasFetchedWeather = true
            fetchWeatherAsync(lat, lng)
        }
    }

    private fun fetchWeatherAsync(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Fails silently and leaves weatherSnapshot as null if offline.
            weatherSnapshot = weatherRepository.getWeatherAtLocation(lat, lon)
        }
    }

    fun finishAndSaveRun() {
        _uiState.update { it.copy(isTracking = false) }
        locationJob?.cancel()
        timerJob?.cancel()

        viewModelScope.launch {
            val state = _uiState.value
            val session = RunSession(
                runTypeId = runTypeId,
                durationSeconds = state.elapsedTimeSeconds,
                totalDistanceMeters = state.distanceMeters,
                timestamp = System.currentTimeMillis(),
                weatherSnapshot = weatherSnapshot
            )
            val newRunId = runRepository.saveRun(session, runPointsToSave)
            _runFinishedEvent.send(newRunId)
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                _uiState.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        timerJob?.cancel()
    }
}
