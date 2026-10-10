package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BoothAmber
import com.example.ui.theme.BoothBlack
import com.example.ui.theme.BoothBorder
import com.example.ui.theme.BoothDim
import com.example.ui.theme.BoothInk
import com.example.ui.theme.BoothPaper
import com.example.ui.theme.BoothSurface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(onSignedIn: () -> Unit) {
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var creating by rememberSaveable { mutableStateOf(false) }
  var busy by rememberSaveable { mutableStateOf(false) }
  var error by rememberSaveable { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BoothBlack)
      .padding(horizontal = 28.dp, vertical = 72.dp),
    verticalArrangement = Arrangement.Top
  ) {
    Text("LOOP JOURNAL", color = BoothDim, fontSize = 12.sp, letterSpacing = 2.sp)
    Spacer(Modifier.height(12.dp))
    Text(
      if (creating) "Create a profile" else "Sign in",
      color = BoothPaper,
      fontSize = 32.sp,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Normal
    )
    Spacer(Modifier.height(28.dp))
    AuthField(email, { email = it }, "Email", KeyboardType.Email)
    Spacer(Modifier.height(12.dp))
    AuthField(password, { password = it }, "Password", KeyboardType.Password, hidden = true)
    if (error != null) {
      Spacer(Modifier.height(12.dp))
      Text(error!!, color = BoothAmber, fontSize = 14.sp)
    }
    Spacer(Modifier.height(24.dp))
    Button(
      onClick = {
        if (busy) return@Button
        busy = true
        error = null
        CoroutineScope(Dispatchers.Main).launch {
          try {
            val auth = FirebaseAuth.getInstance()
            if (creating) {
              auth.createUserWithEmailAndPassword(email.trim(), password).await()
            } else {
              auth.signInWithEmailAndPassword(email.trim(), password).await()
            }
            onSignedIn()
          } catch (t: Throwable) {
            error = t.message ?: "Could not sign in."
          } finally {
            busy = false
          }
        }
      },
      enabled = email.isNotBlank() && password.length >= 6 && !busy,
      modifier = Modifier.fillMaxWidth().height(52.dp),
      colors = ButtonDefaults.buttonColors(containerColor = BoothAmber, contentColor = BoothInk)
    ) {
      Text(if (creating) "Create profile" else "Sign in")
    }
    TextButton(onClick = { creating = !creating; error = null }) {
      Text(
        if (creating) "I already have a profile" else "Create a profile",
        color = BoothPaper
      )
    }
  }
}

@Composable
private fun AuthField(
  value: String,
  onChange: (String) -> Unit,
  label: String,
  keyboard: KeyboardType,
  hidden: Boolean = false
) {
  OutlinedTextField(
    value = value,
    onValueChange = onChange,
    label = { Text(label) },
    singleLine = true,
    visualTransformation = if (hidden) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions = KeyboardOptions(keyboardType = keyboard),
    modifier = Modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
      focusedTextColor = BoothPaper,
      unfocusedTextColor = BoothPaper,
      focusedBorderColor = BoothAmber,
      unfocusedBorderColor = BoothBorder,
      focusedLabelColor = BoothAmber,
      unfocusedLabelColor = BoothDim,
      cursorColor = BoothAmber,
      focusedContainerColor = BoothSurface,
      unfocusedContainerColor = BoothSurface
    )
  )
}
