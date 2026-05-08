package com.example.coursework.util.calcs

import java.util.Locale

private const val SECONDS_PER_MINUTE = 60
private const val METERS_PER_KM = 1000f
private const val PACE_MIN_DISTANCE_KM = 0.05f

object CommonUtils {

    fun getTimeAsString(seconds: Long) : String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun getPaceAsString(durationSeconds: Long, distanceMeters: Float): String {
        val distanceKm = distanceMeters / METERS_PER_KM
        if (distanceKm <= PACE_MIN_DISTANCE_KM) return "0:00"
        val paceMinutes = (durationSeconds / SECONDS_PER_MINUTE.toFloat()) / distanceKm
        val pMins = paceMinutes.toInt()
        val pSecs = ((paceMinutes - pMins) * SECONDS_PER_MINUTE).toInt()
        return String.format(Locale.getDefault(), "%d:%02d", pMins, pSecs)
    }
}
