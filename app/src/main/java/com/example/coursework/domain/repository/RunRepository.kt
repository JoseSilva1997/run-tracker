package com.example.coursework.domain.repository

import com.example.coursework.domain.model.RunPoint
import com.example.coursework.domain.model.RunSession
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun saveRun(runSession: RunSession, points: List<RunPoint>): Long
    suspend fun getRunDetails(runId: Long): Pair<RunSession, List<RunPoint>>
    fun getAllRuns(): Flow<List<RunSession>>
}