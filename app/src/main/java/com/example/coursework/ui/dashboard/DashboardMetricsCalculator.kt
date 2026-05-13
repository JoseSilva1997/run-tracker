package com.example.coursework.ui.dashboard

import com.example.coursework.domain.model.DashboardMetrics
import com.example.coursework.domain.model.RunSession

private const val METERS_PER_KM = 1000f
private const val SECONDS_PER_DAY = 86_400L
private const val DAYS_PER_WEEK = 7
private const val MILLIS_PER_SECOND = 1000L
private const val PERCENT = 100f
private const val MIN_DISTANCE_METERS_FOR_PACE = 50f

/**
 * Pure computation of the dashboard metrics from a list of runs — kept out of the ViewModel so it's
 * easy to test.
 */
object DashboardMetricsCalculator {

    // Builds the dashboard's metric set. Returns zeroed/null fields when there are no runs so the
    // UI can render an empty state.
    fun calculate(
        runs: List<RunSession>,
        // Passed as a Map so each run can look up its target in O(1) inside the loop instead
        // of re-querying the DAO per run.
        targetsByRunTypeId: Map<Long, Float>,
        nowMillis: Long = System.currentTimeMillis()
    ): DashboardMetrics {
        if (runs.isEmpty()) {
            return DashboardMetrics(
                avgPaceSecPerKM = null,
                bestTimeSeconds = null,
                weeklyDistanceMeters = 0f,
                trendPct = null
            )
        }
        // Average pace and best time only count runs that actually reached their target — pace per
        // km is misleading for a run that was aborted early. Weekly distance still counts every run,
        // since incomplete distance still reflects effort.
        val completed = runs.filter { run ->
            val target = targetsByRunTypeId[run.runTypeId] ?: return@filter false
            run.totalDistanceMeters >= target
        }

        val weekMillis = DAYS_PER_WEEK * SECONDS_PER_DAY * MILLIS_PER_SECOND
        val thisWeekStart = nowMillis - weekMillis
        val prevWeekStart = nowMillis - 2 * weekMillis

        val thisWeek = runs.filter { it.timestamp >= thisWeekStart }
        val prevWeek = runs.filter { it.timestamp in prevWeekStart until thisWeekStart }

        val weeklyDistanceMeters = thisWeek.sumOf { it.totalDistanceMeters.toDouble() }.toFloat()
        val prevWeeklyDistanceMeters = prevWeek.sumOf { it.totalDistanceMeters.toDouble() }.toFloat()

        return DashboardMetrics(
            avgPaceSecPerKM = avgPace(completed),
            // minOfOrNull (not min) so an empty list yields null instead of throwing.
            // The UI then shows "no data" rather than crashing.
            bestTimeSeconds = completed.minOfOrNull { it.durationSeconds },
            weeklyDistanceMeters = weeklyDistanceMeters,
            trendPct = trend(weeklyDistanceMeters, prevWeeklyDistanceMeters)
        )
    }

    // Average pace in seconds per kilometre across the given runs. Returns null if the combined
    // distance is too small to give a meaningful figure.
    private fun avgPace(runs: List<RunSession>): Int? {
        val totalMeters = runs.sumOf { it.totalDistanceMeters.toDouble() }.toFloat()
        if(totalMeters < MIN_DISTANCE_METERS_FOR_PACE) return null
        val totalSeconds = runs.sumOf { it.durationSeconds }
        val totalKm = totalMeters / METERS_PER_KM
        return (totalSeconds / totalKm).toInt()
    }

    // Percentage change between this week's and last week's distance, or null when there's no
    // previous week to compare against.
    private fun trend(current: Float, previous: Float): Float? {
        if (previous <= 0f) return null
        return ((current - previous) / previous) * PERCENT
    }
}