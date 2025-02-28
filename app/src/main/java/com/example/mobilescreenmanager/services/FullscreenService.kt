package com.mobilescreenmanager.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*

class FullscreenService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FullscreenService", "Iniciando serviço de Tela Cheia no Android ${Build.VERSION.SDK_INT}")

        if (!Settings.canDrawOverlays(this)) {
            Log.e("FullscreenService", "Permissão de sobreposição não concedida.")
            stopSelf()
            return START_NOT_STICKY
        }

        if (overlayView != null) {
            Log.d("FullscreenService", "Overlay já existe, ignorando nova criação")
            return START_STICKY
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Invisível
            setOnApplyWindowInsetsListener { _, insets ->
                Log.d("FullscreenService", "Interceptando insets, reativando tela cheia")
                applyFullscreenMode(this)
                insets
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // Permite que os toques passem pela sobreposição
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(overlayView, layoutParams)
            Log.d("FullscreenService", "Overlay adicionado com sucesso")

            Handler(Looper.getMainLooper()).postDelayed({
                applyFullscreenMode(overlayView!!)
            }, 500)
        } catch (e: Exception) {
            Log.e("FullscreenService", "Erro ao adicionar overlay: ${e.message}")
        }

        return START_STICKY
    }

    private fun applyFullscreenMode(view: View) {
        Log.d("FullscreenService", "Aplicando modo tela cheia")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.post {
                val controller = view.windowInsetsController
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE // Agora só reaparece ao deslizar
                    Log.d("FullscreenService", "Modo tela cheia ativado com WindowInsetsController")
                } else {
                    Log.e("FullscreenService", "WindowInsetsController é nulo, falha ao ocultar status bars")
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d("FullscreenService", "Destruindo serviço de Tela Cheia")

        overlayView?.let {
            try {
                windowManager.removeView(it)
                Log.d("FullscreenService", "Overlay removido com sucesso")
            } catch (e: Exception) {
                Log.e("FullscreenService", "Erro ao remover overlay: ${e.message}")
            }
            overlayView = null
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
