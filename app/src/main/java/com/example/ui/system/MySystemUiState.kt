package com.example.ui.system

import com.example.domain.model.DayBlock

data class MySystemUiState(
  val blocks: List<DayBlock> = emptyList(),
  val editingBlock: DayBlock? = null,
  val isAddingNewBlock: Boolean = false,
  val selectedAnchorFilter: String = "ALL"
)
