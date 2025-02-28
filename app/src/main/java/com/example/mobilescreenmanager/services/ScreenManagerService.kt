package com.mobilescreenmanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.mobilescreenmanager.MainActivity
import com.mobilescreenmanager.R

class ScreenManagerService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = "screen_manager_channel"
        val channelName = "Gerenciador de Tela"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gerenciamento de Tela Ativo")
            .setContentText("Escolha um modo para ativar")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_menu_slideshow, "Ativar Tela Cheia", getPendingIntent(FullscreenService::class.java))
            .addAction(R.drawable.ic_menu_camera, "Ativar Rotação", getPendingIntent(ScreenOrientationService::class.java))
            .build()
    }

    private fun getPendingIntent(serviceClass: Class<out Service>): PendingIntent {
        val intent = Intent(this, serviceClass)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
