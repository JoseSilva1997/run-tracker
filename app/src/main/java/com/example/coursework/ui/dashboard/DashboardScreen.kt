package com.example.coursework.ui.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimary
import com.example.coursework.ui.theme.BtnPrimaryBlue
import com.example.coursework.ui.theme.TextPrimary
import com.example.coursework.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    filterOptions: List<String>,
    startRunOptions: List<String>,
    selectedRunType: String,
    onStartRunSelected: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onRunTypeSelected: (String) -> Unit,
    onAddNewRunType: () -> Unit
) {
    Scaffold (
        containerColor = BgDark,
        bottomBar = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text (
                    text = "Select run type",
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))

                RunTypeDropdown(
                    options = startRunOptions,
                    selectedOption = selectedRunType,
                    width = 0.4f,
                    onSelected = { selected ->
                        onRunTypeSelected(selected)
                    }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onStartRunSelected(selectedRunType) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnPrimary)
                ) {
                    Text("Start Run", color = TextPrimary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Dashboard",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(text = "Filter by run type", color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            var selectedFilter by remember { mutableStateOf(filterOptions.firstOrNull().orEmpty()) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RunTypeDropdown(
                options = filterOptions,
                selectedOption = selectedFilter,
                width = 0.3f,
                onSelected = {
                    selectedFilter = it
                    onFilterSelected(it)
                }
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = { onAddNewRunType() },
                modifier = Modifier
                    .size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = BtnPrimaryBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new run type"
                )
            }
        }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Metrics",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            MetricsGrid()
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunTypeDropdown(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    width: Float
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = TextSecondary.copy(alpha = 0.25f)
    val bgColor = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onPrimary



    ExposedDropdownMenuBox (
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth(width)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedTrailingIconColor = textPrimary,
                unfocusedTrailingIconColor = textPrimary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MetricsGrid() {
    Column {
        Row(Modifier.fillMaxWidth()) {
            MetricCard("Avg Pace", "--:-- /km", Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            MetricCard("Best Time", "--:--", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricCard("Weekly Distance", "-- km", Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            MetricCard("Recent Trend", "--", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    val borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)

    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

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
private fun MetricsGridPreview() {
    MetricsGrid()
}

@Preview
@Composable
private fun MetricCardPreview() {
    MetricCard(title ="Avg Pace", value = "6:15  /km")
}


