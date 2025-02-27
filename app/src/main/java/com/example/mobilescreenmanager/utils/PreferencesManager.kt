package com.mobilescreenmanager.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun setRotationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("rotation_enabled", enabled).apply()
    }

    fun isRotationEnabled(): Boolean {
        return prefs.getBoolean("rotation_enabled", false)
    }

    fun setFullscreenEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("fullscreen_enabled", enabled).apply()
    }

    fun isFullscreenEnabled(): Boolean {
        return prefs.getBoolean("fullscreen_enabled", false)
    }
}
