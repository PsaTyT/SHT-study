package com.example.sht.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS completions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_completions_habitId ON completions(habitId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_completions_date ON completions(date)")

                val today = LocalDate.now().toEpochDay()
                db.execSQL("""
                    INSERT INTO completions (habitId, date)
                    SELECT id, $today FROM habits WHERE doneToday = 1
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habits_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("INSERT INTO habits_new (id, title) SELECT id, title FROM habits")

                db.execSQL("DROP TABLE habits")

                db.execSQL("ALTER TABLE habits_new RENAME TO habits")
            }
        }

        fun getInstance(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "sht_study.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
