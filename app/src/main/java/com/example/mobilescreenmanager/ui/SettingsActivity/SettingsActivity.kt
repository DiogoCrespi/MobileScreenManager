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

        val switchRotation = findViewById<Switch>(R.id.switchRotation)
        val switchFullscreen = findViewById<Switch>(R.id.switchFullscreen)

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
