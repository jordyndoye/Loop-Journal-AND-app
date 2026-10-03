package com.example.domain.model

enum class ExperimentStatus(val displayName: String) {
  ACTIVE("In Progress"),
  KEPT("Kept in System"),
  MODIFIED("Modified & Testing"),
  ABANDONED("Abandoned / Ineffective")
}

data class Experiment(
  val id: String,
  val number: Int,
  val title: String,
  val hypothesis: String,
  val singleIntervention: String,
  val metricToWatch: String,
  val status: ExperimentStatus = ExperimentStatus.ACTIVE,
  val dayCount: Int = 1,
  val totalDays: Int = 7,
  val observationNotes: String = ""
)
