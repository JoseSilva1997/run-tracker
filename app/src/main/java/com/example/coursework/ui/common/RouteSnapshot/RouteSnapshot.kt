package com.example.coursework.ui.common.RouteSnapshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.coursework.BuildConfig
import com.example.coursework.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

private val PlaceholderColor = Color(0xFF2A2A2A)

// Shared neutral background used for both the loading and error states so the
// preview slot doesn't visually pop when the image finishes (or fails) loading.
@Composable
private fun PreviewPlaceholder(content: @Composable () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlaceholderColor),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// Small non-interactive route preview used on history cards and the run summary. Renders
// the route as a Google Static Maps image so we don't have to spin up a full interactive
// MapView for every list item.
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

    // BoxWithConstraints so we can ask the Static Maps API for an image at exactly the
    // pixel size we'll render at. Requesting a larger image wastes bandwidth, a smaller
    // one looks blurry once scaled up.
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val withPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        // Keyed on the points and the measured size so the URL is only rebuilt when
        // something that actually affects the image changes. Recompositions from
        // unrelated state don't trigger a refetch.
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
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                loading = { PreviewPlaceholder() },
                // Static map fetch failed (typically offline). Tell the user why the
                // preview is blank and that the route is still viewable.
                error = {
                    PreviewPlaceholder {
                        Text(
                            text = "Map preview unavailable — tap to view route",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            )
        }

    }


}