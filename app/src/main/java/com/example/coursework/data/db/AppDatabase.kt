package com.example.coursework.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.coursework.data.db.dao.RunSessionDao
import com.example.coursework.data.db.dao.RunTypeDao
import com.example.coursework.data.db.entity.RunPointEntity
import com.example.coursework.data.db.entity.RunSessionEntity
import com.example.coursework.data.db.entity.RunTypeEntity

@Database(
    entities = [
        RunTypeEntity::class,
        RunSessionEntity::class,
        RunPointEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runTypeDao(): RunTypeDao
    abstract fun runSessionDao(): RunSessionDao
}
