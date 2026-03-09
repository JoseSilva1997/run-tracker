package com.example.coursework.data.repository

import com.example.coursework.data.db.dao.RunSessionDao
import com.example.coursework.domain.model.RunPoint
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.util.mappers.toDomain
import com.example.coursework.util.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunRepositoryImpl @Inject constructor(
    private val runSessionDao: RunSessionDao
) : RunRepository {

    override suspend fun saveRun(runSession: RunSession, points: List<RunPoint>): Long {
        // 1. Convert domain model to entity and insert to get the generated runId
        val sessionEntity = runSession.toEntity()
        val generatedRunId = runSessionDao.insertRunSession(sessionEntity)

        // 2. Assign the generated runId to all the points and insert them
        val pointEntities = points.map { it.toEntity(runSessionId = generatedRunId) }
        runSessionDao.insertRunPoints(pointEntities)

        return generatedRunId
    }

    override suspend fun getRunDetails(runId: Long): Pair<RunSession, List<RunPoint>> {
        val runWithPoints = runSessionDao.getRunSessionWithPoints(runId)
        val sessionDomain = runWithPoints.runSession.toDomain()
        val pointsDomain = runWithPoints.points.map { entity ->
            RunPoint(
                latitude = entity.latitude,
                longitude = entity.longitude,
                timestamp = entity.timestamp,
                accuracy = entity.accuracy
            )
        }
        return Pair(sessionDomain, pointsDomain)
    }

    override fun getAllRuns(): Flow<List<RunSession>> {
        return runSessionDao.getAllRunSessionsWithPoints().map { list ->
            list.map { it.runSession.toDomain() }
        }
    }
}
