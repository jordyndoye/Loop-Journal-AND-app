package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.DayDao
import com.example.data.local.entity.JournalEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository class to abstract the data source and provide a clean API
 * for ViewModels to interact with the Room database for journal entries.
 */
class JournalRepository(
  private val dayDao: DayDao,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

  /**
   * Observe all journal entries ordered by date descending.
   * Returns a reactive Flow for UI consumption.
   */
  val allJournalEntries: Flow<List<JournalEntry>> = dayDao.observeAllJournalEntries()

  /**
   * Observe a single journal entry for a given date (yyyy-MM-dd).
   */
  fun observeJournalEntry(date: String): Flow<JournalEntry?> {
    return dayDao.observeJournalEntry(date)
  }

  /**
   * Directly fetch a single journal entry by date.
   */
  suspend fun getJournalEntry(date: String): JournalEntry? = withContext(ioDispatcher) {
    dayDao.getJournalEntry(date)
  }

  /**
   * Directly fetch all journal entries.
   */
  suspend fun getAllJournalEntries(): List<JournalEntry> = withContext(ioDispatcher) {
    dayDao.getAllJournalEntries()
  }

  /**
   * Insert or update a JournalEntry entity in Room.
   */
  suspend fun insertOrUpdate(entry: JournalEntry) = withContext(ioDispatcher) {
    dayDao.upsertJournalEntry(entry)
  }

  /**
   * Convenience method to save daily observations: date, reflection, and identified break points.
   */
  suspend fun saveJournalEntry(
    date: String,
    reflection: String,
    breakPoints: String = "",
    energyRating: Float = 3.0f,
    stressRating: Float = 2.0f,
    isClosed: Boolean = false,
    closedAtTime: String? = null
  ) = withContext(ioDispatcher) {
    val existing = dayDao.getJournalEntry(date)
    val entry = JournalEntry(
      date = date,
      reflection = reflection,
      breakPoints = breakPoints,
      wentToPlan = reflection.ifBlank { existing?.wentToPlan ?: "" },
      didNotGoToPlan = existing?.didNotGoToPlan ?: "",
      inTheWay = breakPoints.ifBlank { existing?.inTheWay ?: "" },
      energyRating = energyRating,
      stressRating = stressRating,
      isClosed = isClosed,
      closedAtTime = closedAtTime ?: existing?.closedAtTime
    )
    dayDao.upsertJournalEntry(entry)
  }

  /**
   * Delete a journal entry by its date.
   */
  suspend fun deleteJournalEntry(date: String) = withContext(ioDispatcher) {
    dayDao.deleteJournalEntryByDate(date)
  }

  /**
   * Delete a specific journal entry entity.
   */
  suspend fun delete(entry: JournalEntry) = withContext(ioDispatcher) {
    dayDao.deleteJournalEntry(entry)
  }

  companion object {
    @Volatile
    private var INSTANCE: JournalRepository? = null

    /**
     * Get or create a singleton instance using the application context.
     */
    fun getInstance(context: Context): JournalRepository {
      return INSTANCE ?: synchronized(this) {
        val database = AppDatabase.getInstance(context)
        val instance = JournalRepository(database.dayDao())
        INSTANCE = instance
        instance
      }
    }

    /**
     * Create an instance directly from a DayDao.
     */
    fun fromDao(dao: DayDao): JournalRepository {
      return JournalRepository(dao)
    }
  }
}

/**
 * Alias for semantic flexibility.
 */
typealias JournalEntryRepository = JournalRepository
