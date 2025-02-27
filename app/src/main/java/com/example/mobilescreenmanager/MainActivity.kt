package com.mobilescreenmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.mobilescreenmanager.services.FullscreenService
import com.mobilescreenmanager.services.ScreenOrientationService
import com.mobilescreenmanager.ui.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartRotation = findViewById<Button>(R.id.btnStartRotation)
        val btnStartFullscreen = findViewById<Button>(R.id.btnStartFullscreen)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnStartRotation.setOnClickListener {
            startService(Intent(this, ScreenOrientationService::class.java))
        }

        btnStartFullscreen.setOnClickListener {
            startService(Intent(this, FullscreenService::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
