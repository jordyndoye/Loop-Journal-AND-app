package com.example.ui.experiments

import com.example.domain.model.Experiment
import com.example.domain.model.ExperimentSuggestion

data class ExperimentsUiState(
  val suggestedExperiment: ExperimentSuggestion? = null,
  val activeExperiment: Experiment? = null,
  val pastExperiments: List<Experiment> = emptyList(),
  val showModifySheet: Boolean = false,
  val showNewSheet: Boolean = false,
  val notesInput: String = "",
  val feedbackToast: String? = null
)
