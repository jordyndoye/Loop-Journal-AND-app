package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.ui.today.TodayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TodayViewModelTest {

  private lateinit var repository: RoutineRepository
  private lateinit var viewModel: TodayViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    viewModel = TodayViewModel(repository)
  }

  @Test
  fun testSpineAnchorsExist() {
    val blocks = repository.blocks.value
    val wakeSpine = blocks.find { it.anchorType == AnchorType.SPINE_WAKE }
    val sleepSpine = blocks.find { it.anchorType == AnchorType.SPINE_SLEEP }

    assertNotNull("Wake spine block must be present", wakeSpine)
    assertNotNull("Sleep spine block must be present", sleepSpine)
  }

  @Test
  fun testStatusUpdateToMissed() {
    val blocks = repository.blocks.value
    val targetBlock = blocks.first()

    viewModel.setBlockStatus(targetBlock.id, BlockStatus.MISSED)

    val updatedBlock = repository.blocks.value.first { it.id == targetBlock.id }
    assertEquals(BlockStatus.MISSED, updatedBlock.status)

    // Today reel segment should be captured because observation occurred
    val todayReel = repository.weekReel.value.first { it.isToday }
    assertTrue("A reel segment lights amber when captured, including days with missed blocks", todayReel.isCaptured)
  }

  @Test
  fun testStatusUpdateToDoneAndModified() {
    val blocks = repository.blocks.value
    val block2 = blocks[1]
    val block3 = blocks[2]

    viewModel.setBlockStatus(block2.id, BlockStatus.DONE)
    viewModel.setBlockStatus(block3.id, BlockStatus.MODIFIED, note = "Short sleep adjustment")

    val updated2 = repository.blocks.value.first { it.id == block2.id }
    val updated3 = repository.blocks.value.first { it.id == block3.id }

    assertEquals(BlockStatus.DONE, updated2.status)
    assertEquals(BlockStatus.MODIFIED, updated3.status)
    assertEquals("Short sleep adjustment", updated3.note)
  }

  @Test
  fun testRecordMissedBlockWithCauseAndNote() {
    val block = repository.blocks.value[3] // Midday Meal
    viewModel.openMissSheet(block)
    assertEquals(block.id, viewModel.uiState.value.activeMissSheetBlock?.id)

    viewModel.recordMissedBlock(
      blockId = block.id,
      cause = "Late work",
      note = "Meeting ran over by 40 minutes"
    )

    // Verify sheet dismissed
    assertEquals(null, viewModel.uiState.value.activeMissSheetBlock)

    // Verify block updated in repository
    val updated = repository.blocks.value.first { it.id == block.id }
    assertEquals(BlockStatus.MISSED, updated.status)
    assertEquals("Late work", updated.cause)
    assertEquals("Meeting ran over by 40 minutes", updated.note)

    // Verify day is captured (lights amber on reel)
    val todayReel = repository.weekReel.value.first { it.isToday }
    assertTrue(todayReel.isCaptured)
  }

  @Test
  fun testSpineRatingsSavedSeparately() {
    val wakeBlock = repository.blocks.value.first { it.anchorType == AnchorType.SPINE_WAKE }
    val sleepBlock = repository.blocks.value.first { it.anchorType == AnchorType.SPINE_SLEEP }

    viewModel.saveSpineReadings(wakeBlock, energy = 4, mood = 5, stress = 2)
    viewModel.saveSpineReadings(sleepBlock, energy = 3, mood = 4, stress = 1)

    val todayRec = repository.todayRecord.value
    assertEquals(4, todayRec.wakeEnergy)
    assertEquals(5, todayRec.wakeMood)
    assertEquals(2, todayRec.wakeStress)
    assertEquals(3, todayRec.sleepEnergy)
    assertEquals(4, todayRec.sleepMood)
    assertEquals(1, todayRec.sleepStress)
    assertTrue(todayRec.hasWakeReadings)
    assertTrue(todayRec.hasSleepReadings)
  }
}
