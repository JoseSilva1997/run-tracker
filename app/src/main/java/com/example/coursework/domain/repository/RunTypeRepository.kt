package com.example.coursework.domain.repository

import com.example.coursework.domain.model.RunType
import kotlinx.coroutines.flow.Flow

interface RunTypeRepository {
    fun observeAll(): Flow<List<RunType>>
    suspend fun addRunType(name: String, targetDistanceMeters: Float): Result<Long>
    suspend fun getRunTypeById(id: Long): RunType
}