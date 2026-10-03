package com.example.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.RoutineRepository
import com.example.domain.model.AnchorType
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MySystemViewModel(
  private val repository: RoutineRepository = RoutineRepository.instance
) : ViewModel() {

  private val _uiState = MutableStateFlow(MySystemUiState())
  val uiState: StateFlow<MySystemUiState> = _uiState.asStateFlow()

  init {
    _uiState.update { it.copy(blocks = repository.blocks.value) }
    viewModelScope.launch {
      repository.blocks.collect { blocks ->
        _uiState.update { it.copy(blocks = blocks) }
      }
    }
  }

  fun setFilter(filter: String) {
    _uiState.update { it.copy(selectedAnchorFilter = filter) }
  }

  fun startAddBlock() {
    _uiState.update { it.copy(isAddingNewBlock = true, editingBlock = null) }
  }

  fun startEditBlock(block: DayBlock) {
    _uiState.update { it.copy(editingBlock = block, isAddingNewBlock = false) }
  }

  fun dismissSheet() {
    _uiState.update { it.copy(editingBlock = null, isAddingNewBlock = false) }
  }

  fun saveBlock(
    id: String?,
    title: String,
    time: String,
    anchorType: AnchorType,
    note: String?,
    remindMe: Boolean = true
  ) {
    if (id == null) {
      // Create new block
      val newBlock = DayBlock(
        id = "b_${UUID.randomUUID().toString().take(8)}",
        title = title.trim(),
        intendedTime = time.trim(),
        anchorType = anchorType,
        status = BlockStatus.PENDING,
        note = note?.trim()?.ifBlank { null },
        remindMe = remindMe
      )
      repository.addBlock(newBlock)
    } else {
      // Update existing
      val existing = repository.systemBlocks.value.find { it.id == id } ?: repository.blocks.value.find { it.id == id }
      if (existing != null) {
        val updated = existing.copy(
          title = title.trim(),
          intendedTime = time.trim(),
          plannedTime = time.trim(),
          anchorType = anchorType,
          note = note?.trim()?.ifBlank { null },
          remindMe = remindMe
        )
        repository.updateBlock(updated)
      }
    }
    dismissSheet()
  }

  fun toggleBlockReminder(blockId: String, remindMe: Boolean) {
    repository.toggleBlockReminder(blockId, remindMe)
  }

  fun deleteBlock(blockId: String) {
    repository.deleteBlock(blockId)
    dismissSheet()
  }
}
