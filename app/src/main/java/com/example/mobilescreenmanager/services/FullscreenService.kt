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

        // Verifica se a permissão de sobreposição foi concedida
        if (!Settings.canDrawOverlays(this)) {
            Log.e("FullscreenService", "Permissão de sobreposição não concedida. Encerrando serviço.")
            stopSelf()
            return START_NOT_STICKY
        }

        if (overlayView != null) {
            Log.d("FullscreenService", "Overlay já existe, ignorando nova criação")
            return START_STICKY
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setOnClickListener { stopSelf() } // Tocando na tela, desativa a tela cheia
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE, // API 24-25 usa TYPE_PHONE
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager.addView(overlayView, layoutParams)
            Log.d("FullscreenService", "Overlay adicionado com sucesso")

            // Adiciona um atraso antes de ocultar a barra de status para evitar falhas de sessão
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
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    Log.d("FullscreenService", "Modo tela cheia ativado com WindowInsetsController")
                } else {
                    Log.e("FullscreenService", "WindowInsetsController é nulo, falha ao ocultar status bars")
                }
            }
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            Log.d("FullscreenService", "Modo tela cheia ativado via systemUiVisibility")
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
