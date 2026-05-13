package com.example.coursework.ui.common.RouteSnapshot

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.net.URLEncoder

private const val STATIC_MAP_BASE =  "https://maps.googleapis.com/maps/api/staticmap"
// Asks the server for a higher-resolution image so the snapshot stays sharp on dense (retina-class) screens.
private const val MAP_SCALE = 2
private const val PATH_WEIGHT = 4   // line thickness in static map px
private const val PATH_COLOR = "0x0000ff" // ARGB hex, blue opaque
private const val MAX_URL_LENGTH = 16384 // Google hard limit
private const val MAX_DIMENSION_PX = 640 // free tier cap

/**
 * Builds a Google Static Maps URL for the given route. Returns null if the route is empty or can't
 * be made to fit Google's URL length cap.
 */
fun buildStaticMapUrl (
    pathPoints: List<LatLng>,
    widthPx: Int,
    heightPx: Int,
    apiKey: String
): String? {
    if (pathPoints.isEmpty()) return null

    val w = widthPx.coerceIn(1, MAX_DIMENSION_PX)
    val h = heightPx.coerceIn(1, MAX_DIMENSION_PX)
    // Polyline encoding compresses the lat/lng pairs into a short URL-safe string, far shorter than
    // spelling out lat,lng|lat,lng|.... This is what keeps the URL under the length limit for runs
    // with hundreds of points.
    val encoded = PolyUtil.encode(pathPoints)

    val pathParam = "color:$PATH_COLOR|weight:$PATH_WEIGHT|enc:$encoded"
    val url = buildString {
        append(STATIC_MAP_BASE)
        append("?size=").append(w).append("x").append(h)
        append("&scale=").append(MAP_SCALE)
        // URL-encoded after the parameter is built because the | separators (and the : inside color: /
        // weight: / enc:) have to be escaped once they sit inside a query string.
        append("&path=").append(URLEncoder.encode(pathParam, "utf-8"))
        append("&key=").append(apiKey)
    }

    if (url.length <= MAX_URL_LENGTH) return url

    // Fallback: downsample and retry.
    // In practice, the encoded polyline is so compact it's unlikely to fire this branch.
    var stride = 2
    while (stride < pathPoints.size) {
        val sampled =
            pathPoints.filterIndexed { i, _ -> i % stride == 0 || i == pathPoints.lastIndex }
        val sEnc = PolyUtil.encode(sampled)
        val sParam = "color:$PATH_COLOR|weight:$PATH_WEIGHT|enc:$sEnc"
        val sUrl = "$STATIC_MAP_BASE?size=${w}x${h}&scale=$MAP_SCALE&path=" +
                URLEncoder.encode(sParam, "UTF-8") + "&key=$apiKey"
        if (sUrl.length <= MAX_URL_LENGTH) return sUrl
        stride *= 2
    }
    return null
}