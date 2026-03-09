package com.example.coursework.data.repository

import com.example.coursework.data.db.dao.RunTypeDao
import com.example.coursework.data.db.entity.RunTypeEntity
import com.example.coursework.domain.model.RunType
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.util.mappers.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunTypeRepositoryImpl @Inject constructor(
    private val dao: RunTypeDao
) : RunTypeRepository {

    override fun observeAll(): Flow<List<RunType>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addRunType(name: String, targetDistanceMeters: Int): Result<Long> {
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
}