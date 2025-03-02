package com.mobilescreenmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mobilescreenmanager.services.FullscreenService

class FullscreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_ON) {
            Log.d("FullscreenReceiver", "Tela ligada, reativando modo tela cheia")
            context.startService(Intent(context, FullscreenService::class.java))
        }
    }
}
