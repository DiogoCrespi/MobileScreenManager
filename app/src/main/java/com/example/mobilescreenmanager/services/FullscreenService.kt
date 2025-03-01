package com.mobilescreenmanager.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FullscreenService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isFullscreenActive = false

    override fun onCreate() {
        super.onCreate()
        startFullscreenMode()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startFullscreenMode() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Overlay transparente

            setOnTouchListener { _, event ->
                Log.d("FullscreenService", "Toque detectado na posição: (${event.x}, ${event.y})")

                if (event.action == MotionEvent.ACTION_DOWN) {
                    Log.d("FullscreenService", "Toque detectado, verificando status da tela cheia")
                    if (!isFullscreenActive) {
                        isFullscreenActive = true
                        applyFullscreenMode(this)
                    }
                    performClick() // Conformidade com acessibilidade
                }

                return@setOnTouchListener false  // Permite que os toques passem para os apps abaixo
            }
        }

        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                LayoutParams.TYPE_SYSTEM_ALERT,
            LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    LayoutParams.FLAG_NOT_FOCUSABLE or
                    LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(overlayView, layoutParams)
        Log.d("FullscreenService", "Overlay adicionado com sucesso")
    }

    private fun applyFullscreenMode(view: View) {
        Log.d("FullscreenService", "Aplicando modo tela cheia")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.post {
                val controller = view.windowInsetsController
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    Log.d("FullscreenService", "Modo tela cheia ativado e interativo")
                } else {
                    Log.e("FullscreenService", "WindowInsetsController é nulo, falha ao ocultar status bars")
                }
            }
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        Log.d("FullscreenService", "Overlay removido e serviço destruído")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
