package com.aegis.objectfilter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aegis.objectfilter.ui.CameraScreen

class MainActivity : ComponentActivity() {

  private val requestCameraPermission =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        Surface {
          var hasPermission by remember {
            mutableStateOf(
              ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA,
              ) == PackageManager.PERMISSION_GRANTED,
            )
          }

          var requested by remember { mutableStateOf(false) }
          LaunchedEffect(Unit) {
            if (!requested) {
              requested = true
              if (!hasPermission) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
              }
            }
          }

          hasPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.CAMERA,
          ) == PackageManager.PERMISSION_GRANTED

          if (hasPermission) {
            CameraScreen()
          } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(text = "Camera permission is required")
            }
          }
        }
      }
    }
  }
}
