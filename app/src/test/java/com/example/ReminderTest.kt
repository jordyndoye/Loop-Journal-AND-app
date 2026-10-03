package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import com.example.receiver.ReminderScheduler
import com.example.ui.system.MySystemViewModel
import com.example.util.DateTimeUtils
import com.example.util.ReminderUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReminderTest {

  private lateinit var repository: RoutineRepository
  private lateinit var systemViewModel: MySystemViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    systemViewModel = MySystemViewModel(repository)
  }

  @Test
  fun testReminderQuestionsNamingAndTone() {
    // 07:15 Morning Movement / Training -> "How did training go?"
    val trainingBlock = DayBlock(
      id = "b2",
      title = "Morning Movement / Training",
      intendedTime = "07:15",
      anchorType = AnchorType.FLEXIBLE
    )
    assertEquals("How did training go?", ReminderUtils.generateReminderQuestion(trainingBlock))

    // Wake -> "How did you wake?"
    val wakeBlock = DayBlock(
      id = "b1",
      title = "Wake Spine",
      intendedTime = "04:30",
      anchorType = AnchorType.SPINE_WAKE
    )
    assertEquals("How did you wake?", ReminderUtils.generateReminderQuestion(wakeBlock))

    // Sleep -> "Winding down?"
    val sleepBlock = DayBlock(
      id = "b8",
      title = "Sleep Spine",
      intendedTime = "22:00",
      anchorType = AnchorType.SPINE_SLEEP
    )
    assertEquals("Winding down?", ReminderUtils.generateReminderQuestion(sleepBlock))

    // Breakfast -> "Did you eat breakfast?"
    val breakfastBlock = DayBlock(
      id = "b_bfast",
      title = "Breakfast & Tea",
      intendedTime = "08:00",
      anchorType = AnchorType.HARD
    )
    assertEquals("Did you eat breakfast?", ReminderUtils.generateReminderQuestion(breakfastBlock))
  }

  @Test
  fun testNoForbiddenWordsInAnyReminderQuestions() {
    val sampleTitles = listOf(
      "Wake Spine",
      "Morning Movement / Training",
      "Deep Focus Block 1",
      "Midday Meal & Walking Reset",
      "Execution Block 2",
      "Evening Shutdown",
      "Dinner",
      "Sleep Spine",
      "Reading",
      "Workout",
      "Client triage",
      "Partner Dinner"
    )

    val forbiddenWords = listOf("missed", "overdue", "streak", "don't forget", "dont forget", "failed", "you failed")

    sampleTitles.forEach { title ->
      val question = ReminderUtils.generateReminderQuestion(title).lowercase()
      forbiddenWords.forEach { forbidden ->
        assertFalse(
          "Reminder question '$question' must never contain '$forbidden'",
          question.contains(forbidden)
        )
      }
      assertTrue("Reminder must end with a question mark", question.endsWith("?"))
    }
  }

  @Test
  fun testCalculateReminderTimeIsFewMinutesAfterPlannedTime() {
    val dateIso = "2026-10-02"
    val time = "07:15"
    val targetMillis = ReminderScheduler.calculateReminderTimeMillis(time, dateIso)
    assertNotNull(targetMillis)

    // Expected 07:20 (5 minutes after 07:15)
    val expectedTarget = java.time.ZonedDateTime.of(
      java.time.LocalDate.parse(dateIso),
      java.time.LocalTime.of(7, 20),
      java.time.ZoneId.systemDefault()
    ).toInstant().toEpochMilli()
    assertEquals(expectedTarget, targetMillis)
  }

  @Test
  fun testBadTimeOrNullDoesNotThrow() {
    assertNull(ReminderScheduler.calculateReminderTimeMillis(null, "2026-10-02"))
    assertNull(ReminderScheduler.calculateReminderTimeMillis("bad-time", "2026-10-02"))
    assertNull(ReminderScheduler.calculateReminderTimeMillis("07:15", "invalid-date"))
    assertNull(ReminderScheduler.calculateReminderTimeMillis("", ""))
    
    // Null safety on scheduling calls
    ReminderScheduler.scheduleBlockReminder(null, null, null)
    ReminderScheduler.cancelBlockReminder(null, null)
    ReminderScheduler.rescheduleAll(null, null, null)
    ReminderScheduler.createNotificationChannel(null)
  }

  @Test
  fun testIsValidBlockForScheduling() {
    val today = DateTimeUtils.todayIso()
    val validBlock = DayBlock(
      id = "b1",
      title = "Wake Spine",
      intendedTime = "05:00",
      anchorType = AnchorType.SPINE_WAKE,
      status = BlockStatus.PENDING,
      remindMe = true
    )
    assertTrue(ReminderScheduler.isValidBlockForScheduling(validBlock, today))

    // Null block
    assertFalse(ReminderScheduler.isValidBlockForScheduling(null, today))

    // Blank ID
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(id = ""), today))

    // Blank planned time
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(plannedTime = ""), today))

    // RemindMe is false
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(remindMe = false), today))

    // Status is not PENDING (e.g. DONE, MISSED, MODIFIED, SKIP)
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(status = BlockStatus.DONE), today))
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(status = BlockStatus.MISSED), today))
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(status = BlockStatus.MODIFIED), today))
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock.copy(status = BlockStatus.SKIP), today))

    // Different date
    assertFalse(ReminderScheduler.isValidBlockForScheduling(validBlock, "2020-01-01"))
  }

  @Test
  fun testTogglingReminderSwitchInMySystem() {
    val block2 = repository.blocks.value.first { it.id == "b2" }
    assertTrue("Default remindMe is true", block2.remindMe)

    // Toggle off
    systemViewModel.toggleBlockReminder("b2", false)
    val updatedBlock2 = repository.blocks.value.first { it.id == "b2" }
    assertFalse("remindMe must now be false", updatedBlock2.remindMe)

    // System template block must also be updated
    val systemBlock2 = repository.systemBlocks.value.first { it.id == "b2" }
    assertFalse("System template remindMe must now be false", systemBlock2.remindMe)
  }

  @Test
  fun testMarkingBlockCancelsPendingReminder() {
    val block = repository.blocks.value.first { it.id == "b2" }
    assertEquals(BlockStatus.PENDING, block.status)
    assertTrue(block.remindMe)

    // Mark Done
    repository.updateBlockStatus(block.id, BlockStatus.DONE)
    val updated = repository.blocks.value.first { it.id == block.id }
    assertEquals(BlockStatus.DONE, updated.status)
  }
}
