package com.example.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsightsViewModel(
  private val repository: RoutineRepository = RoutineRepository.instance
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    InsightsUiState(synthesis = repository.tapeSynthesis.value)
  )
  val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      repository.tapeSynthesis.collect { synthesis ->
        _uiState.update { it.copy(synthesis = synthesis) }
      }
    }
  }

  fun promoteSuggestedExperiment() {
    val suggestion = _uiState.value.synthesis?.oneSuggestedExperiment
    if (suggestion != null) {
      repository.promoteSuggestionToExperiment(suggestion)
      _uiState.update {
        it.copy(
          isExperimentPromoted = true,
          feedbackMessage = "Experiment #${suggestion.number} staged into Experiments tab."
        )
      }
    }
  }

  fun dismissFeedback() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }
}
