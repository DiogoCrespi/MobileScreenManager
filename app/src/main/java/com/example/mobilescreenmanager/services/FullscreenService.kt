package com.mobilescreenmanager.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class FullscreenService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
