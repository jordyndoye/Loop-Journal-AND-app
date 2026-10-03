package com.example.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RoutineRepository
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import com.example.ui.components.ReelDay
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodayViewModel(
  private val repository: RoutineRepository = RoutineRepository.instance
) : ViewModel() {

  private fun buildInitialState(): TodayUiState {
    val blocks = repository.blocks.value
    val reel = repository.weekReel.value
    val activeDate = repository.activeDateIso.value
    val todayReelDay = reel.firstOrNull { it.isToday }
    val currentBlockId = DateTimeUtils.findCurrentBlockId(blocks.map { it.id to it.intendedTime })
    val headerDate = DateTimeUtils.formatHeaderDate(activeDate)

    return TodayUiState(
      blocks = blocks,
      weekReel = reel,
      isTodayCaptured = todayReelDay?.isCaptured == true,
      selectedDay = todayReelDay,
      dateIso = activeDate,
      formattedHeaderDate = headerDate,
      currentBlockId = currentBlockId
    )
  }

  private val _uiState = MutableStateFlow(buildInitialState())
  val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

  init {
    repository.ensureToday()
    viewModelScope.launch {
      combine(repository.blocks, repository.weekReel, repository.activeDateIso) { blocks, reel, activeDate ->
        val todayReelDay = reel.firstOrNull { it.isToday }
        val currentBlockId = DateTimeUtils.findCurrentBlockId(blocks.map { it.id to it.intendedTime })
        val headerDate = DateTimeUtils.formatHeaderDate(activeDate)

        TodayUiState(
          blocks = blocks,
          weekReel = reel,
          isTodayCaptured = todayReelDay?.isCaptured == true,
          selectedDay = _uiState.value.selectedDay ?: todayReelDay,
          blockBeingEdited = _uiState.value.blockBeingEdited,
          activeMissSheetBlock = _uiState.value.activeMissSheetBlock,
          dateIso = activeDate,
          formattedHeaderDate = headerDate,
          currentBlockId = currentBlockId
        )
      }.collect { combinedState ->
        _uiState.value = combinedState
      }
    }
  }

  fun checkDayTransition() {
    if (repository.ensureToday()) {
      // Midnight passed, repository reloaded today's rows
    }
  }

  fun setBlockStatus(blockId: String, status: BlockStatus, note: String? = null, cause: String? = null) {
    repository.updateBlockStatus(blockId, status, note, cause)
    _uiState.update { it.copy(blockBeingEdited = null) }
  }

  fun openMissSheet(block: DayBlock) {
    _uiState.update { it.copy(activeMissSheetBlock = block) }
  }

  fun dismissMissSheet() {
    _uiState.update { it.copy(activeMissSheetBlock = null) }
  }

  fun recordMissedBlock(blockId: String, cause: String, note: String) {
    repository.updateBlockStatus(
      blockId = blockId,
      newStatus = BlockStatus.MISSED,
      note = note.ifBlank { null },
      cause = cause.ifBlank { null }
    )
    _uiState.update { it.copy(activeMissSheetBlock = null) }
  }

  fun selectDay(day: ReelDay) {
    _uiState.update { it.copy(selectedDay = day) }
    if (day.dateIso.isNotBlank()) {
      repository.loadDay(day.dateIso)
    }
  }

  fun selectBlockForEditing(block: DayBlock?) {
    _uiState.update { it.copy(blockBeingEdited = block) }
  }

  fun saveSpineReadings(block: DayBlock, energy: Int?, mood: Int?, stress: Int?) {
    val isWake = block.anchorType == com.example.domain.model.AnchorType.SPINE_WAKE ||
      block.title.contains("Wake", ignoreCase = true)
    if (isWake) {
      repository.saveWakeReadings(energy, mood, stress)
    } else {
      repository.saveSleepReadings(energy, mood, stress)
    }
  }
}
