package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.ExperimentStatus
import com.example.ui.experiments.ExperimentsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExperimentsViewModelTest {

  private lateinit var repository: RoutineRepository
  private lateinit var viewModel: ExperimentsViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    viewModel = ExperimentsViewModel(repository)
  }

  @Test
  fun testExperimentsStartsEmpty() {
    val state = viewModel.uiState.value
    // Experiments starts empty. One experiment only after the user creates it.
    assertEquals(null, state.activeExperiment)
    assertTrue("Past experiments archive starts empty", state.pastExperiments.isEmpty())
  }

  @Test
  fun testCreateExperimentAndKeepIntegratesIntoBoard() {
    viewModel.createNewExperiment(
      title = "Evening Buffer",
      hypothesis = "15m buffer prevents drift.",
      intervention = "Stop work at 17:45.",
      metric = "Dinner start time",
      durationDays = 7
    )
    val active = repository.activeExperiment.value
    assertNotNull("Created experiment must be active", active)

    val initialBoardCount = repository.blocks.value.size
    viewModel.updateNotes("Intervention stabilized evening wind-down.")
    viewModel.keepExperiment()

    val updatedActive = repository.activeExperiment.value
    assertEquals(ExperimentStatus.KEPT, updatedActive?.status)

    // Verify it was integrated into the system board
    val updatedBoard = repository.blocks.value
    assertEquals(initialBoardCount + 1, updatedBoard.size)
    val integratedBlock = updatedBoard.find { it.id == "b_exp_${active!!.number}" }
    assertNotNull(integratedBlock)
  }

  @Test
  fun testModifyExperimentResetsCycle() {
    viewModel.createNewExperiment(
      title = "Evening Buffer",
      hypothesis = "15m buffer prevents drift.",
      intervention = "Stop work at 17:45.",
      metric = "Dinner start time",
      durationDays = 7
    )
    viewModel.confirmModify(
      modifiedIntervention = "Set 21:45 hard stop instead of 21:30.",
      notes = "Tuning timing buffer."
    )

    val updated = repository.activeExperiment.value
    assertNotNull(updated)
    assertEquals(ExperimentStatus.MODIFIED, updated!!.status)
    assertEquals("Set 21:45 hard stop instead of 21:30.", updated.singleIntervention)
    assertEquals(1, updated.dayCount)
  }

  @Test
  fun testAbandonExperiment() {
    viewModel.createNewExperiment(
      title = "Evening Buffer",
      hypothesis = "15m buffer prevents drift.",
      intervention = "Stop work at 17:45.",
      metric = "Dinner start time",
      durationDays = 7
    )
    viewModel.updateNotes("Did not buffer late work overrun.")
    viewModel.abandonExperiment()

    val updated = repository.activeExperiment.value
    assertNotNull(updated)
    assertEquals(ExperimentStatus.ABANDONED, updated!!.status)
  }
}
