package com.example.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.BreakpointChain
import com.example.domain.model.DayRecord
import com.example.domain.model.TapeSynthesis
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothBorderSubtle
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated

@Composable
fun InsightsScreen(
  modifier: Modifier = Modifier,
  viewModel: InsightsViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val synthesis = uiState.synthesis
  val hasSevenDays = uiState.capturedDaysCount >= 7

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .testTag("insights_screen")
  ) {
    // Header
    InsightsTopHeader()

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      if (hasSevenDays && synthesis != null) {
        // Section 1: Playback Note
        item {
          PlaybackNoteCard(synthesis = synthesis)
        }

        // Section 2: Possible Chain Language, Never Diagnosis
        if (synthesis.chains.isNotEmpty()) {
          item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
              Text(
                text = "POSSIBLE BREAKPOINT CHAINS",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.3.sp,
                color = BoothDim
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Mechanical chain hypotheses observed across recorded tapes. Causal patterns, never clinical diagnosis.",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = BoothDim
              )
            }
          }

          items(items = synthesis.chains, key = { it.id }) { chain ->
            BreakpointChainCard(chain = chain)
          }
        }

        // Section 3: Woke Against Before-Sleep Comparison
        item {
          WokeAgainstBeforeSleepCard(capturedDays = uiState.capturedDays)
        }
      } else {
        // Under 7 captured days: show "Not enough days yet."
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(BoothSurface, RoundedCornerShape(10.dp))
              .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
              .padding(28.dp)
              .testTag("insights_not_enough_days")
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Not enough days yet.",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = BoothPaper
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "7 real captured days are required before breakpoint chains and routine fidelity can be observed.",
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = BoothDim,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InsightsTopHeader() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothBlack)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "OBSERVATIONS // SYNTHESIS",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Insights",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        letterSpacing = 0.5.sp,
        color = BoothPaper
      )
    }

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(BoothSurface)
        .border(1.dp, BoothBorder, RoundedCornerShape(6.dp))
        .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
      Text(
        text = "7-DAY SYNTHESIS",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = BoothAmber
      )
    }
  }
}

@Composable
private fun PlaybackNoteCard(synthesis: TapeSynthesis) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(16.dp)
      .testTag("playback_note_card")
  ) {
    Column {
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
              .size(8.dp)
              .clip(CircleShape)
              .background(BoothAmber)
          )
          Text(
            text = "PLAYBACK NOTE",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = BoothAmber
          )
        }

        Text(
          text = "${synthesis.capturedDaysCount} / ${synthesis.daysAnalyzed} CAPTURED",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Medium,
          fontSize = 10.sp,
          color = BoothDim
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = synthesis.playbackNote,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = BoothPaper
      )
    }
  }
}

@Composable
private fun BreakpointChainCard(chain: BreakpointChain) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("chain_card_${chain.id}")
  ) {
    // Chain Title in Serif
    Text(
      text = chain.title,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Step 1: Trigger
    ChainStepRow(
      stepTag = "TRIGGER",
      text = chain.trigger,
      tagColor = BoothDim
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Step 2: Mechanism
    ChainStepRow(
      stepTag = "MECHANISM",
      text = chain.mechanism,
      tagColor = BoothDim
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Step 3: Downstream Impact
    ChainStepRow(
      stepTag = "DOWNSTREAM",
      text = chain.downstreamImpact,
      tagColor = BoothAmber
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Frequency observation
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(BoothSurfaceElevated)
        .border(0.8.dp, BoothBorderSubtle, RoundedCornerShape(4.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Text(
        text = chain.frequencyNote,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = BoothDim
      )
    }
  }
}

@Composable
private fun ChainStepRow(
  stepTag: String,
  text: String,
  tagColor: androidx.compose.ui.graphics.Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = "$stepTag →",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      letterSpacing = 0.5.sp,
      color = tagColor,
      modifier = Modifier.width(90.dp)
    )
    Text(
      text = text,
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      color = BoothPaper,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun WokeAgainstBeforeSleepCard(capturedDays: List<DayRecord>) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(16.dp)
      .testTag("woke_against_before_sleep_card")
  ) {
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
            .size(8.dp)
            .clip(CircleShape)
            .background(BoothAmber)
        )
        Text(
          text = "WOKE AGAINST BEFORE-SLEEP",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothAmber
        )
      }
      Text(
        text = "SPINE RATINGS (1–5)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = BoothDim
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Observed morning woke ratings compared against evening before-sleep ratings across recorded days.",
      fontFamily = FontFamily.SansSerif,
      fontSize = 11.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Table Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "DATE",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        color = BoothDim,
        modifier = Modifier.weight(1f)
      )
      Text(
        text = "WOKE (E • M • S)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        color = BoothPaper,
        modifier = Modifier.weight(1.2f)
      )
      Text(
        text = "BEFORE-SLEEP (E • M • S)",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        color = BoothDim,
        modifier = Modifier.weight(1.3f)
      )
    }

    HorizontalDivider(color = BoothBorderSubtle, thickness = 0.8.dp)
    Spacer(modifier = Modifier.height(6.dp))

    if (capturedDays.isEmpty()) {
      Text(
        text = "No recorded spine ratings yet.",
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        color = BoothDim,
        modifier = Modifier.padding(vertical = 8.dp)
      )
    } else {
      capturedDays.take(14).forEach { day ->
        val wakeStr = if (day.hasWakeReadings) {
          "${day.wakeEnergy ?: "-"} • ${day.wakeMood ?: "-"} • ${day.wakeStress ?: "-"}"
        } else {
          "--"
        }
        val sleepStr = if (day.hasSleepReadings) {
          "${day.sleepEnergy ?: "-"} • ${day.sleepMood ?: "-"} • ${day.sleepStress ?: "-"}"
        } else {
          "--"
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = day.dateIso,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = BoothDim,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = wakeStr,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = if (day.hasWakeReadings) BoothPaper else BoothDim,
            modifier = Modifier.weight(1.2f)
          )
          Text(
            text = sleepStr,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = if (day.hasSleepReadings) BoothAmber else BoothDim,
            modifier = Modifier.weight(1.3f)
          )
        }
      }
    }
  }
}
