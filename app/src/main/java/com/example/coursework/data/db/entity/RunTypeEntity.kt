package com.example.coursework.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "run_type",
    indices = [Index(value = ["name"], unique = true)]
)
data class RunTypeEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetDistanceMeters: Float
)


