package com.example.coursework.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import com.example.coursework.BuildConfig
import com.example.coursework.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

@Composable
fun RouteSnapshot(
    pathPoints: List<LatLng>,
    modifier: Modifier = Modifier
) {
    if(pathPoints.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("No route data", color = TextSecondary)
        }
        return
    }

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val withPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val url = remember(pathPoints, withPx, heightPx) {
            buildStaticMapUrl(
                pathPoints = pathPoints,
                widthPx = withPx.toInt(),
                heightPx = heightPx.toInt(),
                apiKey = BuildConfig.MAPS_STATIC_API_KEY
            )
        }
        if (url == null) {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text("No route data", color = TextSecondary)
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                placeholder = ColorPainter(Color(0xFF2A2A2A)),
                error = ColorPainter(Color(0xFF2A2A2A))
            )
        }

    }


}