package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.experiments.ExperimentsScreen
import com.example.ui.insights.InsightsScreen
import com.example.ui.journal.JournalScreen
import com.example.ui.navigation.BoothBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.splash.FilmReelSplashScreen
import com.example.ui.system.MySystemScreen
import com.example.ui.theme.BoothBlack
import com.example.ui.today.TodayScreen

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
  startWithSplash: Boolean = true
) {
  var isAppOpening by rememberSaveable { mutableStateOf(startWithSplash) }
  var backStack by rememberSaveable { mutableStateOf(listOf(Screen.Today.route)) }
  val currentRoute = backStack.lastOrNull() ?: Screen.Today.route

  // Back handling: pop previous screen from backstack until root (Today)
  BackHandler(enabled = backStack.size > 1 && !isAppOpening) {
    backStack = backStack.dropLast(1)
  }

  Crossfade(
    targetState = isAppOpening,
    animationSpec = tween(durationMillis = 350),
    label = "opening_crossfade"
  ) { showOpening ->
    if (showOpening) {
      FilmReelSplashScreen(
        onFinishLoading = { isAppOpening = false },
        modifier = modifier
      )
    } else {
      val activeScreen = when (currentRoute) {
        Screen.MySystem.route -> Screen.MySystem
        Screen.Journal.route -> Screen.Journal
        Screen.Insights.route -> Screen.Insights
        Screen.Experiments.route -> Screen.Experiments
        Screen.Splash.route -> Screen.Splash
        else -> Screen.Today
      }

      Scaffold(
        modifier = modifier
          .fillMaxSize()
          .testTag("main_scaffold"),
        containerColor = BoothBlack,
        bottomBar = {
          BoothBottomNavBar(
            currentScreen = activeScreen,
            onScreenSelected = { screen ->
              if (screen.route != currentRoute) {
                // If selecting root Today, reset backstack to avoid loops
                if (screen == Screen.Today) {
                  backStack = listOf(Screen.Today.route)
                } else {
                  backStack = backStack.filterNot { it == screen.route } + screen.route
                }
              }
            }
          )
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(BoothBlack)
            .padding(innerPadding)
        ) {
          AnimatedContent(
            targetState = activeScreen,
            transitionSpec = {
              fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                fadeOut(animationSpec = tween(durationMillis = 150))
            },
            label = "tab_crossfade"
          ) { screen ->
            when (screen) {
              Screen.Today -> TodayScreen(
                onNavigateToSystem = {
                  backStack = backStack.filterNot { it == Screen.MySystem.route } + Screen.MySystem.route
                }
              )
              Screen.MySystem -> MySystemScreen()
              Screen.Journal -> JournalScreen()
              Screen.Insights -> InsightsScreen()
              Screen.Experiments -> ExperimentsScreen()
              Screen.Splash -> FilmReelSplashScreen(
                onFinishLoading = {
                  backStack = listOf(Screen.Today.route)
                }
              )
            }
          }
        }
      }
    }
  }
}
