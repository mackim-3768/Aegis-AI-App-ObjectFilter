package com.aegis.objectfilter.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aegis.objectfilter.ui.overlay.WarningBanner

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsState()

  val previewView = remember {
    PreviewView(context).apply {
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    LaunchedBindCamera(
      previewView = previewView,
      lifecycleOwner = lifecycleOwner,
      viewModel = viewModel,
    )

    WarningBanner(uiState = uiState)
  }
}

@Composable
private fun LaunchedBindCamera(
  previewView: PreviewView,
  lifecycleOwner: androidx.lifecycle.LifecycleOwner,
  viewModel: CameraViewModel,
) {
  val context = LocalContext.current
  androidx.compose.runtime.LaunchedEffect(previewView) {
    viewModel.bindCamera(context, lifecycleOwner, previewView)
  }
}
