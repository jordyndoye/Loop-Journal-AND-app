package com.example.domain.model

data class BreakpointChain(
  val id: String,
  val title: String,
  val trigger: String,
  val mechanism: String,
  val downstreamImpact: String,
  val frequencyNote: String
)

data class ExperimentSuggestion(
  val id: String,
  val number: Int,
  val title: String,
  val hypothesis: String,
  val singleIntervention: String,
  val metricToWatch: String,
  val targetDurationDays: Int = 7
)

data class TapeSynthesis(
  val playbackNote: String,
  val daysAnalyzed: Int,
  val capturedDaysCount: Int,
  val totalBreakpointsCount: Int,
  val chains: List<BreakpointChain>,
  val oneSuggestedExperiment: ExperimentSuggestion? = null
)
