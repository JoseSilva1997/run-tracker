package com.example.coursework.util.calcs

import java.util.Locale

/**
 * Small formatting helpers shared by the live run, summary, and history screens
 * so the same run is displayed the same way everywhere.
 */


private const val SECONDS_PER_MINUTE = 60
private const val METERS_PER_KM = 1000f
private const val PACE_MIN_DISTANCE_KM = 0.05f

object CommonUtils {

    // Formats a duration in seconds as HH:MM:SS. Locale.ROOT is used so the
    // digits and separators stay stable regardless of the device's locale
    fun getTimeAsString(seconds: Long) : String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    // Formats pace as minutes:seconds per kilometre. Returns "0:00" below a minimum distance because
    // pace over a handful of metres is dominated by GPS jitter and would show misleading numbers at the start of a run.
    fun getPaceAsString(durationSeconds: Long, distanceMeters: Float): String {
        val distanceKm = distanceMeters / METERS_PER_KM
        if (distanceKm <= PACE_MIN_DISTANCE_KM) return "0:00"
        val paceMinutes = (durationSeconds / SECONDS_PER_MINUTE.toFloat()) / distanceKm
        val pMins = paceMinutes.toInt()
        val pSecs = ((paceMinutes - pMins) * SECONDS_PER_MINUTE).toInt()
        return String.format(Locale.getDefault(), "%d:%02d", pMins, pSecs)
    }
}
