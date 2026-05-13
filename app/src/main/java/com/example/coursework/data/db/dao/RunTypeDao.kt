package com.example.coursework.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coursework.data.db.entity.RunTypeEntity
import kotlinx.coroutines.flow.Flow

// Room DAO for the run_type table.
@Dao
interface RunTypeDao {

    @Query("SELECT * FROM run_type WHERE isArchived = 0 ORDER BY targetDistanceMeters ASC")
    fun observeActive(): Flow<List<RunTypeEntity>>

    @Query("SELECT * FROM run_type ORDER BY targetDistanceMeters ASC")
    fun observeAllIncludingArchived(): Flow<List<RunTypeEntity>>

    @Query("UPDATE run_type SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("SELECT COUNT(*) FROM run_sessions WHERE runTypeId = :id")
    suspend fun countRunsForType(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(runType: RunTypeEntity): Long

    @Query("DELETE FROM run_type WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM run_type WHERE id = :id")
    suspend fun getById(id: Long): RunTypeEntity

}
