package com.example.coursework.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Small status badges shown on run cards and the summary screen. Only the "Done!" badge
// exists today, but kept as its own file so future statuses (e.g. partial, personal best)
// can be added alongside without inflating the call sites.

private val DoneGreen = Color(0xFF2ECC71)

// Green pill shown next to a run that reached its target distance. Visual shorthand
// so the user can scan history at a glance without reading the numbers.
@Composable
fun DoneBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(DoneGreen, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Done!",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}