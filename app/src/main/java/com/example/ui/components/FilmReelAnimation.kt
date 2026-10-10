package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated vintage film reel and unspooling perforated 35mm film strip
 * matching the analog reel loading animation.
 *
 * @param modifier Composable modifier
 * @param reelColor Warm golden-amber color of the film reel and film strip
 * @param backgroundColor Dark charcoal background color used for cutouts and perforations
 * @param isSpinning Whether the reel is actively rotating and feeding film
 */
@Composable
fun FilmReelAnimation(
  modifier: Modifier = Modifier,
  reelSize: Dp = 220.dp,
  reelColor: Color = Color(0xFFE5B86E),
  backgroundColor: Color = Color(0xFF161310),
  isSpinning: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "film_reel_transition")

  // Rotation of the circular film reel (spins smoothly as film unspools)
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "reel_rotation"
  )

  // Unspooling progression: film strip feeds out to the right
  val unspoolProgress by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "film_unspool"
  )

  // Continuous sprocket scroll along the film path
  val sprocketScroll by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sprocket_scroll"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(reelSize * 1.35f)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Center the reel slightly to the left so the film strip can extend to the right
      val reelCenter = Offset(w * 0.36f, h * 0.46f)
      val reelRadius = (reelSize.toPx() * 0.44f).coerceAtMost(h * 0.40f).coerceAtMost(w * 0.28f)

      // 1. Draw the unspooling film strip underneath
      drawFilmStrip(
        reelCenter = reelCenter,
        reelRadius = reelRadius,
        reelColor = reelColor,
        darkColor = backgroundColor,
        progress = if (isSpinning) unspoolProgress else 0.85f,
        sprocketScrollFraction = if (isSpinning) sprocketScroll else 0f
      )

      // 2. Draw the circular film reel on top
      drawFilmReel(
        reelCenter = reelCenter,
        reelRadius = reelRadius,
        reelColor = reelColor,
        darkColor = backgroundColor,
        rotationDeg = if (isSpinning) rotationAngle else 0f
      )
    }
  }
}

/**
 * Draws the spinning circular film reel with outer rim, 5 circular cutouts,
 * and center axle hub.
 */
private fun DrawScope.drawFilmReel(
  reelCenter: Offset,
  reelRadius: Float,
  reelColor: Color,
  darkColor: Color,
  rotationDeg: Float
) {
  // Main reel disc
  drawCircle(
    color = reelColor,
    radius = reelRadius,
    center = reelCenter,
    style = Fill
  )

  // Outer rim concentric groove
  drawCircle(
    color = darkColor,
    radius = reelRadius * 0.94f,
    center = reelCenter,
    style = Stroke(width = (reelRadius * 0.024f).coerceAtLeast(1.5f))
  )

  // Outer border edge
  drawCircle(
    color = darkColor,
    radius = reelRadius,
    center = reelCenter,
    style = Stroke(width = (reelRadius * 0.032f).coerceAtLeast(2.0f))
  )

  // 5 evenly spaced circular cutouts around the center
  val cutoutRadius = reelRadius * 0.225f
  val cutoutDistance = reelRadius * 0.54f

  for (i in 0 until 5) {
    val angleDeg = rotationDeg + (i * 72f)
    val angleRad = (angleDeg * PI / 180.0).toFloat()
    val holeX = reelCenter.x + cos(angleRad) * cutoutDistance
    val holeY = reelCenter.y + sin(angleRad) * cutoutDistance
    val holeCenter = Offset(holeX, holeY)

    // Inner cutout hole (transparent / dark background)
    drawCircle(
      color = darkColor,
      radius = cutoutRadius,
      center = holeCenter,
      style = Fill
    )

    // Clean border around each cutout hole
    drawCircle(
      color = darkColor,
      radius = cutoutRadius,
      center = holeCenter,
      style = Stroke(width = (reelRadius * 0.02f).coerceAtLeast(1.5f))
    )
  }

  // Central hub assembly
  val hubRadius = reelRadius * 0.26f
  // Hub outer ring
  drawCircle(
    color = darkColor,
    radius = hubRadius,
    center = reelCenter,
    style = Stroke(width = (reelRadius * 0.028f).coerceAtLeast(2.0f))
  )

  // Center axle spindle hole
  val axleRadius = reelRadius * 0.105f
  drawCircle(
    color = darkColor,
    radius = axleRadius,
    center = reelCenter,
    style = Fill
  )
  drawCircle(
    color = darkColor,
    radius = axleRadius,
    center = reelCenter,
    style = Stroke(width = (reelRadius * 0.02f).coerceAtLeast(1.5f))
  )

  // 4 drive pin / notch holes around axle
  val pinRadius = reelRadius * 0.024f
  val pinDist = reelRadius * 0.18f
  for (k in 0 until 4) {
    val pinAngleDeg = rotationDeg + 45f + (k * 90f)
    val pinAngleRad = (pinAngleDeg * PI / 180.0).toFloat()
    val px = reelCenter.x + cos(pinAngleRad) * pinDist
    val py = reelCenter.y + sin(pinAngleRad) * pinDist
    drawCircle(
      color = darkColor,
      radius = pinRadius,
      center = Offset(px, py),
      style = Fill
    )
  }
}

