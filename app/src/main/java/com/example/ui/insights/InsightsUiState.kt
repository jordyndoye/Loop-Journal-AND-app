package com.example.ui.insights

import com.example.domain.model.DayRecord
import com.example.domain.model.TapeSynthesis

data class InsightsUiState(
  val synthesis: TapeSynthesis? = null,
  val capturedDays: List<DayRecord> = emptyList(),
  val capturedDaysCount: Int = 0,
  val isExperimentPromoted: Boolean = false,
  val feedbackMessage: String? = null
)
