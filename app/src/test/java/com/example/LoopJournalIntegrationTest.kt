package com.example

import com.example.data.repository.RoutineRepository
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.domain.model.ExperimentStatus
import com.example.ui.experiments.ExperimentsViewModel
import com.example.ui.insights.InsightsViewModel
import com.example.ui.journal.JournalViewModel
import com.example.ui.today.TodayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoopJournalIntegrationTest {

  private lateinit var repository: RoutineRepository
  private lateinit var todayViewModel: TodayViewModel
  private lateinit var journalViewModel: JournalViewModel
  private lateinit var insightsViewModel: InsightsViewModel
  private lateinit var experimentsViewModel: ExperimentsViewModel

  @Before
  fun setUp() {
    repository = RoutineRepository()
    todayViewModel = TodayViewModel(repository)
    journalViewModel = JournalViewModel(repository)
    insightsViewModel = InsightsViewModel(repository)
    experimentsViewModel = ExperimentsViewModel(repository)
  }

  @Test
  fun testFullCoreLoop_Plan_Observe_Breakpoint_Journal_Insight_Experiment_Adjust() {
    // 1. PLAN & DO: Routine board has wake and sleep spine
    val initialBlocks = repository.blocks.value
    val wakeSpine = initialBlocks.find { it.anchorType == AnchorType.SPINE_WAKE }
    val sleepSpine = initialBlocks.find { it.anchorType == AnchorType.SPINE_SLEEP }
    assertNotNull(wakeSpine)
    assertNotNull(sleepSpine)

    // Mark Wake as Done and save morning spine ratings
    todayViewModel.setBlockStatus(wakeSpine!!.id, BlockStatus.DONE)
    todayViewModel.saveSpineReadings(wakeSpine, energy = 4, mood = 5, stress = 2)
    assertEquals(BlockStatus.DONE, repository.blocks.value.find { it.id == wakeSpine.id }?.status)
    assertEquals(4, repository.todayRecord.value.wakeEnergy)

    // 2. BREAKPOINT & CAUSE: Mark a block missed with reality cause
    val afternoonBlock = initialBlocks.find { it.title.contains("Execution Block 2") } ?: initialBlocks[4]
    todayViewModel.recordMissedBlock(
      blockId = afternoonBlock.id,
      cause = "Late work overrun",
      note = "Emergency client triage"
    )
    val missedBlock = repository.blocks.value.find { it.id == afternoonBlock.id }!!
    assertEquals(BlockStatus.MISSED, missedBlock.status)
    assertEquals("Late work overrun", missedBlock.cause)

    // 3. NIGHT CLOSE (Journal):
    journalViewModel.setWentToPlan("Morning training and deep work 1 held solid ground.")
    journalViewModel.setDidNotGoToPlan("Afternoon work overrun delayed partner evening.")
    journalViewModel.setInTheWay("Late client triage without time buffer.")
    journalViewModel.commitNightClose()

    val closedRecord = repository.todayRecord.value
    assertTrue("Night close must seal the day tape", closedRecord.isClosed)
    assertTrue("Day must be captured on reel even with missed blocks", closedRecord.isCaptured)

    // 4. INSIGHTS & SYNTHESIS:
    // With fewer than 7 days, synthesis must be null ("Not enough days yet")
    val synthesis = insightsViewModel.uiState.value.synthesis
    assertEquals(null, synthesis)

    // 5. EXPERIMENT (Keep / Modify / Abandon):
    // Experiments starts empty; user initiates a cycle
    experimentsViewModel.createNewExperiment(
      title = "Evening Buffer",
      hypothesis = "Buffer prevents late work overrun.",
      intervention = "Stop work at 17:45.",
      metric = "Dinner start time",
      durationDays = 7
    )
    val activeExperiment = repository.activeExperiment.value
    assertNotNull(activeExperiment)

    // Test cycle decision: User chooses KEEP
    val initialBoardSize = repository.blocks.value.size
    experimentsViewModel.updateNotes("Buffer prevented late work overrun across 7 days.")
    experimentsViewModel.keepExperiment()

    val keptExp = repository.activeExperiment.value
    assertEquals(ExperimentStatus.KEPT, keptExp?.status)

    // 6. ADJUST (Integrated into My System routine board):
    val updatedBoard = repository.blocks.value
    assertEquals("Kept experiment must integrate as an anchor on the board", initialBoardSize + 1, updatedBoard.size)
    val integratedAnchor = updatedBoard.find { it.id == "b_exp_${activeExperiment!!.number}" }
    assertNotNull(integratedAnchor)
  }
}
