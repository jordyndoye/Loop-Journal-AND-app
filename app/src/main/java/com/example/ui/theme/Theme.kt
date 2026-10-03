package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BoothColorScheme = darkColorScheme(
  primary = BoothAmber,
  onPrimary = BoothInk,
  primaryContainer = BoothSurfaceElevated,
  onPrimaryContainer = BoothPaper,
  secondary = BoothPaper,
  onSecondary = BoothInk,
  secondaryContainer = BoothSurface,
  onSecondaryContainer = BoothPaper,
  tertiary = BoothDim,
  onTertiary = BoothPaper,
  background = BoothBlack,
  onBackground = BoothPaper,
  surface = BoothBlack,
  onSurface = BoothPaper,
  surfaceVariant = BoothSurface,
  onSurfaceVariant = BoothDim,
  outline = BoothBorder,
  outlineVariant = BoothBorderSubtle,
  error = BoothMissedGraphite,
  onError = BoothPaper,
  errorContainer = BoothSurface,
  onErrorContainer = BoothPaper
)

@Composable
fun BoothTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = BoothColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
