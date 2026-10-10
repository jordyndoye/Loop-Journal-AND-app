package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BlockOutcomeEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseInitializationTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    // Clean up any existing DB file before test
    context.deleteDatabase(AppDatabase.DB_NAME)
  }

  @Test
  fun testSafeInitializationCreatesDatabase() = runBlocking {
    val db = AppDatabase.openSafely(context)
    assertNotNull("AppDatabase should be initialized successfully", db)

    val dao = db!!.dayDao()
    val testOutcome = BlockOutcomeEntity(
      id = "2026-10-03_test1",
      dateIso = "2026-10-03",
      blockId = "test1",
      title = "Test Block",
      intendedTime = "08:00",
      anchorType = "HARD",
      status = "PENDING"
    )
    dao.upsertBlockOutcome(testOutcome)

    val loaded = dao.getBlockOutcomes("2026-10-03")
    assertEquals(1, loaded.size)
    assertEquals("Test Block", loaded[0].title)
  }

  @Test
  fun testDatabaseSingletonReturnsSameInstance() {
    val db1 = AppDatabase.getInstance(context)
    val db2 = AppDatabase.getInstance(context)
    org.junit.Assert.assertSame("getInstance must return the singleton instance", db1, db2)
  }
}
