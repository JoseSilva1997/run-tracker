package com.example.coursework.ui.common.RouteSnapshot

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.net.URLEncoder

private const val STATIC_MAP_BASE =  "https://maps.googleapis.com/maps/api/staticmap"
private const val MAP_SCALE = 2     // retina; 1 px asset = 2 device px
private const val PATH_WEIGHT = 4   // line thickness in static map px
private const val PATH_COLOR = "0x0000ff" // ARGB hex, blue opaque
private const val MAX_URL_LENGTH = 16384 // Google hard limit
private const val MAX_DIMENSION_PX = 640 // free tier cap

fun buildStaticMapUrl (
    pathPoints: List<LatLng>,
    widthPx: Int,
    heightPx: Int,
    apiKey: String
): String? {
    if (pathPoints.isEmpty()) return null

    val w = widthPx.coerceIn(1, MAX_DIMENSION_PX)
    val h = heightPx.coerceIn(1, MAX_DIMENSION_PX)
    val encoded = PolyUtil.encode(pathPoints)

    val pathParam = "color:$PATH_COLOR|weight:$PATH_WEIGHT|enc:$encoded"
    val url = buildString {
        append(STATIC_MAP_BASE)
        append("?size=").append(w).append("x").append(h)
        append("&scale=").append(MAP_SCALE)
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