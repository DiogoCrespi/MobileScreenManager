package com.mobilescreenmanager.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Surface

class RotationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtém a rotação enviada pelo serviço
        val rotation = intent.getIntExtra("ROTATION", Surface.ROTATION_0)

        // Define a rotação da tela
        requestedOrientation = when (rotation) {
            Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        // Fecha a activity após definir a rotação
        finish()
    }
}
