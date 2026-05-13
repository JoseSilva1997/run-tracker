package com.example.coursework.data.repository

import com.example.coursework.data.db.dao.RunTypeDao
import com.example.coursework.data.db.entity.RunTypeEntity
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.DeleteRunTypeResult
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.util.mappers.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of the run type repository. Wraps the DAO and maps stored entities into the domain
 * RunType model so the rest of the app never sees the database shape.
 */
class RunTypeRepositoryImpl @Inject constructor(
    private val dao: RunTypeDao
) : RunTypeRepository {

    // Inserts a new run type. Wrapped in a Result so callers can react to a failure
    // without dealing with exceptions directly.
    override suspend fun addRunType(name: String, targetDistanceMeters: Float): Result<Long> {
        return try {
            val id = dao.insert(
                RunTypeEntity(
                    name = name.trim(),
                    targetDistanceMeters = targetDistanceMeters
                )
            )
            Result.success(id)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun getRunTypeById(id: Long): RunType {
        return dao.getById(id).toDomain()
    }

    // Stream of run types the user can currently pick from — excludes archived ones so deleted types
    // don't reappear in the picker.
    override fun observeActive(): Flow<List<RunType>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    //  Stream of every run type including archived ones, so past runs can still display the name of a
    //  type that's been deleted.
    override fun observeAll(): Flow<List<RunType>> =
        dao.observeAllIncludingArchived().map { list -> list.map { it.toDomain() } }

    // Hard-deletes the run type if no runs reference it, otherwise archives it.
    // Archiving (instead of deleting) preserves the user's run history.
    // The foreign key cascade would otherwise wipe every past run of that type.
    override suspend fun deleteRunType(id: Long): Result<DeleteRunTypeResult> = try {
        val runCount = dao.countRunsForType(id)
        if (runCount == 0) {
            dao.deleteById(id)
            Result.success(DeleteRunTypeResult.HardDelete)
        } else {
            dao.archive(id)
            Result.success(DeleteRunTypeResult.Archive(runCount))
        }
    } catch (t: Throwable) {Result.failure(t)}
}
