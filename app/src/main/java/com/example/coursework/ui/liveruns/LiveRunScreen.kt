package com.example.coursework.ui.liveruns

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimary
import com.example.coursework.ui.theme.TextPrimary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

// Per-file UI tuning constants.
private const val MAP_FOLLOW_ZOOM = 17f                // Street-level zoom while tracking the runner.
private const val MAP_CAMERA_ANIMATION_MS = 1000       // Smooth pan duration when following new fixes.
private const val COUNTDOWN_START = 3                  // Pre-run countdown begins at this number.
private const val COUNTDOWN_TICK_MS = 1000L            // One second between countdown decrements.
// Delay Google Map inflation so navigation transition can finish first.
// MapView's native/GL surface is expensive to inflate on first launch
// and freezes the main thread mid-transition without this gate.
private const val MAP_INFLATE_DELAY_MS = 200L

@Composable
fun LiveRunScreen(
    runTypeName: String,
    onClose: () -> Unit,
    onRunFinished: (Long) -> Unit,
    viewModel: LiveRunViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Pre-run countdown overlay state. Null = hidden. When non-null it
    // ticks 3 -> 2 -> 1 -> null, and the run starts on the transition to null.
    var countdown by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // TextToSpeech instance lives for the lifetime of this screen. We stash
    // the reference in state so the countdown effect can speak each tick,
    // and shut it down via DisposableEffect to avoid leaking the engine.
    val ttsReady by viewModel.tts.ready.collectAsStateWithLifecycle()

    // Countdown FSM. Each state change speaks one number (or "Go") and
    // waits one second. Re-keying on `countdown` cancels the previous
    // coroutine if state changes early (e.g. the user navigates away).
    LaunchedEffect(countdown, ttsReady) {
        val current = countdown ?: return@LaunchedEffect
        if (!ttsReady) return@LaunchedEffect
        viewModel.tts.speak(current.toString(), flush = true, utteranceId = "tick-$current")
        delay(COUNTDOWN_TICK_MS)
        if (current > 1) {
            countdown = current - 1
        } else {
            viewModel.toggleTracking()
            countdown = null
        }
    }

    // One-shot run-finished event; never replays after recomposition.
    // Collected inside a LaunchedEffect so navigation only fires once.
    LaunchedEffect(Unit) {
        viewModel.runFinishedEvent.collect { id -> onRunFinished(id) }
    }

    // Result handler for the system permission dialog. We accept either
    // fine OR coarse location: coarse still gives us a usable track, just
    // less precise, and the user may legitimately deny fine.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(isGranted)
    }

    // First composition: check existing grants. If we already have one,
    // tell the VM directly. Otherwise, prompt for both at once so the user
    // only sees a single system dialog.
    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            viewModel.onLocationPermissionResult(true)
        } else {
            // Request both permissions simultaneously
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Outer Box stacks layered overlays. Z-order back -> front:
    //   1. Column with stats card + map area
    //   2. Pause scrim (only when paused)
    //   3. RunControl buttons (always interactive on top of map/scrim)
    //   4. Countdown overlay (covers everything during pre-run countdown)
    //   5. Close icon (always reachable, top-left)
    Scaffold(
        containerColor = BgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Card containing run stats
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E) // Slightly lighter than BgDark for separation
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(all = 24.dp)
                    ) {
                        LiveTimer(timeText = state.timeString, modifier = Modifier.fillMaxWidth())

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DistanceMeter(distanceText = state.distanceString, modifier = Modifier.weight(1f))
                            PaceMeter(paceText = state.paceString, modifier = Modifier.weight(1f))
                        }
                    }
                }

                BottomContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    runTypeName = runTypeName,
                    hasLocationPermission = state.hasLocationPermission,
                    pathPoints = state.pathPoints,
                    currentLocation = state.currentLocation
                )
            }

            // Pause scrim - dims everything except control buttons
            if (state.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )
            }

            RunControl(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
                isTracking = state.isTracking,
                isPaused = state.isPaused,
                onStart = { if (countdown == null && ttsReady) countdown = COUNTDOWN_START },
                onPause = { viewModel.pauseTracking() },
                onResume = { viewModel.resumeTracking() },
                onEnd = { viewModel.finishAndSaveRun() }
            )

            // Countdown overlay
            countdown?.let { value ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        color = Color.White,
                        fontSize = 180.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Close Button "x"
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Go back",
                    tint = TextPrimary
                )
            }
        }
    }
}

// Baseline meter card composable
@Composable
internal fun MeterCard(
    measurement: String,
    style: TextStyle,
    fontWeight: FontWeight,
    measureUnit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = measurement,
            color = TextPrimary,
            style = style,
            fontWeight = fontWeight
        )
        Text(
            text = measureUnit,
            color = TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Light
        )
    }
}

// Main MeterCard displaying the total time elapsed during a run
@Composable
internal fun LiveTimer(timeText: String, modifier: Modifier = Modifier) {
    MeterCard(
        measurement = timeText,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        measureUnit = "ELAPSED TIME",
        modifier = modifier
    )
}

// MeterCard displaying the total distance ran
@Composable
internal fun DistanceMeter(distanceText: String, modifier: Modifier = Modifier) {
    MeterCard(
        measurement = distanceText,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        measureUnit = "KM",
        modifier = modifier
    )
}

