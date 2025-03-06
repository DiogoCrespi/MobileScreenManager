package com.mobilescreenmanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mobilescreenmanager.R

class FullscreenService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val TAG = "FullscreenService"

    override fun onCreate() {
        super.onCreate()
        startForeground(1001, createNotification())

        if (checkOverlayPermission()) {
            startFullscreenMode()
        } else {
            Log.e(TAG, "Permissão SYSTEM_ALERT_WINDOW não concedida")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun startFullscreenMode() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Totalmente transparente

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    sendTouchBroadcast(event.rawX, event.rawY)
                }
                false // Permite que os eventos de toque passem para a tela abaixo
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, // Isso garante que os toques passem para o fundo
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(overlayView, layoutParams)
            Log.d(TAG, "Overlay adicionada com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar overlay: ${e.message}")
            stopSelf()
        }
    }

    private fun sendTouchBroadcast(x: Float, y: Float) {
        val intent = Intent("TOUCH_EVENT")
        intent.putExtra("X", x)
        intent.putExtra("Y", y)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "Toque detectado na posição: ($x, $y)")
    }

    private fun createNotification(): Notification {
        val channelId = "fullscreen_service"

        // ✅ Criar o canal APENAS se a versão do Android for 8.0 (API 26) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fullscreen Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fullscreen Mode Ativo")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ajuste o ícone conforme necessário
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        Log.d(TAG, "Overlay removida e serviço destruído")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
