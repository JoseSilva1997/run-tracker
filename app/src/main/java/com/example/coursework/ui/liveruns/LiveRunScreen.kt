package com.example.coursework.ui.liveruns

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimary
import com.example.coursework.ui.theme.TextPrimary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale


@Composable
fun LiveRunScreen(
    runTypeName: String,
    onClose: () -> Unit,
    onRunFinished: (Long) -> Unit,
    viewModel: LiveRunViewModel = hiltViewModel()
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val pathPoints by viewModel.pathPoints.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTimeSeconds.collectAsStateWithLifecycle()
    val distance by viewModel.distanceMeters.collectAsStateWithLifecycle()
    val savedRunId by viewModel.savedRunId.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Get run id when finished
    LaunchedEffect(savedRunId) {
        savedRunId?.let { id ->
            onRunFinished(id)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if either fine or coarse location was granted
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(isGranted)
    }

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

    // FORMATTING METRICS
    // 1. Time (always HH:MM:SS for a stable timer layout)
    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60
    val timeString = String.format(
        Locale.getDefault(),
        "%02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )

    // 2. Distance (Kilometers)
    val distanceKm = distance / 1000f
    val distanceString = String.format(Locale.getDefault(), "%.2f", distanceKm)

    // 3. Pace (Minutes per Kilometer)
    val paceString = if (distanceKm > 0.05f) { // Prevent wild pace numbers in the first 50 meters
        val paceMinutes = (elapsedTime / 60f) / distanceKm
        val pMins = paceMinutes.toInt()
        val pSecs = ((paceMinutes - pMins) * 60).toInt()
        String.format(Locale.getDefault(), "%d:%02d", pMins, pSecs)
    } else {
        "0:00"
    }

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
                        LiveTimer(timeText = timeString, modifier = Modifier.fillMaxWidth()) // Live Timer

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DistanceMeter(distanceText = distanceString, modifier = Modifier.weight(1f))
                            PaceMeter(paceText = paceString, modifier = Modifier.weight(1f))
                        }
                    }
                }

                BottomContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    runTypeName = runTypeName,
                    hasLocationPermission = hasLocationPermission,
                    isTracking = isTracking,
                    pathPoints = pathPoints,
                    onToggleTracking =  {
                        if (isTracking) viewModel.finishAndSaveRun() else viewModel.toggleTracking() },
                    currentLocation = currentLocation
                )
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

// Bottom Container for the map and start button
@Composable
internal fun BottomContainer(
    modifier: Modifier,
    runTypeName: String,
    hasLocationPermission: Boolean,
    isTracking: Boolean,
    pathPoints: List<LatLng>,
    onToggleTracking: () -> Unit,
    currentLocation: LatLng?
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Map as background
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

        StartButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            isTracking = isTracking,
            onToggleTracking = onToggleTracking
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
    val cameraPositionState = rememberCameraPositionState()

    // 1. Initial Camera Setup:
    // Animate to the user's location as soon as we get the first coordinate
    LaunchedEffect(currentLocation) {
        if (currentLocation != null && pathPoints.isEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation, 17f),
                durationMs = 1000
            )
        }
    }

    // 2. Continuous Tracking Camera Setup:
    // Follow the runner while tracking is active
    LaunchedEffect(pathPoints.lastOrNull()) {
        pathPoints.lastOrNull()?.let { latestLocation ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latestLocation, 17f),
                durationMs = 1000
            )
        }
    }

    // Re-create properties ONLY when the permission state changes
    val mapProperties by remember (hasLocationPermission) {
        mutableStateOf(
            MapProperties(isMyLocationEnabled = hasLocationPermission)
        )
    }

    // uiSettings don't change, so remember them across all recompositions
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = true,
                zoomControlsEnabled = false // Hides + and - buttons for a cleaner UI
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
            Polyline(
                points = pathPoints,
                color = Color.Blue,
                width = 12f
            )
        }
    }
}


@Composable
internal fun StartButton(
    modifier: Modifier = Modifier,
    isTracking: Boolean,
    onToggleTracking: () -> Unit
) {
    val startButtonColor = Color(0xFFCE2029)
    val stopButtonColor = Color(0xFFFFA500) // Orange for STOP
    
    val activeColor = if (isTracking) stopButtonColor else startButtonColor
    
    // Use explicit sizes for the layers to make the fade effect easy to control.
    // The sizes are hierarchical: outer layer > middle layer > main button.
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer fade layer (Largest)
        Surface(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(100.dp),
            color = activeColor.copy(alpha = 0.1f)
        ) {}

        // Middle fade layer
        Surface(
            modifier = Modifier.size(116.dp),
            shape = RoundedCornerShape(100.dp),
            color = activeColor.copy(alpha = 0.2f)
        ) {}

        // Main Button (Center)
        Button(
            onClick = onToggleTracking,
            modifier = Modifier.size(92.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (isTracking) "STOP" else "START",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
