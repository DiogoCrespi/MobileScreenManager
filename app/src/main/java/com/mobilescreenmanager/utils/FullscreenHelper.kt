package com.example.mobilescreenmanager.utils

import android.content.Context
import android.os.Build
import android.provider.Settings

object FullscreenHelper {
    fun isFullscreenSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
