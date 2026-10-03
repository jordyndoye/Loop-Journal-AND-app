package com.example.ui.experiments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RoutineRepository
import com.example.domain.model.Experiment
import com.example.domain.model.ExperimentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExperimentsViewModel(
  private val repository: RoutineRepository = RoutineRepository.instance
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    ExperimentsUiState(
      activeExperiment = repository.activeExperiment.value,
      pastExperiments = repository.pastExperiments.value,
      notesInput = repository.activeExperiment.value?.observationNotes ?: ""
    )
  )
  val uiState: StateFlow<ExperimentsUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      combine(
        repository.activeExperiment,
        repository.pastExperiments
      ) { active, past ->
        Pair(active, past)
      }.collect { (active, past) ->
        _uiState.update { current ->
          current.copy(
            activeExperiment = active,
            pastExperiments = past,
            notesInput = if (current.notesInput.isBlank()) active?.observationNotes ?: "" else current.notesInput
          )
        }
      }
    }
  }

  fun updateNotes(notes: String) {
    _uiState.update { it.copy(notesInput = notes) }
    repository.updateExperimentObservationNotes(notes)
  }

  fun keepExperiment() {
    val notes = _uiState.value.notesInput
    repository.recordExperimentDecision(
      decision = ExperimentStatus.KEPT,
      notes = notes
    )
    _uiState.update {
      it.copy(feedbackToast = "Experiment KEPT: Integrated permanently into My System routine board.")
    }
  }

  fun abandonExperiment() {
    val notes = _uiState.value.notesInput
    repository.recordExperimentDecision(
      decision = ExperimentStatus.ABANDONED,
      notes = notes
    )
    _uiState.update {
      it.copy(feedbackToast = "Experiment ABANDONED: Discarded without altering routine board.")
    }
  }

  fun startModify() {
    _uiState.update { it.copy(showModifySheet = true) }
  }

  fun dismissModify() {
    _uiState.update { it.copy(showModifySheet = false) }
  }

  fun confirmModify(modifiedIntervention: String, notes: String) {
    repository.recordExperimentDecision(
      decision = ExperimentStatus.MODIFIED,
      notes = notes,
      modifiedIntervention = modifiedIntervention
    )
    _uiState.update {
      it.copy(
        showModifySheet = false,
        feedbackToast = "Experiment MODIFIED: New intervention staged; cycle reset to Day 1."
      )
    }
  }

  fun startNewExperiment() {
    _uiState.update { it.copy(showNewSheet = true) }
  }

  fun dismissNewExperiment() {
    _uiState.update { it.copy(showNewSheet = false) }
  }

  fun createNewExperiment(
    title: String,
    hypothesis: String,
    intervention: String,
    metric: String,
    durationDays: Int
  ) {
    repository.createNewExperiment(
      title = title,
      hypothesis = hypothesis,
      intervention = intervention,
      metric = metric,
      durationDays = durationDays
    )
    _uiState.update {
      it.copy(
        showNewSheet = false,
        feedbackToast = "New single experiment initiated."
      )
    }
  }

  fun dismissToast() {
    _uiState.update { it.copy(feedbackToast = null) }
  }
}
