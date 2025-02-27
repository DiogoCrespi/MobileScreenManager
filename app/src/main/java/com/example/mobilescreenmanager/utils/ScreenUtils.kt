package com.mobilescreenmanager.utils

import android.app.Activity
import android.view.View

object ScreenUtils {
    fun enableFullscreen(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}
