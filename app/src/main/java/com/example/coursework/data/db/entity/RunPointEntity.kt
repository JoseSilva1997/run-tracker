package com.example.coursework.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// One GPS sample belonging to a run. Cascade-deleted when its parent run is removed.
@Entity(
    tableName = "run_points",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["runId"],
            childColumns = ["runSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("runSessionId")]
)
data class RunPointEntity(
    @PrimaryKey(autoGenerate = true) val pointId: Long = 0,
    val runSessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float
)
