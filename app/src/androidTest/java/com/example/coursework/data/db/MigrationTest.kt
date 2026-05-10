package com.example.coursework.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.coursework.data.db.migrations.MIGRATION_3_4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate3To4_addsIsArchivedColumnDefaultZero() {
        helper.createDatabase(dbName, 3).apply {
            execSQL(
                "INSERT INTO run_type (id, name, targetDistanceMeters) VALUES (1, '5k', 5000.0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            4,
            true,
            MIGRATION_3_4
        )

        val cursor = db.query("SELECT id, name, isArchived FROM run_type WHERE id = 1")
        cursor.use {
            assertEquals(true, it.moveToFirst())
            assertEquals(1L, it.getLong(0))
            assertEquals("5k", it.getString(1))
            assertEquals(0, it.getInt(2))
        }
        db.close()
    }
}
