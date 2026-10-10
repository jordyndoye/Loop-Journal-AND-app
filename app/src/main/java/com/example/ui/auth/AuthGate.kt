package com.example.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.MainScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthGate() {
  val auth = remember { FirebaseAuth.getInstance() }
  var signedIn by remember { mutableStateOf(auth.currentUser != null) }
  if (signedIn) {
    MainScreen()
  } else {
    AuthScreen(onSignedIn = { signedIn = true })
  }
}
