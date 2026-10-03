package com.example.ui.experiments

import com.example.domain.model.Experiment

data class ExperimentsUiState(
  val activeExperiment: Experiment? = null,
  val pastExperiments: List<Experiment> = emptyList(),
  val showModifySheet: Boolean = false,
  val showNewSheet: Boolean = false,
  val notesInput: String = "",
  val feedbackToast: String? = null
)