// MeterCard displaying the average pace during a run
@Composable
internal  fun PaceMeter(paceText: String, modifier: Modifier = Modifier) {
    MeterCard(
        measurement = paceText,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        measureUnit = "PACE (MIN/KM)",
        modifier = modifier
    )
}

// Floating Target Badge - Creative way to display the target
@Composable
internal  fun GoalDisplay(
    runTypeName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = BtnPrimary,
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 12.dp, topEnd = 12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "GOAL:",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = runTypeName.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// Bottom Container for the map area
@Composable
internal fun BottomContainer(
    modifier: Modifier,
    runTypeName: String,
    hasLocationPermission: Boolean,
    pathPoints: List<LatLng>,
    currentLocation: LatLng?
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        MapView(
            modifier = Modifier.fillMaxSize(),
            hasLocationPermission = hasLocationPermission,
            pathPoints = pathPoints,
            currentLocation = currentLocation
        )

        GoalDisplay(
            runTypeName = runTypeName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
    }
}

@Composable
internal fun MapView(
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean,
    pathPoints: List<LatLng>,
    currentLocation: LatLng?
) {
    var inflateMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(MAP_INFLATE_DELAY_MS)
        inflateMap = true
    }

    if (!inflateMap || currentLocation == null) {
        Box(modifier = modifier.background(BgDark))
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, MAP_FOLLOW_ZOOM)
    }

    // Animate the camera to the user the first time we get a location, and
    // every time it changes pre-tracking (cached last-known -> real fix).
    // Once tracking starts, pathPoints takes over via the follow effect.
    LaunchedEffect(pathPoints.lastOrNull()) {
        pathPoints.lastOrNull()?.let { latest ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latest, MAP_FOLLOW_ZOOM),
                durationMs = MAP_CAMERA_ANIMATION_MS
            )
        }
    }

    // Re-create properties ONLY when the permission state changes
    val mapProperties by remember(hasLocationPermission) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                isTrafficEnabled = false
            )
        )
    }

    // uiSettings don't change, so remember them across all recompositions
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                tiltGesturesEnabled = false,
            )
        )
    }

    GoogleMap(
        modifier = modifier,
        properties = mapProperties,
        uiSettings = uiSettings,
        cameraPositionState = cameraPositionState
    ) {
        // Draw the route
        if (pathPoints.isNotEmpty()) {
            Polyline(points = pathPoints, color = Color.Blue, width = 12f)
        }
    }
}


// Three-state run control. Renders one of:
//   - START button     (run not yet started)
//   - PAUSE icon       (run active and not paused)
//   - END + RESUME     (run paused; user picks one)
// Caller owns the actual VM calls; this composable is purely presentational.
@Composable
internal fun RunControl(
    modifier: Modifier = Modifier,
    isTracking: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit
) {
    when {
        !isTracking -> PrimaryRunButton(
            modifier = modifier,
            text = "START",
            color = Color(0xFFCE2029),
            onClick = onStart
        )
        !isPaused -> PrimaryRunIconButton(
            modifier = modifier,
            color = Color(0xFFFFA500),
            icon = Icons.Default.Pause,
            contentDescription = "Pause run",
            onClick = onPause
        )
        else -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledIconButton(
                label = "END",
                color = Color(0xFFCE2029),
                icon = Icons.Default.Stop,
                contentDescription = "End run",
                onClick = onEnd
            )
            LabeledIconButton(
                label = "RESUME",
                color = Color(0xFF1DB954),
                icon = Icons.Default.PlayArrow,
                contentDescription = "Resume run",
                onClick = onResume
            )
        }
    }
}

// Big circular text button (START) wrapped in two translucent halo
// rings to give the button visual weight against the map. Sizes are
// hierarchical: 140dp outer halo > 116dp inner halo > 92dp button.
@Composable
private fun PrimaryRunButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer halo (faintest)
        Surface(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(100.dp),
            color = color.copy(alpha = 0.1f)
        ) {}
        // Inner halo (slightly stronger)
        Surface(
            modifier = Modifier.size(116.dp),
            shape = RoundedCornerShape(100.dp),
            color = color.copy(alpha = 0.2f)
        ) {}
        Button(
            onClick = onClick,
            modifier = Modifier.size(92.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Same halo shape as PrimaryRunButton but renders an icon instead of text.
// Used for the PAUSE state so the user sees the universal "two bars" glyph.
@Composable
private fun PrimaryRunIconButton(
    modifier: Modifier = Modifier,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(100.dp),
            color = color.copy(alpha = 0.1f)
        ) {}
        Surface(
            modifier = Modifier.size(116.dp),
            shape = RoundedCornerShape(100.dp),
            color = color.copy(alpha = 0.2f)
        ) {}
        Button(
            onClick = onClick,
            modifier = Modifier.size(92.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

// Smaller circular icon button with a white text label floating above.
// Used for the END / RESUME pair shown when paused. No halos - the
// pause-state scrim already gives these buttons enough contrast.
@Composable
private fun LabeledIconButton(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Button(
            onClick = onClick,
            modifier = Modifier.size(76.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
