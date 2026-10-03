package com.example.ui.navigation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim

data class NavItem(
  val screen: Screen,
  val label: String,
  val icon: ImageVector,
  val testTag: String
)

val navigationItems = listOf(
  NavItem(Screen.Today, "Today", Icons.Outlined.RadioButtonChecked, "nav_today"),
  NavItem(Screen.MySystem, "My System", Icons.Outlined.ViewAgenda, "nav_my_system"),
  NavItem(Screen.Journal, "Journal", Icons.Outlined.GraphicEq, "nav_journal"),
  NavItem(Screen.Insights, "Insights", Icons.Outlined.Timeline, "nav_insights"),
  NavItem(Screen.Experiments, "Experiments", Icons.Outlined.Explore, "nav_experiments")
)

@Composable
fun BoothBottomNavBar(
  currentScreen: Screen,
  onScreenSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(BoothBlack)
      .border(width = 1.dp, color = BoothBorder)
      .navigationBarsPadding()
      .testTag("booth_bottom_navigation")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      navigationItems.forEach { item ->
        val isSelected = currentScreen == item.screen
        val activeColor = if (isSelected) BoothAmber else BoothDim

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onScreenSelected(item.screen) }
            .testTag(item.testTag)
        ) {
          Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = activeColor,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = item.label,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 10.sp,
            letterSpacing = 0.3.sp,
            color = activeColor
          )
        }
      }
    }
  }
}
