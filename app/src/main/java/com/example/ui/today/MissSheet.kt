package com.example.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DayBlock
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothBorderSubtle
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothMissedRing
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated

/**
 * Common causes reflecting real daytime conditions:
 * - Late work
 * - Partner evening that stretched bedtime
 * - Training after short sleep
 * - Heavy meal or skipped meal
 * - Low energy / schedule friction
 */
val CauseOptions = listOf(
  "Late work",
  "Partner evening",
  "Short sleep",
  "Heavy meal",
  "Skipped meal",
  "Schedule overrun",
  "Low energy",
  "Friction / Context switch"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissSheet(
  block: DayBlock,
  onDismiss: () -> Unit,
  onConfirmMiss: (cause: String, note: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  var selectedCause by remember(block) { mutableStateOf(block.cause ?: CauseOptions.first()) }
  var noteText by remember(block) { mutableStateOf(block.note ?: "") }

  ModalBottomSheet(
    onDismissRequest = {
      onConfirmMiss(selectedCause, noteText.trim())
    },
    sheetState = sheetState,
    containerColor = BoothSurfaceElevated,
    contentColor = BoothPaper,
    scrimColor = BoothBlack.copy(alpha = 0.75f),
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 8.dp)
          .size(width = 38.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(BoothDim)
      )
    },
    modifier = Modifier.testTag("miss_sheet")
  ) {
    MissSheetContent(
      block = block,
      selectedCause = selectedCause,
      onCauseSelected = { selectedCause = it },
      noteText = noteText,
      onNoteChange = { noteText = it },
      onKeepTapeRolling = {
        onConfirmMiss(selectedCause, noteText.trim())
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissSheetContent(
  block: DayBlock,
  selectedCause: String,
  onCauseSelected: (String) -> Unit,
  noteText: String,
  onNoteChange: (String) -> Unit,
  onKeepTapeRolling: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp)
      .navigationBarsPadding()
      .testTag("miss_sheet_content")
  ) {
    // Top Context Row: Time + Block Title + Empty Graphite Ring
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Missed indicator: Strictly empty graphite ring, NEVER red
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(2.dp, BoothMissedRing, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(BoothMissedGraphite)
          )
        }

        Text(
          text = "${block.intendedTime} // BREAKPOINT",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothDim
        )
      }

      Text(
        text = "RECORD TAPE",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothAmber
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Title in Serif
    Text(
      text = "What got in the way?",
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 30.sp,
      color = BoothPaper,
      modifier = Modifier.testTag("miss_sheet_title")
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Block: ${block.title}. Log what happened without judgment. The day is still captured.",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Normal,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Cause Chips Section Header
    Text(
      text = "PRIMARY CAUSE",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.sp,
      letterSpacing = 1.2.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Cause Chips FlowRow
    FlowRow(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("cause_chips_group"),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      CauseOptions.forEach { cause ->
        val isSelected = cause == selectedCause
        CauseChip(
          text = cause,
          isSelected = isSelected,
          onClick = { onCauseSelected(cause) }
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // One-line note field
    Text(
      text = "ONE-LINE OBSERVATION",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 11.sp,
      letterSpacing = 1.2.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = noteText,
      onValueChange = onNoteChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("miss_note_input"),
      placeholder = {
        Text(
          text = "e.g., meeting stretched 40m, skipped meal beforehand",
          fontFamily = FontFamily.SansSerif,
          fontSize = 13.sp,
          color = BoothDim
        )
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = BoothPaper,
        unfocusedTextColor = BoothPaper,
        focusedContainerColor = BoothSurface,
        unfocusedContainerColor = BoothSurface,
        focusedBorderColor = BoothAmber,
        unfocusedBorderColor = BoothBorder,
        cursorColor = BoothAmber
      ),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Primary Action Button: "Keep the tape rolling"
    Button(
      onClick = onKeepTapeRolling,
      colors = ButtonDefaults.buttonColors(
        containerColor = BoothAmber,
        contentColor = BoothInk
      ),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("btn_keep_tape_rolling")
    ) {
      Text(
        text = "Keep the tape rolling",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp
      )
    }

    Spacer(modifier = Modifier.height(14.dp))
  }
}

@Composable
private fun CauseChip(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val bg = if (isSelected) BoothAmber.copy(alpha = 0.18f) else BoothSurface
  val border = if (isSelected) BoothAmber else BoothBorder
  val fg = if (isSelected) BoothAmber else BoothPaper

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bg)
      .border(1.dp, border, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp)
      .testTag("chip_$text"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontFamily = FontFamily.SansSerif,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
      fontSize = 12.sp,
      color = fg
    )
  }
}
