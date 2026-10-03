package com.example

import com.example.data.repository.RoutineRepository
import com.example.ui.insights.InsightsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
  }

  @Test
  fun testPromoteSuggestedExperimentWhenSynthesisExists() {
    val suggestion = com.example.domain.model.ExperimentSuggestion(
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
}
