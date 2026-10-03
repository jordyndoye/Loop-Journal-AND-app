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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.domain.model.ExperimentSuggestion
import com.example.domain.model.TapeSynthesis
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothBorderSubtle
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
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
      if (synthesis != null && synthesis.capturedDaysCount >= 7) {
        // Section 1: Playback Note
        item {
          PlaybackNoteCard(synthesis = synthesis)
        }

        // Section 2: Possible Chain Language, Never Diagnosis
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

        // Section 3: One Experiment Suggestion
        item {
          Column(modifier = Modifier.padding(top = 6.dp)) {
            Text(
              text = "ONE EXPERIMENT SUGGESTION",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              letterSpacing = 1.3.sp,
              color = BoothDim
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Only one small change tested at a time. Derived from the primary friction chain.",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Normal,
              fontSize = 11.sp,
              color = BoothDim
            )
          }
        }

        item {
          OneExperimentSuggestionCard(
            suggestion = synthesis.oneSuggestedExperiment,
            isPromoted = uiState.isExperimentPromoted,
            feedbackMessage = uiState.feedbackMessage,
            onPromote = { viewModel.promoteSuggestedExperiment() }
          )
        }
      } else {
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
private fun OneExperimentSuggestionCard(
  suggestion: ExperimentSuggestion,
  isPromoted: Boolean,
  feedbackMessage: String?,
  onPromote: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurfaceElevated, RoundedCornerShape(10.dp))
      .border(1.dp, BoothAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
      .padding(16.dp)
      .testTag("one_experiment_suggestion_card")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Science,
          contentDescription = null,
          tint = BoothAmber,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "EXPERIMENT #${suggestion.number} • ${suggestion.targetDurationDays}-DAY CYCLE",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          letterSpacing = 1.sp,
          color = BoothAmber
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(BoothAmber.copy(alpha = 0.15f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "ONE VARIABLE",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          color = BoothAmber
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Title in Serif
    Text(
      text = suggestion.title,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Hypothesis
    Text(
      text = "HYPOTHESIS",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 10.sp,
      letterSpacing = 0.8.sp,
      color = BoothDim
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = suggestion.hypothesis,
      fontFamily = FontFamily.SansSerif,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Single Intervention
    Text(
      text = "SINGLE INTERVENTION",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 10.sp,
      letterSpacing = 0.8.sp,
      color = BoothDim
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = suggestion.singleIntervention,
      fontFamily = FontFamily.SansSerif,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      color = BoothAmber
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Metric to watch
    Text(
      text = "METRIC TO WATCH",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 10.sp,
      letterSpacing = 0.8.sp,
      color = BoothDim
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = suggestion.metricToWatch,
      fontFamily = FontFamily.SansSerif,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Button
    Button(
      onClick = onPromote,
      colors = ButtonDefaults.buttonColors(
        containerColor = if (isPromoted) BoothSurface else BoothAmber,
        contentColor = if (isPromoted) BoothAmber else BoothInk
      ),
      shape = RoundedCornerShape(8.dp),
      border = if (isPromoted) androidx.compose.foundation.BorderStroke(1.dp, BoothAmber) else null,
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("btn_stage_experiment")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = if (isPromoted) Icons.Default.Check else Icons.Default.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isPromoted) "STAGED TO EXPERIMENTS TAB" else "STAGE FOR TESTING // SEND TO EXPERIMENTS",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          letterSpacing = 0.5.sp
        )
      }
    }

    if (feedbackMessage != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = feedbackMessage,
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        color = BoothAmber
      )
    }
  }
}
