package com.example.coursework.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.model.WeatherSnapshot
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimary
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary
import com.example.coursework.util.calcs.CommonUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

private val CARD_BG = Color(0xFF1E1E1E)
private val DELETE_COLOR = Color(0xFFCE2029)
private const val MAP_HEIGHT_DP = 240
private const val MAP_ZOOM = 15f
private const val ROUTE_LINE_WIDTH = 12f

@Composable
fun SummaryScreen(
    onDone: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.runDeletedEvent.collect { onDone() }
    }

    if (showDeleteDialog) {
        DeleteRunDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteRun()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> Text("Loading...", color = TextSecondary)
                uiState.error != null -> Text(uiState.error.orEmpty(), color = TextSecondary)
                uiState.runSession != null -> SummaryContent(
                    runSession = uiState.runSession!!,
                    runType = uiState.runType,
                    pathPoints = uiState.pathPoints
                )
            }

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BtnPrimary)
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DELETE_COLOR)
            ) {
                Text("Delete run", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ColumnScope.SummaryContent(
    runSession: RunSession,
    runType: RunType?,
    pathPoints: List<LatLng>
) {
    Text(
        text = runType?.name ?: "Run",
        color = TextPrimary,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    RouteMap(pathPoints = pathPoints)
    TrainingDataCard(runSession = runSession)
    WeatherCard(weather = runSession.weatherSnapshot)
}

@Composable
private fun RouteMap(pathPoints: List<LatLng>) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pathPoints) {
        val anchor = pathPoints.firstOrNull() ?: return@LaunchedEffect
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(anchor, MAP_ZOOM))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT_DP.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG)
    ) {
        if (pathPoints.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No route data", color = TextSecondary)
            }
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                Polyline(
                    points = pathPoints,
                    color = Color.Blue,
                    width = ROUTE_LINE_WIDTH
                )
            }
        }
    }
}

@Composable
private fun TrainingDataCard(runSession: RunSession) {
    StatsCard(title = "Training data") {
        StatRow(
            label = "Total time",
            value = CommonUtils.getTimeAsString(runSession.durationSeconds)
        )
        StatRow(
            label = "Avg pace",
            value = CommonUtils.getPaceAsString(
                runSession.durationSeconds,
                runSession.totalDistanceMeters
            ) + " /km"
        )
    }
}

@Composable
private fun WeatherCard(weather: WeatherSnapshot?) {
    StatsCard(title = "Weather") {
        if (weather == null) {
            Text("No weather data", color = TextSecondary)
            return@StatsCard
        }
        Text(
            text = weather.conditionText,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        StatRow(
            label = "Temperature",
            value = String.format(Locale.getDefault(), "%.1f °C", weather.temperatureC)
        )
        StatRow(
            label = "Wind speed",
            value = String.format(Locale.getDefault(), "%.1f km/h", weather.windSpeed)
        )
        StatRow(
            label = "Humidity",
            value = String.format(Locale.getDefault(), "%.0f%%", weather.humidity)
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun DeleteRunDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CARD_BG,
        title = {
            Text("Delete run?", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "This run will be permanently deleted. This action cannot be undone.",
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = DELETE_COLOR, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
