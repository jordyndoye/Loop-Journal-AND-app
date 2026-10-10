package com.example.ui.today

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.BlockStatus
import com.example.domain.model.DayBlock
import com.example.ui.components.WeekReelBar
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
  modifier: Modifier = Modifier,
  viewModel: TodayViewModel = viewModel(),
  onNavigateToSystem: (() -> Unit)? = null
) {
  val uiState by viewModel.uiState.collectAsState()
  var expandedBlockId by remember { mutableStateOf<String?>(null) }
  var activeSpineRatingsBlock by remember { mutableStateOf<DayBlock?>(null) }
  val context = LocalContext.current

  // Open the database safely only after Today is visible
  LaunchedEffect(Unit) {
    withContext(Dispatchers.IO) {
      val db = com.example.data.local.AppDatabase.openSafely(context)
      if (db != null) {
        com.example.data.service.CloudMirror.restoreInto(db.dayDao())
        com.example.data.repository.RoutineRepository.instance.initRoom(db.dayDao())
        com.example.data.repository.RoutineRepository.instance.checkAndTriggerSilentInsightsDraft()
      }
    }
  }

  // The next unmarked block is the only row with a quiet amber edge
  val nextUnmarkedBlock = remember(uiState.blocks) {
    findNextUnmarkedBlock(uiState.blocks)
  }
  val nextUnmarkedBlockId = nextUnmarkedBlock?.id

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .testTag("today_screen")
  ) {
    // Header: weekday and date only
    TodayHeader(formattedDate = uiState.formattedHeaderDate)

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // 7-Day Reel (quiet, no "0/7 CAPTURED" or "REEL // 7-DAY TAPE")
      item {
        WeekReelBar(
          days = uiState.weekReel,
          selectedDateIso = uiState.dateIso,
          onDaySelected = { viewModel.selectDay(it) },
          modifier = Modifier.padding(bottom = 10.dp)
        )
      }

      // One column of blocks: each block is one row
      items(items = uiState.blocks, key = { it.id }) { block ->
        val isExpanded = (expandedBlockId == block.id)
        val isNextUnmarked = (block.id == nextUnmarkedBlockId)

        TakeSheetBlockRow(
          block = block,
          isNextUnmarked = isNextUnmarked,
          isExpanded = isExpanded,
          onRowClick = {
            expandedBlockId = if (isExpanded) null else block.id
          },
          onStatusSelected = { status ->
            expandedBlockId = null
            if (status == BlockStatus.MISSED) {
              viewModel.openMissSheet(block)
            } else {
              viewModel.setBlockStatus(block.id, status)
              if (status == BlockStatus.DONE || status == BlockStatus.MODIFIED) {
                val isWake = block.anchorType == com.example.domain.model.AnchorType.SPINE_WAKE ||
                  block.title.contains("Wake", ignoreCase = true)
                val isSleep = block.anchorType == com.example.domain.model.AnchorType.SPINE_SLEEP ||
                  block.title.contains("Sleep", ignoreCase = true)
                if (isWake || isSleep) {
                  activeSpineRatingsBlock = block
                }
              }
            }
          }
        )
      }
    }

    // Modal Miss Sheet for recording "What got in the way?"
    uiState.activeMissSheetBlock?.let { blockToMiss ->
      MissSheet(
        block = blockToMiss,
        onDismiss = { viewModel.dismissMissSheet() },
        onConfirmMiss = { cause, note ->
          viewModel.recordMissedBlock(blockToMiss.id, cause, note)
        }
      )
    }

    // Short Spine Ratings Sheet (Energy, Mood, Stress) for Wake & Sleep
    activeSpineRatingsBlock?.let { spineBlock ->
      SpineRatingsSheet(
        block = spineBlock,
        onDismiss = { activeSpineRatingsBlock = null },
        onConfirm = { energy, mood, stress ->
          viewModel.saveSpineReadings(spineBlock, energy, mood, stress)
          activeSpineRatingsBlock = null
        }
      )
    }
  }
}

/**
 * Finds the single next unmarked block according to current time and routine schedule:
 * - Past unmarked rows stay plain.
 * - Future unmarked rows beyond the next one stay plain.
 * - Only the next unmarked block gets the quiet amber edge.
 */
private fun findNextUnmarkedBlock(
  blocks: List<DayBlock>,
  currentTime: String = DateTimeUtils.currentTime24()
): DayBlock? {
  if (blocks.isEmpty()) return null

  // Index of the active block for the current phone time
  val currentIndex = blocks.indexOfLast { it.plannedTime <= currentTime }

  if (currentIndex == -1) {
    // Current time is before the first block: first unmarked block is the next one
    return blocks.firstOrNull { it.status == BlockStatus.PENDING }
  }

  // If the current block itself is unmarked, it is the next unmarked block
  val currentBlock = blocks[currentIndex]
  if (currentBlock.status == BlockStatus.PENDING) {
    return currentBlock
  }

  // If current block is already marked, find the first upcoming unmarked block
  return blocks.drop(currentIndex + 1).firstOrNull { it.status == BlockStatus.PENDING }
}

@Composable
private fun TodayHeader(
  formattedDate: String
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothBlack)
      .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
  ) {
    Text(
      text = formattedDate.ifBlank { "Today" },
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 26.sp,
      letterSpacing = 0.5.sp,
      color = BoothPaper
    )
  }
}

