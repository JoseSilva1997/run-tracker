package com.example.coursework.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary
import com.example.coursework.util.calcs.CommonUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CARD_BG = Color(0xFF1E1E1E)
private const val CARD_HEIGHT_DP = 140

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    onRunClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "History",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", color = TextSecondary)
                }
            }
            uiState.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Past runs will appear here.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.session.id }) { item ->
                        RunHistoryCard(item = item, onClick = { onRunClick(item.session.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RunHistoryCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT_DP.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDate(item.session.timestamp),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.runTypeName,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = CommonUtils.getTimeAsString(item.session.durationSeconds),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CommonUtils.getPaceAsString(
                            item.session.durationSeconds,
                            item.session.totalDistanceMeters
                        ) + " /km",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            ) {

            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
}
