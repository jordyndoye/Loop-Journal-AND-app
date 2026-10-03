package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.ExperimentStatus
import com.example.domain.model.ExperimentSuggestion
import com.example.ui.insights.InsightsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InsightsViewModelTest {

  private lateinit var repository: RoutineRepository
  private lateinit var viewModel: InsightsViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    viewModel = InsightsViewModel(repository)
  }

  @Test
  fun testInitialStateFewerThan7DaysShowsNoInventedSynthesis() {
    val state = viewModel.uiState.value
    // Fewer than 7 captured days must show null synthesis ("Not enough days yet")
    assertEquals(null, state.synthesis)
    assertTrue("Captured days must be under 7 initially", state.capturedDaysCount < 7)
  }

  @Test
  fun testPromoteSuggestedExperimentWhenSynthesisExists() {
    val suggestion = ExperimentSuggestion(
      id = "sug_1",
      number = 4,
      title = "Buffer Period",
      hypothesis = "Adding 15m buffer prevents drift.",
      singleIntervention = "Add 15m buffer.",
      metricToWatch = "Evening start time"
    )
    repository.promoteSuggestionToExperiment(suggestion)

    val active = repository.activeExperiment.value
    assertNotNull(active)
    assertEquals(suggestion.number, active!!.number)
    assertEquals(suggestion.title, active.title)
  }

  @Test
  fun testSuggestionDecisionsDoNotStartIt() {
    val suggestion = ExperimentSuggestion(
      id = "sug_silent",
      number = 1,
      title = "15-Minute Hard Shutdown",
      hypothesis = "Testing earlier buffer.",
      singleIntervention = "Set 17:45 hard stop alarm.",
      metricToWatch = "Shutdown completion"
    )

    // 1. Keep suggestion
    repository.keepSuggestedExperiment(suggestion)
    // "Do not start it" -> active experiment must remain null!
    assertNull(repository.activeExperiment.value)
    val kept = repository.pastExperiments.value.find { it.title == suggestion.title }
    assertNotNull(kept)
    assertEquals(ExperimentStatus.KEPT, kept!!.status)
    assertEquals(0, kept.dayCount)

    // 2. Modify suggestion
    val suggestion2 = suggestion.copy(number = 2, title = "Evening Read")
    repository.modifySuggestedExperiment(suggestion2, "Read 20m before sleep", "Tuned intervention")
    assertNull(repository.activeExperiment.value)
    val modified = repository.pastExperiments.value.find { it.title == suggestion2.title }
    assertNotNull(modified)
    assertEquals(ExperimentStatus.MODIFIED, modified!!.status)
    assertEquals(0, modified.dayCount)

    // 3. Abandon suggestion
    val suggestion3 = suggestion.copy(number = 3, title = "Morning Sprint")
    repository.abandonSuggestedExperiment(suggestion3, "Abandoned")
    assertNull(repository.activeExperiment.value)
    val abandoned = repository.pastExperiments.value.find { it.title == suggestion3.title }
    assertNotNull(abandoned)
    assertEquals(ExperimentStatus.ABANDONED, abandoned!!.status)
  }

  @Test
  fun testNeverWriteModelOutputBackIntoDayLog() {
    val initialBlocks = repository.blocks.value.map { it.copy() }
    val initialToday = repository.todayRecord.value.copy()

    val suggestion = ExperimentSuggestion(
      id = "sug_test",
      number = 99,
      title = "Experimental Intervention",
      hypothesis = "Test hypothesis",
      singleIntervention = "Do test action",
      metricToWatch = "Completion"
    )

    repository.keepSuggestedExperiment(suggestion)

    // Day log and blocks must never be corrupted with model output
    assertEquals(initialToday.wentToPlan, repository.todayRecord.value.wentToPlan)
    assertEquals(initialToday.didNotGoToPlan, repository.todayRecord.value.didNotGoToPlan)
    assertEquals(initialToday.wakeEnergy, repository.todayRecord.value.wakeEnergy)
    assertEquals(initialToday.sleepEnergy, repository.todayRecord.value.sleepEnergy)
    assertEquals(initialBlocks.size, repository.blocks.value.size)
  }
}
