package com.example.ui.journal

import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord

data class JournalUiState(
  val formattedDate: String = "",
  val todayRecord: DayRecord? = null,
  val pastRecords: List<DayRecord> = emptyList(),
  val todayBlocks: List<DayBlock> = emptyList(),
  val doneBlocks: List<DayBlock> = emptyList(),
  val missedBlocks: List<DayBlock> = emptyList(),
  val pendingBlocks: List<DayBlock> = emptyList(),
  val nightNote: String = "",
  val wentToPlanInput: String = "",
  val didNotGoToPlanInput: String = "",
  val inTheWayInput: String = "",
  val wakeEnergy: Int? = null,
  val wakeMood: Int? = null,
  val wakeStress: Int? = null,
  val sleepEnergy: Int? = null,
  val sleepMood: Int? = null,
  val sleepStress: Int? = null,
  val isTapeSealed: Boolean = false,
  val showCommitConfirmation: Boolean = false,
  val isCaptured: Boolean = false
) {
  val hasWakeReadings: Boolean
    get() = wakeEnergy != null || wakeMood != null || wakeStress != null

  val hasSleepReadings: Boolean
    get() = sleepEnergy != null || sleepMood != null || sleepStress != null
}
