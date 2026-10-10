package com.example.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.service.CloudMirror
import com.example.ui.MainScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthGate() {
  val auth = remember { FirebaseAuth.getInstance() }
  var signedIn by remember { mutableStateOf(auth.currentUser != null) }

  DisposableEffect(auth) {
    val listener = FirebaseAuth.AuthStateListener { signedIn = it.currentUser != null }
    auth.addAuthStateListener(listener)
    onDispose { auth.removeAuthStateListener(listener) }
  }

  LaunchedEffect(signedIn) {
    if (signedIn) CloudMirror.saveProfile()
  }

  if (signedIn) MainScreen() else AuthScreen(onSignedIn = { signedIn = true })
}
