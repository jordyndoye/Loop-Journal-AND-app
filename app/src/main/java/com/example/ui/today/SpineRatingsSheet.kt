package com.example.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.AnchorType
import com.example.domain.model.DayBlock
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
fun SpineRatingsSheet(
  block: DayBlock,
  onDismiss: () -> Unit,
  onConfirm: (energy: Int?, mood: Int?, stress: Int?) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val isWake = block.anchorType == AnchorType.SPINE_WAKE || block.title.contains("Wake", ignoreCase = true)
  val title = if (isWake) "WAKE RATINGS" else "BEFORE SLEEP RATINGS"
  val subtitle = if (isWake) "Morning reading" else "Evening wind-down reading"

  var selectedEnergy by remember { mutableStateOf<Int?>(null) }
  var selectedMood by remember { mutableStateOf<Int?>(null) }
  var selectedStress by remember { mutableStateOf<Int?>(null) }

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
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .navigationBarsPadding()
        .padding(bottom = 20.dp)
        .testTag("spine_ratings_sheet")
    ) {
      Text(
        text = title,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.2.sp,
        color = BoothAmber
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        fontFamily = FontFamily.Serif,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = BoothPaper
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Energy 1..5
      RatingScaleRow(
        metricLabel = "Energy",
        lowLabel = "1 Low",
        highLabel = "5 High",
        selectedValue = selectedEnergy,
        onValueSelected = { selectedEnergy = if (selectedEnergy == it) null else it },
        testTagPrefix = "rating_energy"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Mood 1..5
      RatingScaleRow(
        metricLabel = "Mood",
        lowLabel = "1 Heavy",
        highLabel = "5 Sharp",
        selectedValue = selectedMood,
        onValueSelected = { selectedMood = if (selectedMood == it) null else it },
        testTagPrefix = "rating_mood"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Stress 1..5
      RatingScaleRow(
        metricLabel = "Stress",
        lowLabel = "1 Calm",
        highLabel = "5 High",
        selectedValue = selectedStress,
        onValueSelected = { selectedStress = if (selectedStress == it) null else it },
        testTagPrefix = "rating_stress"
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Actions: Confirm & Skip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Skip Button
        TextButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("btn_skip_spine_ratings")
        ) {
          Text(
            text = "Skip",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = BoothDim
          )
        }

        // Confirm Button
        Button(
          onClick = {
            onConfirm(selectedEnergy, selectedMood, selectedStress)
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = BoothAmber,
            contentColor = BoothInk
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_confirm_spine_ratings")
        ) {
          Text(
            text = "Confirm",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

@Composable
private fun RatingScaleRow(
  metricLabel: String,
  lowLabel: String,
  highLabel: String,
  selectedValue: Int?,
  onValueSelected: (Int) -> Unit,
  testTagPrefix: String
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = metricLabel.uppercase(),
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = BoothPaper
      )

      Text(
        text = if (selectedValue != null) "$selectedValue / 5" else "Not set",
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        color = if (selectedValue != null) BoothAmber else BoothDim
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      (1..5).forEach { value ->
        val isSelected = (selectedValue == value)
        val bg = if (isSelected) BoothAmber else BoothSurface
        val border = if (isSelected) BoothAmber else BoothBorder
        val fg = if (isSelected) BoothInk else BoothPaper

        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onValueSelected(value) }
            .testTag("${testTagPrefix}_$value"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$value",
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = fg
          )
        }
      }
    }
  }
}
