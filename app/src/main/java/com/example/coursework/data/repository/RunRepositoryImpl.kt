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

// Implementation of the run repository. Wraps the DAO and maps stored entities into
// the domain model so the rest of the app never sees the database shape.
class RunRepositoryImpl @Inject constructor(
    private val runSessionDao: RunSessionDao
) : RunRepository {

    // Inserts the session first to get the auto-generated id, then writes the points
    // with that id stamped in. Not wrapped in a single transaction because a partial
    // save (session with no points) is recoverable, the run just shows no route.
    override suspend fun saveRun(runSession: RunSession, points: List<RunPoint>): Long {
        val sessionEntity = runSession.toEntity()
        val generatedRunId = runSessionDao.insertRunSession(sessionEntity)

        val pointEntities = points.map { it.toEntity(runSessionId = generatedRunId) }
        runSessionDao.insertRunPoints(pointEntities)

        return generatedRunId
    }

    // One-shot read used by the summary screen, which only needs the run once on load.
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

    // Removes the run row. The cascade on the foreign key wipes its points too, so we
    // don't have to delete them separately.
    override suspend fun deleteRun(runId: Long) {
        runSessionDao.deleteRunSession(runId)
    }

    // Stream of all run sessions without their points. Used where the route isn't
    // needed (e.g. dashboard metrics), so the heavy point lists aren't loaded.
    override fun getAllRuns(): Flow<List<RunSession>> {
        return runSessionDao.getAllRunSessionsWithPoints().map { list ->
            list.map { it.runSession.toDomain() }
        }
    }

    // Stream of every run with its points. Used by the history screen so each card can
    // render a route preview without making its own DB call per row.
    override fun observeAllWithPoints(): Flow<List<Pair<RunSession, List<RunPoint>>>> {
        return runSessionDao.getAllRunSessionsWithPoints().map { list ->
            list.map { rwp ->
                val session = rwp.runSession.toDomain()
                val points = rwp.points.map { entity ->
                    RunPoint(
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        timestamp = entity.timestamp,
                        accuracy = entity.accuracy
                    )
                }
                session to points
            }
        }
    }

    // Stream of runs optionally filtered by run type. A null id means "all run types",
    // matched by the (:runTypeId IS NULL OR ...) guard in the DAO query.
    override fun observeRuns(runTypeId: Long?): Flow<List<RunSession>> {
        return runSessionDao.observeRuns(runTypeId).map { list ->
            list.map { it.toDomain() }
        }
    }
}
