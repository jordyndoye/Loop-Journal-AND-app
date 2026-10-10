package com.example.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.AnchorType
import com.example.domain.model.DayBlock
import com.example.ui.components.BoothTimePickerDialog
import com.example.ui.today.AnchorBadge
import com.example.util.ReminderUtils
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothBorderSubtle
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySystemScreen(
  modifier: Modifier = Modifier,
  viewModel: MySystemViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  var blockForTimePicker by remember { mutableStateOf<DayBlock?>(null) }

  val filteredBlocks = when (uiState.selectedAnchorFilter) {
    "SPINE" -> uiState.blocks.filter { it.anchorType.isSpine }
    "HARD" -> uiState.blocks.filter { it.anchorType == AnchorType.HARD }
    "FLEXIBLE" -> uiState.blocks.filter { it.anchorType == AnchorType.FLEXIBLE }
    "OPTIONAL" -> uiState.blocks.filter { it.anchorType == AnchorType.OPTIONAL }
    else -> uiState.blocks
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .testTag("my_system_screen")
  ) {
    // Top System Bar
    SystemTopBar(
      onAddBlock = { viewModel.startAddBlock() }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // System Thesis Card
      item {
        SpineThesisCard()
      }

      // Anchor Filter Row
      item {
        AnchorFilterBar(
          selectedFilter = uiState.selectedAnchorFilter,
          onFilterSelected = { viewModel.setFilter(it) },
          totalCount = uiState.blocks.size,
          spineCount = uiState.blocks.count { it.anchorType.isSpine },
          hardCount = uiState.blocks.count { it.anchorType == AnchorType.HARD },
          flexibleCount = uiState.blocks.count { it.anchorType == AnchorType.FLEXIBLE }
        )
      }

      // Section Subtitle
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "THE BOARD LIST // ANCHOR RAIL",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.3.sp,
            color = BoothDim
          )

          Text(
            text = "${filteredBlocks.size} BLOCKS",
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            color = BoothAmber
          )
        }
      }

      // Board blocks along the spine rail or empty state
      if (filteredBlocks.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(BoothSurface, RoundedCornerShape(10.dp))
              .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "NO ANCHORS MATCH FILTER",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = BoothDim
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "No anchors match '${uiState.selectedAnchorFilter}'. Tap '+ ADD ANCHOR' above to create one.",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                color = BoothPaper,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(items = filteredBlocks, key = { it.id }) { block ->
          SystemBoardBlockRow(
            block = block,
            onEditBlock = { viewModel.startEditBlock(block) },
            onTimeClick = { blockForTimePicker = it },
            onToggleReminder = { viewModel.toggleBlockReminder(block.id, it) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = { FirebaseAuth.getInstance().signOut() },
          modifier = Modifier.fillMaxWidth().height(48.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BoothSurface,
            contentColor = BoothPaper
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, BoothBorder)
        ) {
          Text("Sign out")
        }
      }
    }

    // Direct TimePicker Dialog when tapping a block's time row/chip
    blockForTimePicker?.let { blockToEdit ->
      BoothTimePickerDialog(
        initialTime = blockToEdit.intendedTime,
        title = "INTENDED TIME // ${blockToEdit.title.uppercase()}",
        onDismiss = { blockForTimePicker = null },
        onConfirm = { chosenTime ->
          viewModel.saveBlock(
            id = blockToEdit.id,
            title = blockToEdit.title,
            time = chosenTime,
            anchorType = blockToEdit.anchorType,
            note = blockToEdit.note,
            remindMe = blockToEdit.remindMe
          )
          blockForTimePicker = null
        }
      )
    }

    // Modal Sheet for adding or editing routine block
    if (uiState.isAddingNewBlock || uiState.editingBlock != null) {
      AddEditBlockSheet(
        block = uiState.editingBlock,
        isNew = uiState.isAddingNewBlock,
        onDismiss = { viewModel.dismissSheet() },
        onSave = { id, title, time, anchorType, note, remindMe ->
          viewModel.saveBlock(id, title, time, anchorType, note, remindMe)
        },
        onDelete = { id ->
          viewModel.deleteBlock(id)
        }
      )
    }
  }
}

@Composable
private fun SystemTopBar(
  onAddBlock: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothBlack)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "OPERATING SYSTEM // BLUEPRINT",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "My System",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        letterSpacing = 0.5.sp,
        color = BoothPaper
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Add Anchor Button
    Button(
      onClick = onAddBlock,
      colors = ButtonDefaults.buttonColors(
        containerColor = BoothSurfaceElevated,
        contentColor = BoothAmber
      ),
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, BoothBorder),
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
      modifier = Modifier
        .defaultMinSize(minWidth = 1.dp, minHeight = 36.dp)
        .testTag("btn_add_system_anchor")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Anchor",
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "ADD ANCHOR",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 0.5.sp,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

