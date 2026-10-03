package com.example

import com.example.data.repository.RoutineRepository
import com.example.ui.journal.JournalViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JournalViewModelTest {

  private lateinit var repository: RoutineRepository
  private lateinit var viewModel: JournalViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    viewModel = JournalViewModel(repository)
  }

  @Test
  fun testInitialJournalStateLoaded() {
    val state = viewModel.uiState.value
    assertNotNull(state.todayRecord)
    assertTrue("Journal starts empty without seed past days", state.pastRecords.isEmpty())
  }

  @Test
  fun testUpdateObservationsAndNightNote() {
    viewModel.setWentToPlan("Spine wake on time, deep work executed.")
    viewModel.setDidNotGoToPlan("Midday meal postponed by 45 minutes.")
    viewModel.setInTheWay("Client urgent call.")
    viewModel.setNightNote("Reflective evening note.")

    val state = viewModel.uiState.value
    assertEquals("Spine wake on time, deep work executed.", state.wentToPlanInput)
    assertEquals("Midday meal postponed by 45 minutes.", state.didNotGoToPlanInput)
    assertEquals("Client urgent call.", state.inTheWayInput)
    assertEquals("Reflective evening note.", state.nightNote)
  }

  @Test
  fun testSpineReadingsReflectedInJournalState() {
    repository.saveWakeReadings(energy = 4, mood = 5, stress = 2)
    repository.saveSleepReadings(energy = 2, mood = 3, stress = 1)

    val freshViewModel = JournalViewModel(repository)
    val state = freshViewModel.uiState.value
    assertEquals(4, state.wakeEnergy)
    assertEquals(5, state.wakeMood)
    assertEquals(2, state.wakeStress)
    assertEquals(2, state.sleepEnergy)
    assertEquals(3, state.sleepMood)
    assertEquals(1, state.sleepStress)
    assertTrue("Has wake readings", state.hasWakeReadings)
    assertTrue("Has sleep readings", state.hasSleepReadings)
  }

  @Test
  fun testCommitNightClose() {
    viewModel.setWentToPlan("All solid.")
    viewModel.setDidNotGoToPlan("Late partner dinner.")
    viewModel.setInTheWay("Evening stretched bedtime.")

    viewModel.commitNightClose()

    val updatedToday = repository.todayRecord.value
    assertTrue("Day must be sealed upon night close", updatedToday.isClosed)
    assertTrue("Day must be captured", updatedToday.isCaptured)
    assertEquals("All solid.", updatedToday.wentToPlan)
    assertEquals("Late partner dinner.", updatedToday.didNotGoToPlan)
    assertEquals("Evening stretched bedtime.", updatedToday.inTheWay)

    // Reel day must be captured
    val todayReel = repository.weekReel.value.first { it.isToday }
    assertTrue(todayReel.isCaptured)
  }
}
