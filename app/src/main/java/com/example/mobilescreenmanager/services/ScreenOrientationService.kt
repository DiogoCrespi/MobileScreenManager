package com.mobilescreenmanager.services

import android.app.Service
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.IBinder
import android.view.WindowManager
import android.widget.Toast

class ScreenOrientationService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao definir a rotação!", Toast.LENGTH_SHORT).show()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
