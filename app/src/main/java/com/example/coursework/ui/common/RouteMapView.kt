package com.example.coursework.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.coursework.ui.theme.TextSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_ZOOM = 15f
private const val ROUTE_LINE_WIDTH = 12f

@Composable
fun RouteMapView(
    pathPoints: List<LatLng>,
    modifier: Modifier = Modifier,
    zoom: Float = DEFAULT_ZOOM,
    interactive: Boolean = true
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pathPoints) {
        val anchor = pathPoints.firstOrNull() ?: return@LaunchedEffect
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(anchor, zoom))
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
                    scrollGesturesEnabled = interactive,
                    zoomGesturesEnabled = interactive,
                    tiltGesturesEnabled = interactive,
                    rotationGesturesEnabled = interactive,
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
