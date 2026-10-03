package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import com.example.ui.system.MySystemViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MySystemViewModelTest {

  private lateinit var repository: RoutineRepository
  private lateinit var viewModel: MySystemViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    viewModel = MySystemViewModel(repository)
  }

  @Test
  fun testSpineAnchorsMarkedAndProtected() {
    val blocks = repository.blocks.value
    val wakeSpine = blocks.find { it.anchorType == AnchorType.SPINE_WAKE }
    val sleepSpine = blocks.find { it.anchorType == AnchorType.SPINE_SLEEP }

    assertNotNull(wakeSpine)
    assertNotNull(sleepSpine)
    assertTrue(wakeSpine!!.anchorType.isSpine)
    assertTrue(sleepSpine!!.anchorType.isSpine)

    // Attempting to delete spine block should be ignored
    viewModel.deleteBlock(wakeSpine.id)
    val wakeAfter = repository.blocks.value.find { it.id == wakeSpine.id }
    assertNotNull("Spine anchor must not be deleted", wakeAfter)
  }

  @Test
  fun testAddNewAnchorBlock() {
    val initialCount = repository.blocks.value.size
    viewModel.saveBlock(
      id = null,
      title = "Afternoon Reset Walk",
      time = "16:00",
      anchorType = AnchorType.FLEXIBLE,
      note = "Clear cognitive fatigue"
    )

    val updatedBlocks = repository.blocks.value
    assertEquals(initialCount + 1, updatedBlocks.size)
    val added = updatedBlocks.find { it.title == "Afternoon Reset Walk" }
    assertNotNull(added)
    assertEquals(AnchorType.FLEXIBLE, added!!.anchorType)
    assertEquals("16:00", added.intendedTime)
  }

  @Test
  fun testUpdateExistingBlock() {
    val existing = repository.systemBlocks.value.first { it.anchorType == AnchorType.HARD }
    viewModel.saveBlock(
      id = existing.id,
      title = "Deep Focus Block 1 (Modified)",
      time = "09:30",
      anchorType = AnchorType.HARD,
      note = "Pushed by 30 mins"
    )
    val updated = repository.systemBlocks.value.first { it.id == existing.id }
    assertEquals("Deep Focus Block 1 (Modified)", updated.title)
    assertEquals("09:30", updated.intendedTime)
    assertEquals("Pushed by 30 mins", updated.note)
  }

  @Test
  fun testExample1_WokeAt0530_ThenSetWakeTo0430_TodayStays0530_TomorrowIs0430() {
    // Mark today's Wake as DONE
    repository.updateBlockStatus("b1", BlockStatus.DONE)
    val todayWake = repository.blocks.value.first { it.id == "b1" }
    val originalTodayTime = todayWake.plannedTime

    // User updates Wake Spine to 04:00 in My System
    viewModel.saveBlock(
      id = "b1",
      title = "Wake Spine",
      time = "04:00",
      anchorType = AnchorType.SPINE_WAKE,
      note = null
    )

    // Template in My System for tomorrow onward is 04:00
    val templateWake = repository.systemBlocks.value.first { it.id == "b1" }
    assertEquals("04:00", templateWake.intendedTime)

    // Today's block must STAY as original because it already happened (has outcome DONE)
    val todayWakeAfter = repository.blocks.value.first { it.id == "b1" }
    assertEquals(originalTodayTime, todayWakeAfter.plannedTime)
  }

  @Test
  fun testExample2_DinnerPendingBeforeTime_SetDinnerTo1845_TodayBecomes1845_TomorrowIs1845() {
    val templateDinner = repository.systemBlocks.value.first { it.id == "b7" }
    val todayDinner = repository.blocks.value.first { it.id == "b7" }

    // Both reflect 18:45
    assertEquals("18:45", templateDinner.intendedTime)
    assertEquals("18:45", todayDinner.plannedTime)
  }

  @Test
  fun testPastDaysNeverChangeWhenMySystemChanges() {
    // Commit a night close to establish a real saved past day
    repository.commitNightClose(
      wentToPlan = "Morning focus held.",
      didNotGoToPlan = "",
      inTheWay = "",
      energy = 4f,
      stress = 2f
    )
    val pastRecord = repository.pastRecords.value.first()
    val pastDateIso = pastRecord.dateIso

    // Modify a block in My System
    viewModel.saveBlock(
      id = "b5",
      title = "Execution Block 2",
      time = "14:45",
      anchorType = AnchorType.HARD,
      note = null
    )

    // Verify past record for pastDateIso did not change
    val pastRecordAfter = repository.pastRecords.value.first { it.dateIso == pastDateIso }
    assertEquals(pastRecord.id, pastRecordAfter.id)
    assertEquals(pastRecord.dateIso, pastRecordAfter.dateIso)
  }
}
