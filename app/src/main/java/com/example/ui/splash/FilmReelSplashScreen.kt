package com.example.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FilmReelAnimation
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothPaper
import kotlinx.coroutines.delay

/**
 * Opening screen animation displaying the vintage film reel unspooling its tape,
 * met upon app startup while the screen and database load.
 *
 * Tapping anywhere allows the user to immediately enter the app.
 */
@Composable
fun FilmReelSplashScreen(
  onFinishLoading: () -> Unit,
  modifier: Modifier = Modifier,
  minimumDurationMs: Long = 2400L
) {
  var statusText by remember { mutableStateOf("THREADING TAPE...") }
  var isReady by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(minimumDurationMs / 2)
    statusText = "SYSTEM READY"
    delay(minimumDurationMs / 2)
    isReady = true
    onFinishLoading()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onFinishLoading
      )
      .testTag("film_reel_splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    ) {
      // 1. Hero Film Reel Animation (spinning reel with spooling perforated tape)
      FilmReelAnimation(
        reelSize = 250.dp,
        reelColor = BoothAmber,
        backgroundColor = BoothBlack,
        isSpinning = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("film_reel_animation")
      )

      Spacer(modifier = Modifier.height(36.dp))

      // 2. Title in timeless Serif typography
      Text(
        text = "LOOP JOURNAL",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        letterSpacing = 4.sp,
        color = BoothPaper,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("splash_title")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Status indicator & tagline
      Text(
        text = statusText,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        color = BoothDim,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 4. Subtle tap hint
      Text(
        text = "TAP TO SKIP",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        color = BoothDim.copy(alpha = 0.5f)
      )
    }
  }
}
