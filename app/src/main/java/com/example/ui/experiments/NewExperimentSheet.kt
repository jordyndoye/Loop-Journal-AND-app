package com.example.ui.experiments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun NewExperimentSheet(
  onDismiss: () -> Unit,
  onCreate: (title: String, hypothesis: String, intervention: String, metric: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  var title by remember { mutableStateOf("") }
  var hypothesis by remember { mutableStateOf("") }
  var intervention by remember { mutableStateOf("") }
  var metric by remember { mutableStateOf("") }

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
    modifier = Modifier.testTag("new_experiment_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding()
    ) {
      Text(
        text = "NEW HYPOTHESIS // ONE VARIABLE ONLY",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.3.sp,
        color = BoothDim
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Initiate Single Experiment",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = BoothPaper
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Title
      Text("EXPERIMENT TITLE", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp, color = BoothDim)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g. 21:30 Hard Stop Buffer", color = BoothDim) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BoothPaper, unfocusedTextColor = BoothPaper, focusedContainerColor = BoothSurface, unfocusedContainerColor = BoothSurface, focusedBorderColor = BoothAmber, unfocusedBorderColor = BoothBorder, cursorColor = BoothAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("input_new_exp_title")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Hypothesis
      Text("HYPOTHESIS", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp, color = BoothDim)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = hypothesis,
        onValueChange = { hypothesis = it },
        placeholder = { Text("If X condition is set, Y breakpoint will be buffered.", color = BoothDim) },
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BoothPaper, unfocusedTextColor = BoothPaper, focusedContainerColor = BoothSurface, unfocusedContainerColor = BoothSurface, focusedBorderColor = BoothAmber, unfocusedBorderColor = BoothBorder, cursorColor = BoothAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("input_new_exp_hypothesis")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Single Intervention
      Text("SINGLE INTERVENTION (ONE VARIABLE)", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp, color = BoothDim)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = intervention,
        onValueChange = { intervention = it },
        placeholder = { Text("Exact mechanical change to test.", color = BoothDim) },
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BoothPaper, unfocusedTextColor = BoothPaper, focusedContainerColor = BoothSurface, unfocusedContainerColor = BoothSurface, focusedBorderColor = BoothAmber, unfocusedBorderColor = BoothBorder, cursorColor = BoothAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("input_new_exp_intervention")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Metric
      Text("METRIC TO WATCH", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp, color = BoothDim)
      Spacer(modifier = Modifier.height(4.dp))
      OutlinedTextField(
        value = metric,
        onValueChange = { metric = it },
        placeholder = { Text("e.g. Bedtime spine arrival, journal energy rating.", color = BoothDim) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BoothPaper, unfocusedTextColor = BoothPaper, focusedContainerColor = BoothSurface, unfocusedContainerColor = BoothSurface, focusedBorderColor = BoothAmber, unfocusedBorderColor = BoothBorder, cursorColor = BoothAmber),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("input_new_exp_metric")
      )

      Spacer(modifier = Modifier.height(22.dp))

      Button(
        onClick = {
          if (title.isNotBlank() && intervention.isNotBlank()) {
            onCreate(title, hypothesis, intervention, metric)
          }
        },
        enabled = title.isNotBlank() && intervention.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = BoothAmber, contentColor = BoothInk, disabledContainerColor = BoothSurface, disabledContentColor = BoothDim),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_confirm_new_experiment")
      ) {
        Text("Start 7-Day Cycle", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
