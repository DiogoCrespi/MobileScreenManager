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
    private var isFullscreenActive = true

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
            setBackgroundColor(0x00000000) // Transparente
            setOnTouchListener { _, event ->
                Log.d("FullscreenService", "Toque detectado na posição: (${event.x}, ${event.y})")

                if (event.action == MotionEvent.ACTION_DOWN) {
                    Log.d("FullscreenService", "Toque detectado, verificando status da tela cheia")
                    if (!isFullscreenActive) {
                        isFullscreenActive = true
                        applyFullscreenMode(this)
                    }
                }
                false // Permite que os toques passem para os apps abaixo
            }

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
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // Permite que os toques passem
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START

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

                    // Permite exibir a barra de status ao deslizar, mas mantém a interação na tela toda
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                    Log.d("FullscreenService", "Modo tela cheia ativado e interativo")
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
