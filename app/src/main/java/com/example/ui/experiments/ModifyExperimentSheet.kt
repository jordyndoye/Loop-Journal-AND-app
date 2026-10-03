package com.example.ui.experiments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Experiment
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyExperimentSheet(
  experiment: Experiment,
  onDismiss: () -> Unit,
  onConfirm: (modifiedIntervention: String, notes: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  ModifyExperimentSheet(
    title = experiment.title,
    number = experiment.number,
    currentIntervention = experiment.singleIntervention,
    currentNotes = experiment.observationNotes,
    onDismiss = onDismiss,
    onConfirm = onConfirm,
    sheetState = sheetState
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyExperimentSheet(
  title: String,
  number: Int,
  currentIntervention: String,
  currentNotes: String,
  onDismiss: () -> Unit,
  onConfirm: (modifiedIntervention: String, notes: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  var intervention by remember(currentIntervention) { mutableStateOf(currentIntervention) }
  var notes by remember(currentNotes) { mutableStateOf(currentNotes) }

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
    modifier = Modifier.testTag("modify_experiment_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding()
    ) {
      Text(
        text = "CYCLE ADJUSTMENT // EXPERIMENT #$number",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.3.sp,
        color = BoothDim
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Modify Single Intervention",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = BoothPaper
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Adjust the variable parameters and record changes.",
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        color = BoothDim
      )

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "MODIFIED INTERVENTION",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = intervention,
        onValueChange = { intervention = it },
        minLines = 2,
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
          .testTag("input_modify_intervention")
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "REASON FOR ADJUSTMENT",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        placeholder = { Text("e.g. 21:30 was too aggressive; testing 21:45 instead.", color = BoothDim) },
        minLines = 2,
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
          .testTag("input_modify_notes")
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = {
          if (intervention.isNotBlank()) {
            onConfirm(intervention, notes)
          }
        },
        enabled = intervention.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = BoothAmber,
          contentColor = BoothInk,
          disabledContainerColor = BoothSurface,
          disabledContentColor = BoothDim
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_confirm_modify_experiment")
      ) {
        Text(
          text = "Commit Modification",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
