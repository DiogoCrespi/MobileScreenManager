package com.mobilescreenmanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Switch
import com.mobilescreenmanager.R
import com.mobilescreenmanager.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferencesManager = PreferencesManager(this)

        val switchRotation: Switch = findViewById(R.id.switchRotation)
        val switchFullscreen: Switch = findViewById(R.id.switchFullscreen)

        switchRotation.isChecked = preferencesManager.isRotationEnabled()
        switchFullscreen.isChecked = preferencesManager.isFullscreenEnabled()

        switchRotation.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setRotationEnabled(isChecked)
        }

        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setFullscreenEnabled(isChecked)
        }
    }
}
