package com.example.coursework.util.calcs

class CommonUtils {

    fun calculateMinutesFromSeconds(seconds: Long): Long {
        return seconds / 60
    }

    fun getTimeInMinutesAsAString(seconds: Long) : String {
        val minutes = calculateMinutesFromSeconds(seconds)
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}