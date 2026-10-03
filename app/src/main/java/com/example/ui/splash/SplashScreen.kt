package com.example.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularTapeReel
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothMissedGraphite
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onEnterApp: () -> Unit,
  modifier: Modifier = Modifier
) {
  var litSegments by remember { mutableIntStateOf(1) }
  var showContent by remember { mutableStateOf(false) }

  // Tape reel initialization sequence
  LaunchedEffect(Unit) {
    delay(200)
    showContent = true
    delay(400)
    litSegments = 3
    delay(500)
    litSegments = 5
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(BoothBlack)
      .padding(24.dp)
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Analog 7-segment tape reel
      CircularTapeReel(
        capturedCount = litSegments,
        totalSegments = 7,
        reelSize = 150.dp
      )

      Spacer(modifier = Modifier.height(32.dp))

      AnimatedVisibility(
        visible = showContent,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 20 }
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Title in Serif
          Text(
            text = "LOOP JOURNAL",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp,
            letterSpacing = 2.sp,
            color = BoothPaper,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("splash_title")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // System Subtitle
          Text(
            text = "OBSERVE • BREAKPOINT • ADJUST",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = BoothDim,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Operating Thesis
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(BoothSurface, RoundedCornerShape(10.dp))
              .border(1.dp, BoothBorder, RoundedCornerShape(10.dp))
              .padding(16.dp)
          ) {
            Column {
              Text(
                text = "OPERATING SYSTEM",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = BoothAmber
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Where did the intended day break, what got in the way, and what small change should be tested.",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = BoothPaper
              )
            }
          }

          Spacer(modifier = Modifier.height(36.dp))

          // Reel status strip representation
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            repeat(7) { index ->
              val isLit = index < litSegments
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(if (isLit) BoothAmber else BoothMissedGraphite)
              )
            }
          }

          Spacer(modifier = Modifier.height(32.dp))

          // Enter Button: "Keep the tape rolling" / "Open Journal"
          Button(
            onClick = onEnterApp,
            colors = ButtonDefaults.buttonColors(
              containerColor = BoothAmber,
              contentColor = BoothInk
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("splash_enter_button")
          ) {
            Text(
              text = "OPEN TODAY // KEEP TAPE ROLLING",
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Offline • No account required",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = BoothDim
          )
        }
      }
    }
  }
}
