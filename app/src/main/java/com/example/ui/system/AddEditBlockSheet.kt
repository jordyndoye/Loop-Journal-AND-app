package com.example.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnchorType
import com.example.domain.model.DayBlock
import com.example.ui.components.BoothTimePickerDialog
import com.example.util.ReminderUtils
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBlockSheet(
  block: DayBlock?,
  isNew: Boolean,
  onDismiss: () -> Unit,
  onSave: (id: String?, title: String, time: String, anchorType: AnchorType, note: String?, remindMe: Boolean) -> Unit,
  onDelete: (id: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  var title by remember(block) { mutableStateOf(block?.title ?: "") }
  var time by remember(block) { mutableStateOf(block?.intendedTime ?: "09:00") }
  var anchorType by remember(block) { mutableStateOf(block?.anchorType ?: AnchorType.HARD) }
  var note by remember(block) { mutableStateOf(block?.note ?: "") }
  var remindMe by remember(block) { mutableStateOf(block?.remindMe ?: true) }
  var showTimePicker by remember { mutableStateOf(false) }

  val isSpine = block?.anchorType?.isSpine == true

  ModalBottomSheet(
    onDismissRequest = onDismiss,
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
    modifier = Modifier.testTag("add_edit_block_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding()
    ) {
      // Header context
      Text(
        text = if (isNew) "NEW ROUTINE ANCHOR" else "EDIT ROUTINE ANCHOR",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.3.sp,
        color = BoothDim
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = if (isNew) "Add to Board" else "Configure Block",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = BoothPaper
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Block Title
      Text(
        text = "BLOCK TITLE",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g. Deep Work / Nutrition / Training", color = BoothDim) },
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
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_block_title")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Intended Time (24H) - The row still shows the time as HH:mm. The row is the button.
      Text(
        text = "INTENDED TIME (24H)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(BoothSurface)
          .border(1.dp, BoothAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
          .clickable { showTimePicker = true }
          .padding(horizontal = 14.dp, vertical = 14.dp)
          .testTag("input_block_time")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Schedule,
              contentDescription = "Select Time",
              tint = BoothAmber,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = time,
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp,
              letterSpacing = 0.5.sp,
              color = BoothPaper
            )
          }
          Text(
            text = "TAP TO SET TIME",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp,
            color = BoothAmber
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Anchor Type Selection
      Text(
        text = "ANCHOR RIGIDITY",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(6.dp))

      if (isSpine) {
        // Spine blocks (Wake and Sleep) are immutable in their anchor type
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(BoothAmber.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, BoothAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp)
        ) {
          Text(
            text = "SPINE ANCHOR (${anchorType.displayName}) • Wake and Sleep form the immutable spine of your operating system.",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = BoothAmber
          )
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(AnchorType.HARD, AnchorType.FLEXIBLE, AnchorType.OPTIONAL).forEach { type ->
            val isSelected = anchorType == type
            val bg = if (isSelected) BoothAmber.copy(alpha = 0.2f) else BoothSurface
            val border = if (isSelected) BoothAmber else BoothBorder
            val fg = if (isSelected) BoothAmber else BoothPaper

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(8.dp))
                .clickable { anchorType = type }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = type.displayName.replace(" Anchor", ""),
                fontFamily = FontFamily.SansSerif,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.sp,
                color = fg
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Context note
      Text(
        text = "REALITY CONTEXT (OPTIONAL)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        placeholder = { Text("e.g. Partner evening, training volume after short sleep", color = BoothDim) },
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
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_block_note")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Remind Me Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(BoothSurface)
          .border(1.dp, BoothBorder, RoundedCornerShape(8.dp))
          .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
          Text(
            text = "REMIND ME",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = BoothPaper
          )
          Spacer(modifier = Modifier.height(2.dp))
          val questionPreview = ReminderUtils.generateReminderQuestion(title.ifBlank { "this block" }, anchorType)
          Text(
            text = if (remindMe) "Quiet reminder: \"$questionPreview\"" else "Off — never notify",
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            color = if (remindMe) BoothAmber else BoothDim
          )
        }

        Switch(
          checked = remindMe,
          onCheckedChange = { remindMe = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = BoothInk,
            checkedTrackColor = BoothAmber,
            uncheckedThumbColor = BoothDim,
            uncheckedTrackColor = BoothBorder
          ),
          modifier = Modifier.testTag("switch_remind_me")
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (!isNew && !isSpine && block != null) {
          OutlinedButton(
            onClick = { onDelete(block.id) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BoothMissedGraphite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BoothBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(48.dp)
              .testTag("btn_delete_anchor")
          ) {
            Icon(
              imageVector = Icons.Outlined.Delete,
              contentDescription = "Delete",
              tint = BoothDim,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Button(
          onClick = {
            if (title.isNotBlank()) {
              onSave(block?.id, title, time, anchorType, note, remindMe)
            }
          },
          enabled = title.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = BoothAmber,
            contentColor = BoothInk,
            disabledContainerColor = BoothSurface,
            disabledContentColor = BoothDim
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_save_anchor")
        ) {
          Text(
            text = if (isNew) "Add to Board" else "Save Anchor",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }

  if (showTimePicker) {
    BoothTimePickerDialog(
      initialTime = time,
      title = if (isNew) "SET INTENDED TIME" else "EDIT TIME // ${title.ifBlank { "ANCHOR" }}",
      onDismiss = { showTimePicker = false },
      onConfirm = { chosenTime ->
        time = chosenTime
        showTimePicker = false
      }
    )
  }
}
