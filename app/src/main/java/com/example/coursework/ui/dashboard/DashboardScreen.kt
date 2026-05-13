package com.example.coursework.ui.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursework.domain.model.DashboardMetrics
import com.example.coursework.ui.runtypes.AddRunTypeBottomSheet
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimaryBlue
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary
import com.example.coursework.util.calcs.CommonUtils
import java.util.Locale


private const val SECONDS_PER_MINUTE = 60
private const val METERS_PER_KM = 1000f
private const val EMPTY_VALUE = "--"

// Stateless dashboard screen. State and callbacks are hoisted to the parent so this
// composable stays easy to preview and isolate in tests.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    filterOptions: List<String>,
    selectedFilter: String,
    metrics: DashboardMetrics,
    onFilterSelected: (String) -> Unit,
    onAddNewRunType: (String, Float) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showAddRunTypeSheet by remember { mutableStateOf(false) }

    // Snap the selection back to a valid option if the currently selected run type
    // gets archived or deleted while the screen is open, otherwise the chip row
    // would show no selection and metrics would freeze on a missing filter.
    LaunchedEffect(filterOptions, selectedFilter) {
        if (selectedFilter !in filterOptions) {
            filterOptions.firstOrNull()?.let(onFilterSelected)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = "Dashboard",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        // Run type chips + add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChipsRow(
                options = filterOptions,
                selectedFilter = selectedFilter,
                onSelected = onFilterSelected,
                modifier = Modifier.weight(1f)
            )

            FilledIconButton(
                onClick = { showAddRunTypeSheet = true },
                modifier = Modifier.size(46.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = BtnPrimaryBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new run type",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Your Progress",
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Metrics
        MetricsGrid(metrics = metrics)

        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
    }

    if (showAddRunTypeSheet) {
        AddRunTypeBottomSheet(
            onSave = { name, distance -> onAddNewRunType(name, distance) },
            onDismiss = { showAddRunTypeSheet = false }
        )
    }
}

// Horizontally scrollable row of run-type filter chips. Uses a LazyRow rather than a
// Row so the list can grow past the screen width once the user adds more run types.
@Composable
private fun FilterChipsRow(
    options: List<String>,
    selectedFilter: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            // Offscreen compositing plus a DstIn gradient fades the trailing edge of the
            // row out, so scrolling chips appear to slide behind the add button instead
            // of being abruptly clipped.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Black,
                        0.85f to Color.Black,
                        1f to Color.Transparent
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
    ) {
        items(options) { option ->
            FilterChip(
                selected = selectedFilter == option,
                onClick = { onSelected(option) },
                label = { Text(option) },
                leadingIcon = if (selectedFilter == option) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BtnPrimaryBlue.copy(alpha = 0.18f),
                    selectedLabelColor = TextPrimary,
                    selectedLeadingIconColor = BtnPrimaryBlue,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    labelColor = TextSecondary
                )
            )
        }
    }
}

// 2x2 grid of metric cards. Pulled out as its own composable so the dashboard's main
// layout reads top-to-bottom without nested Row/Column noise.
@Composable
internal fun MetricsGrid(metrics: DashboardMetrics) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Avg Pace",
                value = formatPace(metrics.avgPaceSecPerKM),
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            MetricCard(
                title = "Best Time",
                value = metrics.bestTimeSeconds?.let { CommonUtils.getTimeAsString(it) } ?: EMPTY_VALUE,
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Weekly Distance",
                value = formatKm(metrics.weeklyDistanceMeters),
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            MetricCard(
                title = "Weekly Trend",
                value = formatTrend(metrics.trendPct),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Single metric tile: tinted icon, label, value. Shared shape so the four cards in the
// grid stay visually consistent without each call site repeating the styling.
@Composable
internal fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BtnPrimaryBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BtnPrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = value,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Formats pace as "M:SS /km". Falls back to the empty placeholder when there isn't
// enough data, so a fresh user doesn't see a misleading "0:00 /km".
private fun formatPace(secPerKm: Int?): String {
    if (secPerKm == null || secPerKm <= 0) return EMPTY_VALUE
    val mins = secPerKm / SECONDS_PER_MINUTE
    val secs = secPerKm % SECONDS_PER_MINUTE
    return String.format(Locale.getDefault(), "%d:%02d /km", mins, secs)
}

// Formats a metres value as a kilometre string with two decimal places.
private fun formatKm(meters: Float): String {
    if (meters <= 0f) return "0.00 km"
    return String.format(Locale.getDefault(), "%.2f km", meters / METERS_PER_KM)
}

// Formats a percentage trend with an explicit sign. The leading "+" is added by hand
// because String.format only emits one for negative values.
private fun formatTrend(pct: Float?): String {
    if (pct == null) return EMPTY_VALUE
    val sign = if (pct >= 0f) "+" else ""
    return String.format(Locale.getDefault(), "%s%.0f%%", sign, pct)
}