/**
 * Draws the 35mm film strip unspooling beneath the reel with rectangular sprocket holes
 * and frame dividers.
 */
private fun DrawScope.drawFilmStrip(
  reelCenter: Offset,
  reelRadius: Float,
  reelColor: Color,
  darkColor: Color,
  progress: Float,
  sprocketScrollFraction: Float
) {
  val stripHeight = reelRadius * 0.26f
  val startX = reelCenter.x - reelRadius * 0.52f
  val baseY = reelCenter.y + reelRadius * 0.94f

  // Extension length towards the right
  val maxAvailableWidth = (size.width - startX) * 0.92f
  val minLength = reelRadius * 0.85f
  val currentLength = minLength + (maxAvailableWidth - minLength) * progress
  val endX = startX + currentLength

  // Point where the strip exits from under the reel and starts to curl upward
  val exitX = reelCenter.x + reelRadius * 0.22f

  // Calculate curve points along the centerline of the tape
  val stepPx = 6f
  val points = mutableListOf<Offset>()
  val angles = mutableListOf<Float>()

  var currX = startX
  while (currX <= endX) {
    val t = if (currX <= exitX) {
      0f
    } else {
      ((currX - exitX) / (endX - exitX).coerceAtLeast(1f)).coerceIn(0f, 1f)
    }

    // Gentle S-curve lift at the tail
    // Starts flat under reel, waves gently down then curls up towards the tip
    val lift = if (t > 0f) {
      val wave = sin(t * PI.toFloat() * 0.85f) * (reelRadius * 0.08f)
      val curlUp = (t * t) * (reelRadius * 0.38f)
      curlUp - wave
    } else {
      0f
    }

    val y = baseY - lift
    points.add(Offset(currX, y))

    // Estimate tangent angle
    val tangentY = if (t > 0f) -(2 * t * (reelRadius * 0.38f) - cos(t * PI.toFloat() * 0.85f) * (reelRadius * 0.08f) * PI.toFloat() * 0.85f) else 0f
    val angle = kotlin.math.atan2(tangentY, 1f)
    angles.add(angle)

    currX += stepPx
  }

  if (points.size < 2) return

  // Build top and bottom path of the film ribbon
  val topPath = Path()
  val bottomPath = Path()
  val halfH = stripHeight / 2f

  for (i in points.indices) {
    val pt = points[i]
    val ang = angles[i]
    // Normal vector perpendicular to the curve
    val nx = -sin(ang)
    val ny = cos(ang)

    val topPt = Offset(pt.x + nx * halfH, pt.y + ny * halfH)
    val botPt = Offset(pt.x - nx * halfH, pt.y - ny * halfH)

    if (i == 0) {
      topPath.moveTo(topPt.x, topPt.y)
      bottomPath.moveTo(botPt.x, botPt.y)
    } else {
      topPath.lineTo(topPt.x, topPt.y)
      bottomPath.lineTo(botPt.x, botPt.y)
    }
  }

  // Combine into a filled ribbon polygon
  val fullTapePath = Path()
  fullTapePath.addPath(topPath)

  // Connect to bottom path at end
  val lastIdx = points.lastIndex
  val lastPt = points[lastIdx]
  val lastAng = angles[lastIdx]
  val lastNx = -sin(lastAng)
  val lastNy = cos(lastAng)
  val endBot = Offset(lastPt.x - lastNx * halfH, lastPt.y - lastNy * halfH)

  fullTapePath.lineTo(endBot.x, endBot.y)

  // Trace back along bottom
  for (i in points.indices.reversed()) {
    val pt = points[i]
    val ang = angles[i]
    val nx = -sin(ang)
    val ny = cos(ang)
    fullTapePath.lineTo(pt.x - nx * halfH, pt.y - ny * halfH)
  }
  fullTapePath.close()

  // 1. Fill tape ribbon
  drawPath(path = fullTapePath, color = reelColor, style = Fill)

  // 2. Outline tape ribbon
  drawPath(
    path = fullTapePath,
    color = darkColor,
    style = Stroke(width = (reelRadius * 0.024f).coerceAtLeast(1.8f))
  )

  // 3. Sprocket holes (rectangular perforations) along top and bottom edges
  val sprocketWidth = reelRadius * 0.062f
  val sprocketHeight = reelRadius * 0.044f
  val sprocketSpacing = reelRadius * 0.125f
  val margin = stripHeight * 0.28f

  var distFromStart = (sprocketScrollFraction * sprocketSpacing)
  while (distFromStart < currentLength - (sprocketWidth * 1.2f)) {
    val ratio = (distFromStart / currentLength).coerceIn(0f, 1f)
    val ptIdx = (ratio * (points.size - 1)).toInt().coerceIn(0, points.lastIndex)
    val pt = points[ptIdx]
    val ang = angles[ptIdx]
    val nx = -sin(ang)
    val ny = cos(ang)

    // Top sprocket center
    val topHole = Offset(pt.x + nx * (halfH - margin), pt.y + ny * (halfH - margin))
    // Bottom sprocket center
    val botHole = Offset(pt.x - nx * (halfH - margin), pt.y - ny * (halfH - margin))

    val deg = (ang * 180f / PI).toFloat()

    // Draw top sprocket
    rotate(degrees = deg, pivot = topHole) {
      drawRoundRect(
        color = darkColor,
        topLeft = Offset(topHole.x - sprocketWidth / 2f, topHole.y - sprocketHeight / 2f),
        size = Size(sprocketWidth, sprocketHeight),
        cornerRadius = CornerRadius(1.2f, 1.2f),
        style = Fill
      )
    }

    // Draw bottom sprocket
    rotate(degrees = deg, pivot = botHole) {
      drawRoundRect(
        color = darkColor,
        topLeft = Offset(botHole.x - sprocketWidth / 2f, botHole.y - sprocketHeight / 2f),
        size = Size(sprocketWidth, sprocketHeight),
        cornerRadius = CornerRadius(1.2f, 1.2f),
        style = Fill
      )
    }

    distFromStart += sprocketSpacing
  }

  // 4. Subtle vertical frame divider lines spaced along the film
  val frameSpacing = reelRadius * 0.48f
  var frameDist = (sprocketScrollFraction * frameSpacing) + (frameSpacing * 0.5f)
  while (frameDist < currentLength - (frameSpacing * 0.3f)) {
    val ratio = (frameDist / currentLength).coerceIn(0f, 1f)
    val ptIdx = (ratio * (points.size - 1)).toInt().coerceIn(0, points.lastIndex)
    val pt = points[ptIdx]
    val ang = angles[ptIdx]
    val nx = -sin(ang)
    val ny = cos(ang)

    val frameTop = Offset(pt.x + nx * (halfH - margin * 1.5f), pt.y + ny * (halfH - margin * 1.5f))
    val frameBot = Offset(pt.x - nx * (halfH - margin * 1.5f), pt.y - ny * (halfH - margin * 1.5f))

    drawLine(
      color = darkColor.copy(alpha = 0.55f),
      start = frameTop,
      end = frameBot,
      strokeWidth = 1.2f
    )

    frameDist += frameSpacing
  }
}
