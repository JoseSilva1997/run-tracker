package com.example.coursework.domain.repository

import com.example.coursework.domain.model.RunType
import kotlinx.coroutines.flow.Flow

interface RunTypeRepository {

    fun observeActive(): Flow<List<RunType>>
    fun observeAll(): Flow<List<RunType>>
    suspend fun addRunType(name: String, targetDistanceMeters: Float): Result<Long>
    suspend fun getRunTypeById(id: Long): RunType

    suspend fun deleteRunType(id: Long): Result<DeleteRunTypeResult>
}

sealed class DeleteRunTypeResult {
    data object HardDelete : DeleteRunTypeResult()
    data class Archive(val runCount: Int) : DeleteRunTypeResult()
}