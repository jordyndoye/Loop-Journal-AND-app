package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
  object Splash : Screen("splash", "Splash")
  object Today : Screen("today", "Today")
  object MySystem : Screen("my_system", "My System")
  object Journal : Screen("journal", "Journal")
  object Insights : Screen("insights", "Insights")
  object Experiments : Screen("experiments", "Experiments")
}