@Composable
private fun TakeSheetBlockRow(
  block: DayBlock,
  isNextUnmarked: Boolean,
  isExpanded: Boolean,
  onRowClick: () -> Unit,
  onStatusSelected: (BlockStatus) -> Unit
) {
  val isMarked = block.status != BlockStatus.PENDING

  // Border: quiet amber edge for the next unmarked block, plain for everything else
  val borderColor = if (isNextUnmarked) BoothAmber.copy(alpha = 0.75f) else BoothBorder

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(BoothSurface)
      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
      .clickable(onClick = onRowClick)
      .animateContentSize()
      .testTag("block_card_${block.id}")
  ) {
    // Single row: time, name, and small state mark
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 52.dp)
        .padding(horizontal = 14.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Time
        Text(
          text = block.plannedTime,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          letterSpacing = 0.5.sp,
          color = if (block.status == BlockStatus.DONE || isNextUnmarked) BoothAmber else BoothDim
        )

        // Block Name
        Text(
          text = block.title,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Normal,
          fontSize = 15.sp,
          color = BoothPaper,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Small state mark or marked word: Done, Modified, Missed, Skip, nothing else
      if (isMarked) {
        val (statusText, statusColor) = when (block.status) {
          BlockStatus.DONE -> "Done" to BoothAmber
          BlockStatus.MODIFIED -> "Modified" to BoothPaper
          BlockStatus.MISSED -> "Missed" to BoothDim
          BlockStatus.SKIP -> "Skip" to BoothDim
          BlockStatus.PENDING -> "" to BoothDim
        }
        Text(
          text = statusText,
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          letterSpacing = 0.3.sp,
          color = statusColor
        )
      } else {
        // Small state mark for unmarked block (subtle quiet ring)
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .border(
              width = if (isNextUnmarked) 1.5.dp else 1.dp,
              color = if (isNextUnmarked) BoothAmber else BoothBorder,
              shape = CircleShape
            )
        )
      }
    }

    // Tapping a row opens the four choices: Done, Modified, Missed, Skip.
    // Those choices are paper-coloured and readable, not ghosted.
    if (isExpanded) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PaperChoiceButton(
          label = "Done",
          isSelected = block.status == BlockStatus.DONE,
          onClick = { onStatusSelected(BlockStatus.DONE) },
          modifier = Modifier.weight(1f),
          testTag = "btn_done_${block.id}"
        )

        PaperChoiceButton(
          label = "Modified",
          isSelected = block.status == BlockStatus.MODIFIED,
          onClick = { onStatusSelected(BlockStatus.MODIFIED) },
          modifier = Modifier.weight(1f),
          testTag = "btn_mod_${block.id}"
        )

        PaperChoiceButton(
          label = "Missed",
          isSelected = block.status == BlockStatus.MISSED,
          onClick = { onStatusSelected(BlockStatus.MISSED) },
          modifier = Modifier.weight(1f),
          testTag = "btn_missed_${block.id}"
        )

        PaperChoiceButton(
          label = "Skip",
          isSelected = block.status == BlockStatus.SKIP,
          onClick = { onStatusSelected(BlockStatus.SKIP) },
          modifier = Modifier.weight(1f),
          testTag = "btn_skip_${block.id}"
        )
      }
    }
  }
}

/**
 * Large tap target choice button: 48dp height, paper-coloured and readable, not ghosted.
 */
@Composable
private fun PaperChoiceButton(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String
) {
  val borderColor = if (isSelected) BoothAmber else BoothBorder
  val backgroundColor = if (isSelected) BoothAmber.copy(alpha = 0.15f) else BoothSurfaceElevated

  Box(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontFamily = FontFamily.SansSerif,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
      fontSize = 12.sp,
      letterSpacing = 0.4.sp,
      color = BoothPaper,
      maxLines = 1,
      softWrap = false
    )
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun AnchorBadge(anchorType: com.example.domain.model.AnchorType) {
  val (label, bg, fg, border) = when (anchorType) {
    com.example.domain.model.AnchorType.SPINE_WAKE -> Quad("SPINE : WAKE", BoothAmber.copy(alpha = 0.15f), BoothAmber, BoothAmber.copy(alpha = 0.4f))
    com.example.domain.model.AnchorType.SPINE_SLEEP -> Quad("SPINE : SLEEP", BoothAmber.copy(alpha = 0.15f), BoothAmber, BoothAmber.copy(alpha = 0.4f))
    com.example.domain.model.AnchorType.HARD -> Quad("HARD ANCHOR", BoothSurfaceElevated, BoothPaper, BoothBorder)
    com.example.domain.model.AnchorType.FLEXIBLE -> Quad("FLEXIBLE", BoothSurfaceElevated, BoothDim, com.example.ui.theme.BoothBorderSubtle)
    com.example.domain.model.AnchorType.OPTIONAL -> Quad("OPTIONAL", androidx.compose.ui.graphics.Color.Transparent, BoothDim, com.example.ui.theme.BoothBorderSubtle)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(bg)
      .border(0.8.dp, border, RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = label,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 9.sp,
      letterSpacing = 0.8.sp,
      color = fg
    )
  }
}
