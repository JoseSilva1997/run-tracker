package com.example.coursework.ui.dashboard

import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun DashboardScreenPreview() {
    val filterOptions = listOf("All", "5K", "10K")
    val startRunOptions = listOf("5K", "10K")
    DashboardScreen(
        filterOptions = filterOptions,
        startRunOptions = startRunOptions,
        selectedRunType = "5K",
        onStartRunSelected = {},
        onFilterSelected = {},
        onRunTypeSelected = {},
        onAddNewRunType = { _, _ -> }
    )
}

@Preview
@Composable
internal fun MetricsGridPreview() {
    MetricsGrid()
}

@Preview
@Composable
internal fun MetricCardPreview() {
    MetricCard(title ="Avg Pace", value = "6:15  /km", icon = androidx.compose.material.icons.Icons.Default.Speed)
}
