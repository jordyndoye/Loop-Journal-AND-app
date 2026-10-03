package com.example.ui.today

import com.example.domain.model.DayBlock
import com.example.ui.components.ReelDay

data class TodayUiState(
  val blocks: List<DayBlock> = emptyList(),
  val weekReel: List<ReelDay> = emptyList(),
  val isTodayCaptured: Boolean = false,
  val selectedDay: ReelDay? = null,
  val blockBeingEdited: DayBlock? = null,
  val activeMissSheetBlock: DayBlock? = null,
  val dateIso: String = "", // Canonical yyyy-MM-dd
  val formattedHeaderDate: String = "", // Localized e.g. "Friday, Oct 2"
  val currentBlockId: String? = null // Active block based on phone clock
)
