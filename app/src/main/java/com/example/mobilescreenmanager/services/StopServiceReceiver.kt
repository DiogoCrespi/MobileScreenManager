package com.mobilescreenmanager.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobilescreenmanager.services.FullscreenService
import com.mobilescreenmanager.services.ScreenOrientationService
import com.mobilescreenmanager.services.ScreenManagerService

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            it.stopService(Intent(it, FullscreenService::class.java))
            it.stopService(Intent(it, ScreenOrientationService::class.java))
            it.stopService(Intent(it, ScreenManagerService::class.java))
        }
    }
}
