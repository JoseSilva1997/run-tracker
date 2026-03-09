package com.example.coursework.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coursework.data.db.entity.RunTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunTypeDao {

    @Query("SELECT * FROM run_type ORDER BY targetDistanceMeters ASC")
    fun observeAll(): Flow<List<RunTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(runType: RunTypeEntity): Long

    @Query("DELETE FROM run_type WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM run_type WHERE id = :id")
    suspend fun getById(id: Long): RunTypeEntity

}
