package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.components.FilmReelAnimation
import com.example.ui.splash.FilmReelSplashScreen
import com.example.ui.theme.BoothTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FilmReelAnimationTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testFilmReelAnimationRenders() {
    composeTestRule.setContent {
      BoothTheme {
        FilmReelAnimation()
      }
    }
    // Ensures Canvas composition executes without exception
    composeTestRule.waitForIdle()
  }

  @Test
  fun testFilmReelSplashScreenRendersAndAllowsSkip() {
    var finished = false
    composeTestRule.setContent {
      BoothTheme {
        FilmReelSplashScreen(
          onFinishLoading = { finished = true }
        )
      }
    }

    composeTestRule.onNodeWithTag("film_reel_splash_screen").assertExists()
    composeTestRule.onNodeWithTag("film_reel_animation", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("splash_title", useUnmergedTree = true).assertExists()

    // Tapping skips directly to main app
    composeTestRule.onNodeWithTag("film_reel_splash_screen").performClick()
    assertTrue("Tap should trigger onFinishLoading", finished)
  }
}
