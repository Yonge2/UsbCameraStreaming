package com.example.usbcamera.ui

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.usbcamera.UsbCameraHelper

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val usbCameraHelper = remember { UsbCameraHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            usbCameraHelper.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val surfaceView = SurfaceView(it)
                surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        usbCameraHelper.startPreview(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        // 필요 시 처리
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        usbCameraHelper.close()
                    }
                })
                surfaceView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

