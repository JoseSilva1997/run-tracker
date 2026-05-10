package com.example.coursework.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.coursework.ui.common.RouteSnapshot.RouteSnapshot
import com.example.coursework.ui.theme.TextSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_ZOOM = 15f
private const val ROUTE_LINE_WIDTH = 12f
private const val BOUNDS_PADDING_PX = 100;

@Composable
fun RouteMapView(
    pathPoints: List<LatLng>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier.clickable(enabled = pathPoints.isNotEmpty()) {expanded = true}
    ) {
        RouteSnapshot(
            pathPoints = pathPoints,
            modifier = Modifier.fillMaxSize()
        )
    }
    if (expanded) {
        FullScreenRouteMap(
            pathPoints = pathPoints,
            onDismiss = { expanded = false }
        )
    }

}

@Composable
private fun FullScreenRouteMap(
    pathPoints: List<LatLng>,
    onDismiss: () -> Unit
)
{
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            InteractiveRouteMap(
                pathPoints = pathPoints,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun InteractiveRouteMap(
    pathPoints: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pathPoints) {
        if (pathPoints.isEmpty()) return@LaunchedEffect
        if (pathPoints.size == 1) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(pathPoints.first(), DEFAULT_ZOOM))
            return@LaunchedEffect
        }
        val bounds = LatLngBounds.builder().apply {
            pathPoints.forEach { include(it) }
        }.build()
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING_PX)
        )
    }

    Box(modifier) {
        if (pathPoints.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No route data", color = TextSecondary)
            }
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false
                )
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