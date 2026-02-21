package com.example.coursework.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.coursework.data.db.dao.RunTypeDao
import com.example.coursework.data.db.entity.RunTypeEntity

@Database(
    entities = [
        RunTypeEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runTypeDao(): RunTypeDao
}
