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
    // schema revisions including the baseline
    version = 4,
    // writes JSON schema snapshots that migrations are verified against at compile time
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // No runPointDao(): points are loaded via RunSessionWithPoints on the session DAO, never
    // on their own.
    abstract fun runTypeDao(): RunTypeDao
    abstract fun runSessionDao(): RunSessionDao
}
