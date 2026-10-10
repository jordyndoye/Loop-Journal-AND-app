package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DayDao
import com.example.data.local.entity.BlockOutcomeEntity
import com.example.data.local.entity.DayRecordEntity
import com.example.data.local.entity.InsightsDraftEntity
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
    SystemBlockEntity::class,
    InsightsDraftEntity::class
  ],
  version = 4,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun dayDao(): DayDao

  companion object {
    const val DB_NAME = "loop_journal_v2.db"

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          "CREATE TABLE IF NOT EXISTS `insights_draft` (`id` TEXT NOT NULL, `rawJson` TEXT NOT NULL, `fingerprint` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
      }
    }

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          DB_NAME
        )
          .addMigrations(MIGRATION_3_4)
          .build()
        INSTANCE = instance
        instance
      }
    }

    suspend fun openSafely(context: Context): AppDatabase? = withContext(Dispatchers.IO) {
      val appContext = context.applicationContext
      try {
        val db = getInstance(appContext)
        val dao = db.dayDao()
        dao.getBlockOutcomes("probe_check")
        RoutineRepository.instance.initContext(appContext)
        RoutineRepository.instance.initRoom(dao)
        db
      } catch (t: Throwable) {
        Log.e("AppDatabase", "Database open failed; keeping the file", t)
        null
      }
    }
  }
}
