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

class RunTypeRepositoryImpl @Inject constructor(
    private val dao: RunTypeDao
) : RunTypeRepository {

    override fun observeActive(): Flow<List<RunType>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<RunType>> =
        dao.observeAllIncludingArchived().map { list -> list.map { it.toDomain() } }

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
