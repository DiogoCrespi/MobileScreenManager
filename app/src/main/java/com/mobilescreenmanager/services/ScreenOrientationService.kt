package com.mobilescreenmanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.mobilescreenmanager.R
import com.mobilescreenmanager.services.FullscreenService

class ScreenOrientationService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private var lastOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    private var isRotationActive = false
    private var isFullscreenActive = false
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "TOGGLE_ROTATION" -> {
                isRotationActive = !isRotationActive
                updateModes()
            }
            "TOGGLE_FULLSCREEN" -> {
                isFullscreenActive = !isFullscreenActive
                if (isFullscreenActive) {
                    startService(Intent(this, FullscreenService::class.java))
                } else {
                    stopService(Intent(this, FullscreenService::class.java))
                }
                updateModes()
            }
            "TOGGLE_BOTH" -> {
                isRotationActive = true
                isFullscreenActive = true
                updateModes()
            }
            "STOP_SERVICE" -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        showNotification()
        return START_STICKY
    }

    private fun updateModes() {
        showNotification()
        if (isFullscreenActive) {
            addOverlay()
        } else {
            removeOverlay()
        }
    }

    private fun addOverlay() {
        if (overlayView != null) return

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Transparente
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(overlayView, layoutParams)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun applyScreenOrientation(orientation: Int) {
        val intent = Intent("com.mobilescreenmanager.ORIENTATION_CHANGED")
        intent.putExtra("screen_orientation", orientation)
        sendBroadcast(intent)
    }

    private fun showNotification() {
        val channelId = "screen_manager_channel"
        val channelName = "Gerenciamento de Tela"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }


        val rotationIntent = Intent(this, ScreenOrientationService::class.java).apply { action = "TOGGLE_ROTATION" }
        val rotationPendingIntent = PendingIntent.getService(this, 0, rotationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val fullscreenIntent = Intent(this, ScreenOrientationService::class.java).apply { action = "TOGGLE_FULLSCREEN" }
        val fullscreenPendingIntent = PendingIntent.getService(this, 1, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val bothIntent = Intent(this, ScreenOrientationService::class.java).apply { action = "TOGGLE_BOTH" }
        val bothPendingIntent = PendingIntent.getService(this, 2, bothIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, ScreenOrientationService::class.java).apply { action = "STOP_SERVICE" }
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val activeModes = mutableListOf<String>()
        if (isRotationActive) activeModes.add("Rotação")
        if (isFullscreenActive) activeModes.add("Tela Cheia")
        val activeText = if (activeModes.isEmpty()) "Nenhum modo ativo" else "Ativo: ${activeModes.joinToString(", ")}"

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gerenciamento de Tela")
            .setContentText(activeText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.ic_rotation, "Rotação", rotationPendingIntent)
            .addAction(R.drawable.ic_fullscreen, "Tela Cheia", fullscreenPendingIntent)
            .addAction(R.drawable.ic_both, "Rotação e Tela Cheia", bothPendingIntent)
            .addAction(R.drawable.ic_stop, "Desativar", stopPendingIntent)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)

        startForeground(1, notification) // Garante que a notificação sempre aparece
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRotationActive) return

        event?.let {
            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)

            SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
            SensorManager.getOrientation(rotationMatrix, orientationValues)

            val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()

            val newOrientation = when {
                pitch > 45 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                pitch < -45 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                roll > 45 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                roll < -45 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

            if (newOrientation != lastOrientation) {
                lastOrientation = newOrientation
                Log.d("ScreenOrientationService", "Aplicando nova orientação: $newOrientation")
                applyScreenOrientation(newOrientation)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
