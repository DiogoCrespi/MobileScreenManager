package com.mobilescreenmanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mobilescreenmanager.R

class ScreenManagerService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = "screen_manager_channel"
        val channelName = "Gerenciamento de Tela"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val intent = Intent(this, ScreenManagerService::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullscreenIntent = Intent(this, FullscreenService::class.java).apply {
            action = "TOGGLE_FULLSCREEN"
        }
        val fullscreenPendingIntent = PendingIntent.getService(
            this, 1, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rotationIntent = Intent(this, ScreenOrientationService::class.java).apply {
            action = "TOGGLE_ROTATION"
        }
        val rotationPendingIntent = PendingIntent.getService(
            this, 2, rotationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenManagerService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gerenciamento de Tela")
            .setContentText("Controle os modos de tela")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_fullscreen, "Tela Cheia", fullscreenPendingIntent)
            .addAction(R.drawable.ic_rotation, "Rotação", rotationPendingIntent)
            .addAction(R.drawable.ic_stop, "Desativar", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "TOGGLE_FULLSCREEN" -> startService(Intent(this, FullscreenService::class.java))
            "TOGGLE_ROTATION" -> startService(Intent(this, ScreenOrientationService::class.java))
            "STOP_SERVICE" -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopService(Intent(this, FullscreenService::class.java))
        stopService(Intent(this, ScreenOrientationService::class.java))
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
