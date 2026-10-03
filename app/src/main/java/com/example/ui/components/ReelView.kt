package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.example.ui.theme.BoothSurfaceElevated
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Representation of one segment of the 7-segment reel.
 *
 * Rule: A reel segment lights amber when the day was CAPTURED
 * (including days with missed blocks recorded).
 */
data class ReelDay(
  val dayLabel: String, // "M", "T", "W", "T", "F", "S", "S"
  val dateNumber: Int,
  val isCaptured: Boolean,
  val isToday: Boolean = false,
  val hasMissedBlocks: Boolean = false,
  val dateIso: String = "" // yyyy-MM-dd
)

/**
 * Circular 7-Segment Analog Tape Reel.
 * The core visual instrument of Loop Journal.
 */
@Composable
fun CircularTapeReel(
  capturedCount: Int,
  totalSegments: Int = 7,
  modifier: Modifier = Modifier,
  reelSize: Dp = 180.dp,
  isSpinning: Boolean = false
) {
  val animatedCaptured by animateFloatAsState(
    targetValue = capturedCount.toFloat(),
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "reel_captured_animation"
  )

  Box(
    modifier = modifier
      .size(reelSize)
      .testTag("circular_tape_reel"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(reelSize)) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val outerRadius = size.minDimension / 2f - 10.dp.toPx()
      val innerRadius = outerRadius * 0.58f
      val strokeWidth = outerRadius - innerRadius
      val midRadius = (outerRadius + innerRadius) / 2f

      // Outer bezel ring (analog instrument rim)
      drawCircle(
        color = BoothBorder,
        radius = outerRadius + 4.dp.toPx(),
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
      )

      // Background track for the 7 segments
      drawCircle(
        color = BoothSurface,
        radius = midRadius,
        center = center,
        style = Stroke(width = strokeWidth)
      )

      // 7 Segments around the circle (each arc with a physical separator gap)
      val gapDegrees = 6f
      val sweepPerSegment = (360f / totalSegments) - gapDegrees

      for (i in 0 until totalSegments) {
        val startAngle = -90f + i * (360f / totalSegments) + (gapDegrees / 2f)
        val isLit = i < animatedCaptured.toInt()

        val segmentColor = if (isLit) {
          BoothAmber
        } else {
          BoothMissedGraphite
        }

        drawArc(
          color = segmentColor,
          startAngle = startAngle,
          sweepAngle = sweepPerSegment,
          useCenter = false,
          topLeft = Offset(center.x - midRadius, center.y - midRadius),
          size = Size(midRadius * 2f, midRadius * 2f),
          style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
        )
      }

      // Inner hub rim
      drawCircle(
        color = BoothBorder,
        radius = innerRadius - 2.dp.toPx(),
        center = center,
        style = Stroke(width = 2.dp.toPx())
      )

      // Spindle center hole
      drawCircle(
        color = BoothBlack,
        radius = innerRadius * 0.45f,
        center = center
      )

      // Spindle brass teeth (3 radial notches of a tape reel)
      for (notch in 0 until 3) {
        val angleRad = (notch * 120.0 * PI / 180.0).toFloat()
        val startX = center.x + (innerRadius * 0.25f) * cos(angleRad)
        val startY = center.y + (innerRadius * 0.25f) * sin(angleRad)
        val endX = center.x + (innerRadius * 0.65f) * cos(angleRad)
        val endY = center.y + (innerRadius * 0.65f) * sin(angleRad)
        drawLine(
          color = BoothAmber,
          start = Offset(startX, startY),
          end = Offset(endX, endY),
          strokeWidth = 3.dp.toPx(),
          cap = StrokeCap.Round
        )
      }
    }
  }
}

/**
 * 7-Segment Horizontal Week Reel.
 * Displays the current week's capture tape.
 * A segment lights amber when the day was CAPTURED.
 */
@Composable
fun WeekReelBar(
  days: List<ReelDay>,
  modifier: Modifier = Modifier,
  selectedDateIso: String = "",
  onDaySelected: (ReelDay) -> Unit = {}
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(BoothSurface, RoundedCornerShape(12.dp))
      .border(1.dp, BoothBorder, RoundedCornerShape(12.dp))
      .padding(horizontal = 8.dp, vertical = 10.dp)
      .testTag("week_reel_bar")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      days.forEach { day ->
        WeekReelSegment(
          day = day,
          isSelected = day.dateIso == selectedDateIso,
          onClick = { onDaySelected(day) }
        )
      }
    }
  }
}

@Composable
private fun WeekReelSegment(
  day: ReelDay,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val isCaptured = day.isCaptured
  val isToday = day.isToday

  val labelColor = when {
    isCaptured -> BoothAmber
    isSelected || isToday -> BoothPaper
    else -> BoothDim
  }

  val dateColor = when {
    isCaptured -> BoothAmber
    isSelected || isToday -> BoothPaper
    else -> BoothDim
  }

  val containerBg = when {
    isCaptured -> BoothAmber.copy(alpha = 0.22f)
    isSelected -> BoothSurfaceElevated
    isToday -> BoothSurfaceElevated.copy(alpha = 0.6f)
    else -> Color.Transparent
  }

  val containerBorder = when {
    isCaptured -> BoothAmber
    isSelected -> BoothPaper
    isToday -> BoothPaper.copy(alpha = 0.35f)
    else -> Color.Transparent
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(containerBg)
      .border(1.dp, containerBorder, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 6.dp)
      .testTag("reel_segment_${day.dayLabel}_${day.dateNumber}")
  ) {
    Text(
      text = day.dayLabel,
      fontFamily = FontFamily.SansSerif,
      fontWeight = if (isCaptured || isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
      fontSize = 11.sp,
      color = labelColor
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Segment indicator (amber checkmark badge if completed, ring if uncaptured)
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(
          if (isCaptured) BoothAmber else Color.Transparent
        )
        .border(
          width = if (isCaptured || isSelected || isToday) 2.dp else 1.5.dp,
          color = when {
            isCaptured -> BoothAmber
            isSelected -> BoothPaper
            isToday -> BoothPaper
            else -> BoothMissedGraphite
          },
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isCaptured) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Completed",
          tint = BoothInk,
          modifier = Modifier.size(14.dp)
        )
      } else if (day.hasMissedBlocks) {
        // Missed = empty graphite ring dot, NEVER RED
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(BoothMissedGraphite)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "${day.dateNumber}",
      fontFamily = FontFamily.SansSerif,
      fontWeight = if (isCaptured || isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
      fontSize = 12.sp,
      color = dateColor
    )
  }
}
