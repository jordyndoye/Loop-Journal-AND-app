package com.example.ui.experiments

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.model.Experiment
import com.example.domain.model.ExperimentStatus
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentsScreen(
  modifier: Modifier = Modifier,
  viewModel: ExperimentsViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val active = uiState.activeExperiment

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .testTag("experiments_screen")
  ) {
    // Top Bar
    ExperimentsTopBar(
      onInitiateNew = { viewModel.startNewExperiment() }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Feedback Toast
      if (uiState.feedbackToast != null) {
        item {
          FeedbackBanner(
            message = uiState.feedbackToast!!,
            onDismiss = { viewModel.dismissToast() }
          )
        }
      }

      // Thesis card: One Experiment Only
      item {
        SingleVariableRuleCard()
      }

      // Active Experiment
      if (active != null) {
        item {
          ActiveExperimentCard(
            experiment = active,
            notesInput = uiState.notesInput,
            onNotesChange = { viewModel.updateNotes(it) },
            onKeep = { viewModel.keepExperiment() },
            onModify = { viewModel.startModify() },
            onAbandon = { viewModel.abandonExperiment() }
          )
        }
      } else {
        item {
          NoActiveExperimentCard(onStart = { viewModel.startNewExperiment() })
        }
      }

      // Past Experiments Archive
      if (uiState.pastExperiments.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "EXPERIMENT ARCHIVE // HISTORY",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              letterSpacing = 1.3.sp,
              color = BoothDim
            )
            Text(
              text = "${uiState.pastExperiments.size} PAST TESTS",
              fontFamily = FontFamily.SansSerif,
              fontSize = 10.sp,
              color = BoothAmber
            )
          }
        }

        items(items = uiState.pastExperiments, key = { it.id }) { pastExp ->
          PastExperimentCard(experiment = pastExp)
        }
      } else {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(BoothSurface, RoundedCornerShape(10.dp))
              .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
              .padding(18.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "NO COMPLETED EXPERIMENTS YET",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = BoothDim
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "When you decide to Keep, Modify, or Abandon an experiment, its findings will be logged here.",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                color = BoothPaper,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      }
    }

    // Modal Sheet for Modifying Experiment
    if (uiState.showModifySheet && active != null) {
      ModifyExperimentSheet(
        experiment = active,
        onDismiss = { viewModel.dismissModify() },
        onConfirm = { intervention, notes ->
          viewModel.confirmModify(intervention, notes)
        }
      )
    }

    // Modal Sheet for New Experiment
    if (uiState.showNewSheet) {
      NewExperimentSheet(
        onDismiss = { viewModel.dismissNewExperiment() },
        onCreate = { title, hypothesis, intervention, metric ->
          viewModel.createNewExperiment(title, hypothesis, intervention, metric, 7)
        }
      )
    }
  }
}

@Composable
private fun ExperimentsTopBar(
  onInitiateNew: () -> Unit
) {
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
        text = "TEST CYCLE // ONE VARIABLE",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Experiments",
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
        text = "ONE EXPERIMENT ONLY",
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
private fun SingleVariableRuleCard() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
  ) {
    Column {
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
          text = "CORE EXPERIMENTAL RULE",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          letterSpacing = 1.2.sp,
          color = BoothAmber
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Never test multiple interventions at once. One single variable runs for a 7-day cycle. At the end, choose: Keep into system, Modify parameters, or Abandon.",
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = BoothPaper
      )
    }
  }
}

