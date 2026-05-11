package com.example.coursework.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.coursework.data.db.entity.RunPointEntity
import com.example.coursework.data.db.entity.RunSessionEntity
import com.example.coursework.data.db.entity.RunSessionWithPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface RunSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunSession(runSession: RunSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunPoints(runPoints: List<RunPointEntity>)

    @Transaction
    @Query("SELECT * FROM run_sessions WHERE runId = :runId")
    suspend fun getRunSessionWithPoints(runId: Long): RunSessionWithPoints

    @Transaction
    @Query("SELECT * FROM run_sessions ORDER BY timestamp DESC")
    fun getAllRunSessionsWithPoints(): Flow<List<RunSessionWithPoints>>

    @Query("DELETE FROM run_sessions WHERE runId = :runId")
    suspend fun deleteRunSession(runId: Long)

    @Query("SELECT * FROM run_sessions WHERE (:runTypeId IS NULL OR runTypeId = :runTypeId) ORDER BY timestamp DESC")
    fun observeRuns(runTypeId: Long?): Flow<List<RunSessionEntity>>
}
