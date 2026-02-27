package com.example.coursework.ui.dashboard

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

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
        onAddNewRunType = {}
    )
}

class ExpandedStateProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun RunTypeDropdownPreview(
    @PreviewParameter(ExpandedStateProvider::class)
    isExpanded: Boolean
) {
    // We need to recompose the preview when the state changes,
    // so we use a mutable state that is initialized by the preview parameter.
    var expanded by remember { mutableStateOf(isExpanded) }
    var selectedOption by remember { mutableStateOf("5K") }
    val options = listOf("5K", "10K", "Half Marathon")

    // The ExposedDropdownMenuBox needs to be re-drawn when 'expanded' changes
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // Simplified version of the dropdown for preview purposes
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
internal fun MetricsGridPreview() {
    MetricsGrid()
}

@Preview
@Composable
internal fun MetricCardPreview() {
    MetricCard(title ="Avg Pace", value = "6:15  /km")
}
