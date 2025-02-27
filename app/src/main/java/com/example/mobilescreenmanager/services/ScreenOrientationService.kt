package com.mobilescreenmanager.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.mobilescreenmanager.ui.RotationActivity

class ScreenOrientationService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val displayContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            applicationContext.createDisplayContext(display!!)
        } else {
            applicationContext
        }

        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            displayContext.display?.rotation ?: Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        }

        val newRotation = when (rotation) {
            Surface.ROTATION_0 -> Surface.ROTATION_90
            Surface.ROTATION_90 -> Surface.ROTATION_0
            else -> Surface.ROTATION_0
        }

        forceRotation(newRotation)
        return START_STICKY
    }

    private fun forceRotation(rotation: Int) {
        val newIntent = Intent(this, RotationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("ROTATION", rotation)
        }
        startActivity(newIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
