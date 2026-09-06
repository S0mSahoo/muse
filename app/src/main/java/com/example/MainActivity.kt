package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.di.AppContainer
import com.example.ui.auth.AuthGate
import com.example.ui.theme.MUSETheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppContainer.init(this)
    enableEdgeToEdge()
    setContent {
      MUSETheme {
        AuthGate()
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (isFinishing) {
      AppContainer.playbackProvider.release()
    }
  }
}
