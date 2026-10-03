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
  fun testRecreateDatabaseDeletesOldFileAndRebuilds() = runBlocking {
    val db1 = AppDatabase.openSafely(context)
    assertNotNull(db1)
    val dao1 = db1!!.dayDao()
    dao1.upsertBlockOutcome(
      BlockOutcomeEntity(
        id = "2026-10-03_item_a",
        dateIso = "2026-10-03",
        blockId = "item_a",
        title = "Item A",
        intendedTime = "09:00",
        anchorType = "FLEXIBLE",
        status = "DONE"
      )
    )

    // Recreate database file
    val db2 = AppDatabase.recreateDatabase(context)
    assertNotNull("Recreated database must not be null", db2)

    val dao2 = db2.dayDao()
    val itemsAfterRecreate = dao2.getBlockOutcomes("2026-10-03")
    assertTrue("Recreated database starts empty", itemsAfterRecreate.isEmpty())
  }

  @Test
  fun testCorruptedDatabaseRecoversGracefully() = runBlocking {
    // Intentionally corrupt the database file with garbage bytes
    val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
    dbFile.parentFile?.mkdirs()
    FileOutputStream(dbFile).use { it.write("NOT_A_VALID_SQLITE_DATABASE_HEADER".toByteArray()) }

    // openSafely should detect the failure, delete the corrupted file, and recreate cleanly
    val recoveredDb = AppDatabase.openSafely(context)
    assertNotNull("AppDatabase must recover from corrupted database file without crashing", recoveredDb)

    val dao = recoveredDb!!.dayDao()
    val result = dao.getBlockOutcomes("2026-10-03")
    assertTrue(result.isEmpty())
  }
}
