package com.mobilescreenmanager.services

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FullscreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            "TOGGLE_FULLSCREEN" -> {
                val serviceIntent = Intent(context, FullscreenService::class.java)
                if (isServiceRunning(context, FullscreenService::class.java)) {
                    context.stopService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any { serviceInfo ->
            serviceInfo.service.className == serviceClass.name
        }
    }
}
