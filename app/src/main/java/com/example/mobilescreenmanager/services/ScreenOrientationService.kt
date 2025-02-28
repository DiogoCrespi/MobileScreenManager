package com.mobilescreenmanager.services

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.mobilescreenmanager.services.FullscreenService
import com.mobilescreenmanager.R

class ScreenOrientationService : Service() {

    private var isRotationActive = false
    private var isFullscreenActive = false
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

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
        addOverlay()
    }

    private fun addOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Transparente
            setOnTouchListener { _, _ -> false } // Permite passar o toque para o sistema
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // Agora os toques passam pela sobreposição
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, // Mantém a sobreposição ativa
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

        startForeground(1, notification) // Corrigido: Garante que a notificação sempre aparece
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
