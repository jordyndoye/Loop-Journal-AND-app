package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.JournalEntry
import com.example.data.repository.JournalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JournalRepositoryTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: JournalRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = JournalRepository(database.dayDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testSaveAndGetJournalEntry() = runBlocking {
    repository.saveJournalEntry(
      date = "2026-10-03",
      reflection = "Solid focus during morning block.",
      breakPoints = "Late afternoon interruption on email."
    )

    val entry = repository.getJournalEntry("2026-10-03")
    assertNotNull("Entry should be persisted", entry)
    assertEquals("2026-10-03", entry?.date)
    assertEquals("Solid focus during morning block.", entry?.reflection)
    assertEquals("Late afternoon interruption on email.", entry?.breakPoints)
  }

  @Test
  fun testObserveAllJournalEntries() = runBlocking {
    repository.insertOrUpdate(
      JournalEntry(
        date = "2026-10-01",
        reflection = "Day 1 reflection",
        breakPoints = "Friction point 1"
      )
    )
    repository.insertOrUpdate(
      JournalEntry(
        date = "2026-10-02",
        reflection = "Day 2 reflection",
        breakPoints = "Friction point 2"
      )
    )

    val list = repository.allJournalEntries.first()
    assertEquals(2, list.size)
    assertEquals("2026-10-02", list[0].date)
    assertEquals("2026-10-01", list[1].date)
  }

  @Test
  fun testObserveSpecificJournalEntry() = runBlocking {
    repository.saveJournalEntry(
      date = "2026-10-03",
      reflection = "Morning anchor held",
      breakPoints = "None"
    )

    val flowEntry = repository.observeJournalEntry("2026-10-03").first()
    assertNotNull(flowEntry)
    assertEquals("Morning anchor held", flowEntry?.reflection)
  }

  @Test
  fun testDeleteJournalEntry() = runBlocking {
    repository.saveJournalEntry(
      date = "2026-10-03",
      reflection = "Temporary observation",
      breakPoints = "Transient"
    )

    val beforeDelete = repository.getJournalEntry("2026-10-03")
    assertNotNull(beforeDelete)

    repository.deleteJournalEntry("2026-10-03")

    val afterDelete = repository.getJournalEntry("2026-10-03")
    assertNull(afterDelete)
  }
}
