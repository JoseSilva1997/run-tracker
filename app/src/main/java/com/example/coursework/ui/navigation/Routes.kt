package com.example.coursework.ui.navigation

import android.net.Uri

internal const val MAIN_GRAPH_ROUTE = "main"
internal const val DASHBOARD_ROUTE = "dashboard"
internal const val HISTORY_ROUTE = "history"
internal const val LIVE_RUN_ROUTE = "liveRun/{runTypeId}/{runTypeName}"
internal const val SUMMARY_ROUTE = "summary/{runId}"

internal fun liveRunRoute(runTypeId: Long, runTypeName: String): String =
    "liveRun/$runTypeId/${Uri.encode(runTypeName)}"

internal fun summaryRoute(runId: Long): String = "summary/$runId"
