package com.example.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.JournalRepository
import com.example.data.repository.RoutineRepository
import com.example.domain.model.BlockStatus
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JournalViewModel(
  private val repository: RoutineRepository = RoutineRepository.instance,
  private val journalRepository: JournalRepository? = repository.journalRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(buildInitialState())
  val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

  private fun buildInitialState(): JournalUiState {
    val todayRec = repository.todayRecord.value
    val blocks = repository.blocks.value
    val done = blocks.filter { it.status == BlockStatus.DONE || it.status == BlockStatus.MODIFIED }
    val missed = blocks.filter { it.status == BlockStatus.MISSED || it.status == BlockStatus.SKIP }
    val pending = blocks.filter { it.status == BlockStatus.PENDING }
    return JournalUiState(
      formattedDate = DateTimeUtils.formatHeaderDate(repository.activeDateIso.value),
      todayRecord = todayRec,
      pastRecords = emptyList(),
      todayBlocks = blocks,
      doneBlocks = done,
      missedBlocks = missed,
      pendingBlocks = pending,
      nightNote = todayRec.playbackNote,
      wentToPlanInput = todayRec.wentToPlan,
      didNotGoToPlanInput = todayRec.didNotGoToPlan,
      inTheWayInput = todayRec.inTheWay,
      wakeEnergy = todayRec.wakeEnergy,
      wakeMood = todayRec.wakeMood,
      wakeStress = todayRec.wakeStress,
      sleepEnergy = todayRec.sleepEnergy,
      sleepMood = todayRec.sleepMood,
      sleepStress = todayRec.sleepStress,
      isTapeSealed = todayRec.isClosed,
      isCaptured = blocks.any { it.status != BlockStatus.PENDING }
    )
  }

  init {
    viewModelScope.launch {
      combine(
        repository.todayRecord,
        repository.pastRecords,
        repository.blocks
      ) { todayRec, _, blocks ->
        Pair(todayRec, blocks)
      }.collect { (todayRec, blocks) ->
        val done = blocks.filter { it.status == BlockStatus.DONE || it.status == BlockStatus.MODIFIED }
        val missed = blocks.filter { it.status == BlockStatus.MISSED || it.status == BlockStatus.SKIP }
        val pending = blocks.filter { it.status == BlockStatus.PENDING }
        _uiState.update { current ->
          current.copy(
            formattedDate = DateTimeUtils.formatHeaderDate(repository.activeDateIso.value),
            todayRecord = todayRec,
            pastRecords = emptyList(),
            todayBlocks = blocks,
            doneBlocks = done,
            missedBlocks = missed,
            pendingBlocks = pending,
            isTapeSealed = todayRec.isClosed,
            isCaptured = blocks.any { it.status != BlockStatus.PENDING },
            nightNote = if (current.nightNote.isBlank()) todayRec.playbackNote else current.nightNote,
            wentToPlanInput = if (current.wentToPlanInput.isBlank()) todayRec.wentToPlan else current.wentToPlanInput,
            didNotGoToPlanInput = if (current.didNotGoToPlanInput.isBlank()) todayRec.didNotGoToPlan else current.didNotGoToPlanInput,
            inTheWayInput = if (current.inTheWayInput.isBlank()) todayRec.inTheWay else current.inTheWayInput,
            wakeEnergy = todayRec.wakeEnergy,
            wakeMood = todayRec.wakeMood,
            wakeStress = todayRec.wakeStress,
            sleepEnergy = todayRec.sleepEnergy,
            sleepMood = todayRec.sleepMood,
            sleepStress = todayRec.sleepStress
          )
        }
      }
    }
  }

  fun setNightNote(text: String) {
    _uiState.update { it.copy(nightNote = text) }
    repository.updateNightNote(text)
  }

  fun onScreenExit() {
    // If a night note exists, it is already on that date. Leaving the screen saves it.
    repository.updateNightNote(_uiState.value.nightNote)
  }

  fun setWentToPlan(text: String) {
    _uiState.update { it.copy(wentToPlanInput = text) }
  }

  fun setDidNotGoToPlan(text: String) {
    _uiState.update { it.copy(didNotGoToPlanInput = text) }
  }

  fun setInTheWay(text: String) {
    _uiState.update { it.copy(inTheWayInput = text) }
  }

  fun commitNightClose() {
    val state = _uiState.value
    repository.commitNightClose(
      wentToPlan = state.wentToPlanInput,
      didNotGoToPlan = state.didNotGoToPlanInput,
      inTheWay = state.inTheWayInput,
      energy = 0f,
      stress = 0f
    )
    _uiState.update {
      it.copy(
        isTapeSealed = true,
        showCommitConfirmation = true
      )
    }
  }

  fun dismissConfirmation() {
    _uiState.update { it.copy(showCommitConfirmation = false) }
  }

  fun saveDailyObservation(
    date: String = repository.activeDateIso.value,
    reflection: String = _uiState.value.wentToPlanInput,
    breakPoints: String = _uiState.value.inTheWayInput
  ) {
    viewModelScope.launch {
      journalRepository?.saveJournalEntry(
        date = date,
        reflection = reflection,
        breakPoints = breakPoints
      )
    }
  }

  fun getJournalRepository(): JournalRepository? = journalRepository
}
