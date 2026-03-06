package com.example.coursework.ui.liveruns

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursework.ui.theme.BgDark
import com.example.coursework.ui.theme.BtnPrimary
import com.example.coursework.ui.theme.TextPrimary


@Composable
fun LiveRunScreen(
    runTypeName: String,
    onClose: () -> Unit,
) {

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
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                    ) {
                        LiveTimer(modifier = Modifier.fillMaxWidth()) // Live Timer

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DistanceMeter(modifier = Modifier.weight(1f))
                            PaceMeter(modifier = Modifier.weight(1f))
                        }
                    }
                }

                BottomContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    runTypeName = runTypeName
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


@Composable
internal fun LiveTimer(modifier: Modifier = Modifier) {
    MeterCard(
        measurement = "00:00:00",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        measureUnit = "ELAPSED TIME",
        modifier = modifier
    )
}

@Composable
internal fun DistanceMeter(modifier: Modifier = Modifier) {
    MeterCard(
        measurement = "0.00",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        measureUnit = "KM",
        modifier = modifier
    )
}

@Composable
internal  fun PaceMeter(modifier: Modifier = Modifier) {
    MeterCard(
        measurement = "0:00",
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

@Composable
internal fun BottomContainer(modifier: Modifier, runTypeName: String) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Map as background
        MapView(
            modifier = Modifier.fillMaxSize()
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
                .padding(bottom = 50.dp)
        )
    }
}

@Composable
internal fun MapView(modifier: Modifier = Modifier) {
    // Placeholder for the Map view
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Map view will be integrated here",
            color = TextPrimary.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Composable
internal fun StartButton(modifier: Modifier = Modifier) {
    val startButtonColor = Color(0xFFCE2029)
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
            color = startButtonColor.copy(alpha = 0.1f)
        ) {}

        // Middle fade layer
        Surface(
            modifier = Modifier.size(116.dp),
            shape = RoundedCornerShape(100.dp),
            color = startButtonColor.copy(alpha = 0.2f)
        ) {}

        // Main Button (Center)
        Button(
            onClick = { },
            modifier = Modifier.size(92.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = startButtonColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "START",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