@Composable
private fun ActiveExperimentCard(
  experiment: Experiment,
  notesInput: String,
  onNotesChange: (String) -> Unit,
  onKeep: () -> Unit,
  onModify: () -> Unit,
  onAbandon: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurfaceElevated, RoundedCornerShape(12.dp))
      .border(1.dp, BoothAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(16.dp)
      .testTag("active_experiment_card")
  ) {
    // Header & Cycle Progress
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
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "ACTIVE // EXPERIMENT #${experiment.number}",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 1.sp,
          color = BoothAmber
        )
      }

      // 7-Day Cycle Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 1..experiment.totalDays) {
          val isFilled = i <= experiment.dayCount
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(if (isFilled) BoothAmber else BoothBorder)
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "DAY ${experiment.dayCount}/${experiment.totalDays}",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = BoothDim
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Title in Serif
    Text(
      text = experiment.title,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 19.sp,
      lineHeight = 25.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(12.dp))

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
      text = experiment.hypothesis,
      fontFamily = FontFamily.SansSerif,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Single Intervention (Highlighted Box)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(BoothAmber.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
        .border(1.dp, BoothAmber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
        .padding(12.dp)
    ) {
      Column {
        Text(
          text = "SINGLE VARIABLE INTERVENTION",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          letterSpacing = 0.8.sp,
          color = BoothAmber
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = experiment.singleIntervention,
          fontFamily = FontFamily.SansSerif,
          fontSize = 13.sp,
          lineHeight = 18.sp,
          color = BoothPaper
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Metric To Watch
    Text(
      text = "PRIMARY METRIC: ${experiment.metricToWatch}",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Ongoing Notes Field
    Text(
      text = "TEST OBSERVATION LOG",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 10.sp,
      letterSpacing = 0.8.sp,
      color = BoothDim
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
      value = notesInput,
      onValueChange = onNotesChange,
      placeholder = { Text("Log daily observations (e.g. sleep spine arrived on time).", color = BoothDim) },
      minLines = 2,
      maxLines = 4,
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
        .testTag("input_experiment_notes")
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Section title for decisions
    Text(
      text = "CORE DECISIONS // KEEP • MODIFY • ABANDON",
      fontFamily = FontFamily.SansSerif,
      fontWeight = FontWeight.SemiBold,
      fontSize = 10.sp,
      letterSpacing = 1.sp,
      color = BoothDim
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 3 Primary Buttons: Keep / Modify / Abandon
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // KEEP
      Button(
        onClick = onKeep,
        colors = ButtonDefaults.buttonColors(
          containerColor = BoothAmber,
          contentColor = BoothInk
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1.2f)
          .height(46.dp)
          .testTag("btn_keep_experiment")
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Keep",
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }

      // MODIFY
      OutlinedButton(
        onClick = onModify,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BoothPaper),
        border = androidx.compose.foundation.BorderStroke(1.dp, BoothBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .height(46.dp)
          .testTag("btn_modify_experiment")
      ) {
        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = BoothPaper, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Modify",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Medium,
          fontSize = 12.sp
        )
      }

      // ABANDON
      OutlinedButton(
        onClick = onAbandon,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BoothMissedGraphite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BoothBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .height(46.dp)
          .testTag("btn_abandon_experiment")
      ) {
        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = BoothDim, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Abandon",
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Medium,
          fontSize = 12.sp,
          color = BoothDim
        )
      }
    }
  }
}

@Composable
private fun NoActiveExperimentCard(onStart: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(20.dp)
      .testTag("no_active_experiment_card")
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "NO EXPERIMENT IN PROGRESS",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = BoothDim
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Run one experiment at a time to test a specific breakpoint.",
        fontFamily = FontFamily.Serif,
        fontSize = 14.sp,
        color = BoothPaper
      )
      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = onStart,
        colors = ButtonDefaults.buttonColors(containerColor = BoothAmber, contentColor = BoothInk),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("btn_initiate_new_experiment")
      ) {
        Text("Initiate 7-Day Cycle", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun PastExperimentCard(experiment: Experiment) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(10.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
      .padding(14.dp)
      .testTag("past_experiment_${experiment.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "EXPERIMENT #${experiment.number}",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = BoothDim
      )

      val (statusBg, statusFg) = when (experiment.status) {
        ExperimentStatus.KEPT -> Pair(BoothAmber.copy(alpha = 0.2f), BoothAmber)
        ExperimentStatus.MODIFIED -> Pair(BoothPaper.copy(alpha = 0.15f), BoothPaper)
        ExperimentStatus.ABANDONED -> Pair(BoothSurfaceElevated, BoothDim)
        else -> Pair(BoothSurfaceElevated, BoothPaper)
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(statusBg)
          .border(0.8.dp, statusFg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
        .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = experiment.status.displayName.uppercase(),
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          letterSpacing = 0.8.sp,
          color = statusFg
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = experiment.title,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      color = BoothPaper
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Intervention: ${experiment.singleIntervention}",
      fontFamily = FontFamily.SansSerif,
      fontSize = 11.sp,
      color = BoothDim
    )

    if (experiment.observationNotes.isNotBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Observation: ${experiment.observationNotes}",
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        color = BoothPaper
      )
    }
  }
}

@Composable
private fun FeedbackBanner(message: String, onDismiss: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(BoothAmber.copy(alpha = 0.15f))
      .border(1.dp, BoothAmber, RoundedCornerShape(8.dp))
      .clickable(onClick = onDismiss)
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = message,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = BoothAmber
      )
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Dismiss",
        tint = BoothAmber,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}
