package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DayDao
import com.example.data.local.entity.BlockOutcomeEntity
import com.example.data.local.entity.DayRecordEntity
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.SystemBlockEntity
import com.example.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
  entities = [
    DayRecordEntity::class,
    BlockOutcomeEntity::class,
    JournalEntryEntity::class,
    SystemBlockEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun dayDao(): DayDao

  companion object {
    const val DB_NAME = "loop_journal_v2.db"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          DB_NAME
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    /**
     * Safely opens and initializes the Room database from a background coroutine
     * after the UI becomes visible.
     *
     * If opening throws (e.g. database corruption, migration failures, or SQLite lock),
     * it invokes [recreateDatabase] to delete the corrupted database file, recreate an empty
     * database from scratch, and cleanly reinitialize persistence without ever crashing the app.
     */
    suspend fun openSafely(context: Context): AppDatabase? = withContext(Dispatchers.IO) {
      val appContext = context.applicationContext
      try {
        val db = getInstance(appContext)
        val dao = db.dayDao()
        // Execute a probe query to ensure the database file is opened and schema is valid
        dao.getBlockOutcomes("probe_check")
        RoutineRepository.instance.initContext(appContext)
        RoutineRepository.instance.initRoom(dao)
        db
      } catch (t: Throwable) {
        Log.e("AppDatabase", "Database initialization failed; recreating database to prevent crash", t)
        try {
          val newDb = recreateDatabase(appContext)
          val dao = newDb.dayDao()
          dao.getBlockOutcomes("probe_check")
          RoutineRepository.instance.initContext(appContext)
          RoutineRepository.instance.initRoom(dao)
          newDb
        } catch (t2: Throwable) {
          Log.e("AppDatabase", "Failed to recreate database; proceeding with in-memory repository", t2)
          null
        }
      }
    }

    /**
     * Deletes the existing database file and recreates an empty database instance.
     */
    fun recreateDatabase(context: Context): AppDatabase {
      val appContext = context.applicationContext
      synchronized(this) {
        try {
          INSTANCE?.close()
        } catch (e: Throwable) {
          Log.w("AppDatabase", "Error closing old database instance", e)
        }
        INSTANCE = null
        try {
          appContext.deleteDatabase(DB_NAME)
        } catch (e: Throwable) {
          Log.w("AppDatabase", "Error deleting database file $DB_NAME", e)
        }
        val instance = Room.databaseBuilder(
          appContext,
          AppDatabase::class.java,
          DB_NAME
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        return instance
      }
    }
  }
}