@Composable
private fun SpineThesisCard() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
  ) {
    Column {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(BoothAmber)
        )
        Text(
          text = "SPINE ARCHITECTURE",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothAmber
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Wake and sleep form the continuous spine of the day. Hard anchors secure non-negotiable commitments. Flexible anchors absorb friction (late work, partner evenings, training on short sleep).",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = BoothPaper
      )
    }
  }
}

@Composable
private fun AnchorFilterBar(
  selectedFilter: String,
  onFilterSelected: (String) -> Unit,
  totalCount: Int,
  spineCount: Int,
  hardCount: Int,
  flexibleCount: Int
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    FilterPill("ALL", "All ($totalCount)", selectedFilter == "ALL") { onFilterSelected("ALL") }
    FilterPill("SPINE", "Spine ($spineCount)", selectedFilter == "SPINE") { onFilterSelected("SPINE") }
    FilterPill("HARD", "Hard ($hardCount)", selectedFilter == "HARD") { onFilterSelected("HARD") }
    FilterPill("FLEXIBLE", "Flexible ($flexibleCount)", selectedFilter == "FLEXIBLE") { onFilterSelected("FLEXIBLE") }
  }
}

@Composable
private fun FilterPill(
  id: String,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val bg = if (isSelected) BoothAmber.copy(alpha = 0.18f) else BoothSurface
  val border = if (isSelected) BoothAmber else BoothBorder
  val fg = if (isSelected) BoothAmber else BoothDim

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("filter_pill_$id")
  ) {
    Text(
      text = label,
      fontFamily = FontFamily.SansSerif,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
      fontSize = 11.sp,
      color = fg
    )
  }
}

@Composable
private fun SystemBoardBlockRow(
  block: DayBlock,
  onEditBlock: () -> Unit,
  onTimeClick: (DayBlock) -> Unit = {},
  onToggleReminder: (Boolean) -> Unit = {}
) {
  val isSpine = block.anchorType.isSpine

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("system_block_${block.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Spine rail node indicator on the left
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.width(28.dp)
    ) {
      Box(
        modifier = Modifier
          .size(if (isSpine) 14.dp else 8.dp)
          .clip(CircleShape)
          .background(if (isSpine) BoothAmber else BoothSurfaceElevated)
          .border(
            width = if (isSpine) 2.dp else 1.dp,
            color = if (isSpine) BoothPaper else BoothBorder,
            shape = CircleShape
          )
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // Main Card
    Box(
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(10.dp))
        .background(if (isSpine) BoothSurfaceElevated else BoothSurface)
        .border(
          width = 1.dp,
          color = if (isSpine) BoothAmber.copy(alpha = 0.4f) else BoothBorder,
          shape = RoundedCornerShape(10.dp)
        )
        .clickable(onClick = onEditBlock)
        .padding(14.dp)
    ) {
      Column {
        // Time + Badges + Switch + Edit Icon
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Clickable Time Chip - tapping opens TimePickerDialog directly
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSpine) BoothAmber.copy(alpha = 0.15f) else BoothSurfaceElevated)
                .border(
                  width = 1.dp,
                  color = if (isSpine) BoothAmber.copy(alpha = 0.6f) else BoothBorder,
                  shape = RoundedCornerShape(6.dp)
                )
                .clickable { onTimeClick(block) }
                .padding(horizontal = 7.dp, vertical = 3.dp)
                .testTag("time_chip_${block.id}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Schedule,
                  contentDescription = "Set time for ${block.title}",
                  tint = if (isSpine) BoothAmber else BoothPaper,
                  modifier = Modifier.size(13.dp)
                )
                Text(
                  text = block.intendedTime,
                  fontFamily = FontFamily.SansSerif,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = if (isSpine) BoothAmber else BoothPaper
                )
              }
            }

            AnchorBadge(anchorType = block.anchorType)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Switch(
              checked = block.remindMe,
              onCheckedChange = onToggleReminder,
              colors = SwitchDefaults.colors(
                checkedThumbColor = BoothInk,
                checkedTrackColor = BoothAmber,
                uncheckedThumbColor = BoothDim,
                uncheckedTrackColor = BoothBorder
              ),
              modifier = Modifier
                .scale(0.75f)
                .testTag("switch_remind_${block.id}")
            )

            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = "Edit Block",
              tint = BoothDim,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title in Serif
        Text(
          text = block.title,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Normal,
          fontSize = 16.sp,
          lineHeight = 22.sp,
          color = BoothPaper
        )

        // Reality context note if provided
        if (!block.note.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Context: ${block.note}",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = BoothDim
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Gentle reminder status text
        val reminderQuestion = ReminderUtils.generateReminderQuestion(block)
        Text(
          text = if (block.remindMe) "Quiet reminder: \"$reminderQuestion\"" else "Reminders: Off",
          fontFamily = FontFamily.SansSerif,
          fontSize = 11.sp,
          color = if (block.remindMe) BoothAmber.copy(alpha = 0.85f) else BoothDim
        )
      }
    }
  }
}
