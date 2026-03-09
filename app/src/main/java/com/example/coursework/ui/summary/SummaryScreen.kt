package com.example.coursework.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coursework.domain.model.RunSession
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryScreen(
    onDone: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Run Summary",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            when {
                uiState.isLoading -> {
                    Text("Loading...", color = TextSecondary)
                }

                uiState.error != null -> {
                    Text(uiState.error.orEmpty(), color = TextSecondary)
                }

                uiState.runSession != null -> {
                    SummaryContent(runSession = uiState.runSession!!)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun SummaryContent(runSession: RunSession) {
    val weather = runSession.weatherSnapshot
    val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(runSession.timestamp))

    val statLines = listOf(
        "Run ID: ${runSession.id}",
        "Run Type ID: ${runSession.runTypeId}",
        "Duration Seconds: ${runSession.durationSeconds}",
        "Distance Meters: ${String.format(Locale.getDefault(), "%.2f", runSession.totalDistanceMeters)}",
        "Timestamp: $dateText",
        "Temperature C: ${weather?.temperatureC?.toString() ?: "N/A"}",
        "Condition: ${weather?.conditionText ?: "N/A"}",
        "Wind Speed: ${weather?.windSpeed?.toString() ?: "N/A"}",
        "Humidity: ${weather?.humidity?.toString() ?: "N/A"}"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        statLines.forEach { line ->
            Text(
                text = line,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
