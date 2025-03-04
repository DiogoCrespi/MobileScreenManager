package com.mobilescreenmanager.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.app.Activity

class ScreenOrientationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.mobilescreenmanager.ORIENTATION_CHANGED") {
            val newOrientation = intent.getIntExtra("screen_orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            Log.d("ScreenOrientationReceiver", "Recebendo nova orientação: $newOrientation")

            if (context is Activity) {
                context.requestedOrientation = newOrientation
            }
        }
    }
}
