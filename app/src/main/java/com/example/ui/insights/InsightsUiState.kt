package com.example.ui.insights

import com.example.domain.model.TapeSynthesis

data class InsightsUiState(
  val synthesis: TapeSynthesis? = null,
  val isExperimentPromoted: Boolean = false,
  val feedbackMessage: String? = null
)
