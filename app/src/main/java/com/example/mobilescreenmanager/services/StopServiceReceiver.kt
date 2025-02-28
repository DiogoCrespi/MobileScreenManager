package com.mobilescreenmanager.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.stopService(Intent(context, ScreenOrientationService::class.java))
        context?.stopService(Intent(context, FullscreenService::class.java))
        context?.stopService(Intent(context, ScreenManagerService::class.java))
    }
}
