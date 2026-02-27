package com.example.coursework.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursework.ui.runtypes.AddRunTypeBottomSheet
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
    onAddNewRunType: (String, Int) -> Unit
) {
    var showAddRunTypeSheet by remember { mutableStateOf(false) }
    var showRunTypePicker by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(filterOptions.firstOrNull().orEmpty()) }

    LaunchedEffect(filterOptions) {
        if (selectedFilter !in filterOptions) {
            selectedFilter = filterOptions.firstOrNull().orEmpty()
            onFilterSelected(selectedFilter)
        }
    }

    Scaffold (
        containerColor = BgDark,
        bottomBar = {
            StartRunDock(
                selectedRunType = selectedRunType,
                hasRunTypes = startRunOptions.isNotEmpty(),
                onSelectRunType = { showRunTypePicker = true },
                onStartRun = { onStartRunSelected(selectedRunType) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = "Dashboard",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    FilterChipsRow(
                        options = filterOptions,
                        selectedFilter = selectedFilter,
                        onSelected = {
                            selectedFilter = it
                            onFilterSelected(it)
                        }
                    )
                }

                FilledIconButton(
                    onClick = { showAddRunTypeSheet = true },
                    modifier = Modifier
                        .size(46.dp),
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

            MetricsGrid()
            
            // Add some extra padding at the bottom so content isn't hidden behind the bottom bar
            Spacer(Modifier.height(32.dp))
        }
        if (showAddRunTypeSheet) {
            AddRunTypeBottomSheet(
                onSave = { name, distance ->
                    onAddNewRunType(name, distance)
                },
                onDismiss = {
                    showAddRunTypeSheet = false
                },
            )
        }
        if (showRunTypePicker) {
            RunTypePickerBottomSheet(
                options = startRunOptions,
                selectedRunType = selectedRunType,
                onRunTypeSelected = {
                    onRunTypeSelected(it)
                    showRunTypePicker = false
                },
                onAddRunType = {
                    showRunTypePicker = false
                    showAddRunTypeSheet = true
                },
                onDismiss = { showRunTypePicker = false }
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    options: List<String>,
    selectedFilter: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {
        items(options) { option ->
            FilterChip(
                selected = selectedFilter == option,
                onClick = { onSelected(option) },
                label = { Text(option) },
                leadingIcon = if (selectedFilter == option) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunTypePickerBottomSheet(
    options: List<String>,
    selectedRunType: String,
    onRunTypeSelected: (String) -> Unit,
    onAddRunType: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Choose run type",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            if (options.isEmpty()) {
                Text(
                    text = "Create a run type to start tracking.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            } else {
                options.forEach { option ->
                    ListItem(
                        headlineContent = { Text(option) },
                        leadingContent = {
                            RadioButton(
                                selected = option == selectedRunType,
                                onClick = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) { onRunTypeSelected(option) }
                    )
                }
            }

            OutlinedButton(
                onClick = onAddRunType,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add new run type")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StartRunDock(
    selectedRunType: String,
    hasRunTypes: Boolean,
    onSelectRunType: () -> Unit,
    onStartRun: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Start your next run",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onSelectRunType,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = hasRunTypes
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedRunType.ifBlank { "Choose" },
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }

                    Button(
                        onClick = onStartRun,
                        enabled = hasRunTypes && selectedRunType.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BtnPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start run",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Start Run", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun MetricsGrid() {
    Column {
        Row(Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Avg Pace", 
                value = "--:-- /km", 
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            MetricCard(
                title = "Best Time", 
                value = "--:--", 
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Weekly Distance", 
                value = "-- km", 
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            MetricCard(
                title = "Recent Trend", 
                value = "--", 
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

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
