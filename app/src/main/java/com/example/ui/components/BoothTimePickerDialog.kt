package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated
import java.util.Locale

/**
 * 24-Hour TimePicker Dialog using androidx.compose.material3.TimePicker.
 *
 * Rules:
 * - Uses 24-hour time format.
 * - Initial value = current block time (or newly assigned time).
 * - Confirm writes HH:mm back onto the block and saves it.
 * - Cancel changes nothing.
 * - Keyboard is not required.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoothTimePickerDialog(
  initialTime: String,
  title: String = "SET INTENDED TIME",
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  val (initialHour, initialMinute) = parseTime(initialTime)

  val timePickerState = rememberTimePickerState(
    initialHour = initialHour,
    initialMinute = initialMinute,
    is24Hour = true
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = BoothSurfaceElevated,
      tonalElevation = 6.dp,
      modifier = Modifier
        .width(IntrinsicSize.Min)
        .border(1.dp, BoothBorder, RoundedCornerShape(16.dp))
        .padding(2.dp)
        .testTag("time_picker_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = BoothAmber
          )
          Text(
            text = "24-HOUR",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = BoothDim
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TimePicker(
          state = timePickerState,
          colors = TimePickerDefaults.colors(
            clockDialColor = BoothSurface,
            clockDialSelectedContentColor = BoothInk,
            clockDialUnselectedContentColor = BoothPaper,
            selectorColor = BoothAmber,
            timeSelectorSelectedContainerColor = BoothAmber,
            timeSelectorUnselectedContainerColor = BoothSurface,
            timeSelectorSelectedContentColor = BoothInk,
            timeSelectorUnselectedContentColor = BoothPaper
          ),
          modifier = Modifier.testTag("material3_time_picker")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BoothBorder),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = BoothDim
            ),
            modifier = Modifier.testTag("time_picker_cancel_button")
          ) {
            Text(
              text = "Cancel",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Button(
            onClick = {
              val formatted = formatTime(timePickerState.hour, timePickerState.minute)
              onConfirm(formatted)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BoothAmber,
              contentColor = BoothInk
            ),
            modifier = Modifier.testTag("time_picker_confirm_button")
          ) {
            Text(
              text = "Confirm",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }
  }
}

private fun parseTime(timeStr: String): Pair<Int, Int> {
  return try {
    val parts = timeStr.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
  } catch (_: Exception) {
    9 to 0
  }
}

private fun formatTime(hour: Int, minute: Int): String {
  return String.format(Locale.US, "%02d:%02d", hour, minute)
}
