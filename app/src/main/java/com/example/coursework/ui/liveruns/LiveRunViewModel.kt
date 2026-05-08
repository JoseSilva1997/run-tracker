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

// Per-file tuning constants.
private const val LOCATION_UPDATE_INTERVAL_MS = 3000L  // GPS poll cadence. Lower = more points, more battery.
private const val TIMER_TICK_MS = 1000L                // Elapsed-time tick rate (1 Hz).
private const val PACE_DISPLAY_MIN_KM = 0.05f          // Below this distance pace is mathematically meaningless.
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600
private const val METERS_PER_KM = 1000f
// Reject fixes worse than this many meters of horizontal accuracy. Filters
// out weak-signal noise that would otherwise inflate distance and zig-zag the path.
private const val MAX_ACCURACY_METERS = 20f
// Minimum movement between consecutive recorded points. Below this we
// treat the fix as stationary GPS jitter and skip it entirely.
private const val MIN_SEGMENT_METERS = 5f

// Single source of truth consumed by LiveRunScreen. Holds the canonical
// run state plus a few derived display strings. Co-located with the VM
// per the project's UiState convention.
data class LiveRunUiState(
    val hasLocationPermission: Boolean = false,
    val isTracking: Boolean = false,         // True between Start and End (regardless of pause).
    val isPaused: Boolean = false,           // True only while user has the run paused.
    val targetDistanceMeters: Float = 0f,    // 0 means no target; auto-finish disabled.
    val distanceMeters: Float = 0f,
    val elapsedTimeSeconds: Long = 0L,
    val currentLocation: LatLng? = null,     // Latest fix shown on the map; updated even when paused / not tracking.
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
            // Seed currentLocation from the OS's cached last-known fix
            // so the map can open already centered on the user instead
            // of starting at (0,0) and snapping over.
            seedLastKnownLocation()
            startLocationUpdates()
        }
    }

    private fun seedLastKnownLocation() {
        viewModelScope.launch {
            val cached = locationTracker.getLastKnownLocation() ?: return@launch
            // Don't overwrite a fresh fix if one beat us here.
            _uiState.update { state ->
                if (state.currentLocation != null) state
                else state.copy(currentLocation = LatLng(cached.latitude, cached.longitude))
            }
        }
    }

    // Toggles between "not started" and "tracking". Called by LiveRunScreen
    // after the START countdown finishes. End-of-run uses finishAndSaveRun
    // instead, so the false branch here is mostly defensive.
    fun toggleTracking() {
        val willTrack = !_uiState.value.isTracking
        _uiState.update { it.copy(isTracking = willTrack, isPaused = false) }
        if (willTrack) {
            // Seed distance baseline with the most recent fix so the first recorded
            // segment is measured from where the user actually pressed Start.
            lastTrackedPoint = _uiState.value.currentLocation
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }

    // Pause: stop the elapsed-time timer and ignore incoming fixes for
    // distance/path. Location updates keep flowing so currentLocation
    // stays fresh on the map.
    fun pauseTracking() {
        if (!_uiState.value.isTracking || _uiState.value.isPaused) return
        timerJob?.cancel()
        _uiState.update { it.copy(isPaused = true) }
    }

    // Resume: re-seed lastTrackedPoint to the current location so the
    // segment that spans the pause gap is NOT counted as distance, then
    // restart the timer.
    fun resumeTracking() {
        if (!_uiState.value.isPaused) return
        lastTrackedPoint = _uiState.value.currentLocation
        _uiState.update { it.copy(isPaused = false) }
        startTimer()
    }

    // Starts a long-running collection of location fixes. Job is cancelled
    // in finishAndSaveRun() and onCleared() to avoid leaking the flow.
    private fun startLocationUpdates() {
        locationJob = viewModelScope.launch {
            locationTracker.getLocationUpdates(LOCATION_UPDATE_INTERVAL_MS).collect { locationPoint ->
                processLocationUpdate(locationPoint)
            }
        }
    }

    // Core ingest path for every GPS fix. Runs two filters before recording
    // anything to avoid the inflated distance / zig-zag path that naive
    // sampling produces. Off-tracking fixes still update currentLocation
    // so the map's blue dot follows the user.
    private fun processLocationUpdate(runPoint: RunPoint) {
        val latLng = LatLng(runPoint.latitude, runPoint.longitude)

        if (_uiState.value.isTracking && !_uiState.value.isPaused) {
            // Gate 1: drop low-accuracy fixes. Still update currentLocation
            // so the map blue-dot stays roughly correct, but don't record.
            if (runPoint.accuracy > MAX_ACCURACY_METERS) {
                _uiState.update { it.copy(currentLocation = latLng) }
                return
            }

            val previous = lastTrackedPoint
            val segmentDistance = if (previous == null) 0f else distanceBetween(previous, latLng)

            // Gate 2: skip sub-threshold movement. Leave lastTrackedPoint
            // alone so the next valid fix measures from the old anchor -
            // otherwise jitter creeps forward one tiny step at a time.
            if (previous != null && segmentDistance < MIN_SEGMENT_METERS) {
                _uiState.update { it.copy(currentLocation = latLng) }
                return
            }

            handleInitialWeatherFetch(runPoint.latitude, runPoint.longitude)
            runPointsToSave.add(runPoint)
            lastTrackedPoint = latLng

            val newDistance = _uiState.value.distanceMeters + segmentDistance
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

    // Pure haversine via the Android Location helper. Returns meters.
    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude, a.longitude,
            b.latitude, b.longitude,
            results
        )
        return results[0]
    }

    // Weather is captured exactly once per run, lazily on the first
    // recorded fix. Latching with hasFetchedWeather avoids extra API hits
    // and keeps the snapshot tied to where the run actually began.
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

    // End of run: stop timers, persist the session + collected points,
    // then emit the one-shot navigation event so the screen can move on
    // to Summary. Triggered manually (END button) or automatically when
    // the configured target distance is reached.
    fun finishAndSaveRun() {
        _uiState.update { it.copy(isTracking = false, isPaused = false) }
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

    // Simple 1Hz wall-clock counter. Independent of GPS cadence so the
    // displayed time advances smoothly even when fixes are sparse.
    // Counting ticks (rather than reading System.currentTimeMillis at
    // each emit) keeps pause/resume trivial: cancel restarts the count
    // from where it left off.
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                _uiState.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
            }
        }
    }

    // Defensive cleanup. Both jobs are also cancelled in finishAndSaveRun;
    // this guards the case where the screen is destroyed mid-run (process
    // death, back navigation before End is pressed, etc.).
    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        timerJob?.cancel()
    }
}
