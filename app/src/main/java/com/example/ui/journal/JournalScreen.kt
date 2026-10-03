package com.example.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import java.util.Locale

@Composable
fun JournalScreen(
  modifier: Modifier = Modifier,
  viewModel: JournalViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Leaving the screen saves any night note immediately
  DisposableEffect(Unit) {
    onDispose {
      viewModel.onScreenExit()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .testTag("journal_screen")
  ) {
    // Quiet Top Header
    JournalQuietHeader(
      formattedDate = uiState.formattedDate,
      doneCount = uiState.doneBlocks.size,
      missedCount = uiState.missedBlocks.size
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Pre-filled from today's saved blocks: What held ground (Done / Modified)
      item {
        HeldGroundCard(blocks = uiState.doneBlocks)
      }

      // 2. Pre-filled from today's saved blocks: What drifted (Missed / Skipped with causes)
      item {
        DriftFrictionCard(blocks = uiState.missedBlocks)
      }

      // 3. User may add one optional line: "Anything else?"
      item {
        AnythingElseCard(
          noteText = uiState.nightNote,
          onNoteChange = { viewModel.setNightNote(it) }
        )
      }

      // 4. Saved Spine readings: Woke and Before sleep (only displayed if saved)
      if (uiState.hasWakeReadings || uiState.hasSleepReadings) {
        item {
          SpineReadingsDisplayCard(
            wakeEnergy = uiState.wakeEnergy,
            wakeMood = uiState.wakeMood,
            wakeStress = uiState.wakeStress,
            sleepEnergy = uiState.sleepEnergy,
            sleepMood = uiState.sleepMood,
            sleepStress = uiState.sleepStress
          )
        }
      }
    }
  }
}

@Composable
private fun JournalQuietHeader(
  formattedDate: String,
  doneCount: Int,
  missedCount: Int
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothBlack)
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Text(
      text = formattedDate.uppercase(),
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      letterSpacing = 0.5.sp,
      color = BoothPaper
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = "PLAYBACK • $doneCount done • $missedCount missed",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      color = BoothDim
    )
  }
}

@Composable
private fun HeldGroundCard(blocks: List<DayBlock>) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("card_held_ground")
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "HELD GROUND",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothAmber
        )
        Text(
          text = "${blocks.size} COMPLETED",
          fontFamily = FontFamily.SansSerif,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          color = BoothDim
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (blocks.isEmpty()) {
        Text(
          text = "No completed blocks marked yet today.",
          fontFamily = FontFamily.Serif,
          fontSize = 13.sp,
          color = BoothDim
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          blocks.forEach { block ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(BoothAmber)
                )
                Text(
                  text = block.plannedTime,
                  fontFamily = FontFamily.SansSerif,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = BoothAmber
                )
                Text(
                  text = block.title,
                  fontFamily = FontFamily.Serif,
                  fontSize = 13.sp,
                  color = BoothPaper
                )
              }
              if (block.stampedTime != null) {
                Text(
                  text = "stamped ${block.stampedTime}",
                  fontFamily = FontFamily.SansSerif,
                  fontSize = 11.sp,
                  color = BoothDim
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DriftFrictionCard(blocks: List<DayBlock>) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("card_drift_friction")
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DRIFT & FRICTION",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothMissedGraphite
        )
        Text(
          text = "${blocks.size} MISSED",
          fontFamily = FontFamily.SansSerif,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          color = BoothDim
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (blocks.isEmpty()) {
        Text(
          text = "No missed blocks recorded today.",
          fontFamily = FontFamily.Serif,
          fontSize = 13.sp,
          color = BoothDim
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          blocks.forEach { block ->
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(1.5.dp, BoothMissedGraphite, CircleShape)
                )
                Text(
                  text = block.plannedTime,
                  fontFamily = FontFamily.SansSerif,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = BoothDim
                )
                Text(
                  text = block.title,
                  fontFamily = FontFamily.Serif,
                  fontSize = 13.sp,
                  color = BoothPaper
                )
              }
              if (!block.cause.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(modifier = Modifier.padding(start = 18.dp)) {
                  Text(
                    text = "Cause: ${block.cause}",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = BoothAmber
                  )
                  if (!block.note.isNullOrBlank()) {
                    Text(
                      text = " • ${block.note}",
                      fontFamily = FontFamily.SansSerif,
                      fontSize = 11.sp,
                      color = BoothDim
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AnythingElseCard(
  noteText: String,
  onNoteChange: (String) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("card_anything_else")
  ) {
    Column {
      Text(
        text = "NOTE (OPTIONAL)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = BoothDim
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = noteText,
        onValueChange = onNoteChange,
        placeholder = {
          Text(
            text = "Anything else?",
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            color = BoothDim
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = BoothPaper,
          unfocusedTextColor = BoothPaper,
          focusedContainerColor = BoothBlack,
          unfocusedContainerColor = BoothBlack,
          focusedBorderColor = BoothAmber,
          unfocusedBorderColor = BoothBorder,
          cursorColor = BoothAmber
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_anything_else")
      )
    }
  }
}

@Composable
private fun SpineReadingsDisplayCard(
  wakeEnergy: Int?,
  wakeMood: Int?,
  wakeStress: Int?,
  sleepEnergy: Int?,
  sleepMood: Int?,
  sleepStress: Int?
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(BoothSurface)
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("card_spine_readings")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Woke Readings
      if (wakeEnergy != null || wakeMood != null || wakeStress != null) {
        Column(modifier = Modifier.testTag("card_spine_readings_woke")) {
          Text(
            text = "WOKE",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = BoothAmber
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            wakeEnergy?.let {
              ReadingItem(label = "Energy", value = it, testTag = "woke_energy_value")
            }
            wakeMood?.let {
              ReadingItem(label = "Mood", value = it, testTag = "woke_mood_value")
            }
            wakeStress?.let {
              ReadingItem(label = "Stress", value = it, testTag = "woke_stress_value")
            }
          }
        }
      }

      // Divider if both are present
      if ((wakeEnergy != null || wakeMood != null || wakeStress != null) &&
          (sleepEnergy != null || sleepMood != null || sleepStress != null)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BoothBorder)
        )
      }

      // Before Sleep Readings
      if (sleepEnergy != null || sleepMood != null || sleepStress != null) {
        Column(modifier = Modifier.testTag("card_spine_readings_sleep")) {
          Text(
            text = "BEFORE SLEEP",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = BoothAmber
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            sleepEnergy?.let {
              ReadingItem(label = "Energy", value = it, testTag = "sleep_energy_value")
            }
            sleepMood?.let {
              ReadingItem(label = "Mood", value = it, testTag = "sleep_mood_value")
            }
            sleepStress?.let {
              ReadingItem(label = "Stress", value = it, testTag = "sleep_stress_value")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReadingItem(
  label: String,
  value: Int,
  testTag: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = Modifier.testTag(testTag)
  ) {
    Text(
      text = "$label:",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      color = BoothDim
    )
    Text(
      text = "$value/5",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = BoothPaper
    )
  }
}
