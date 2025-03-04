package com.mobilescreenmanager.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.mobilescreenmanager.views.TransparentLayout

class FullscreenService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: TransparentLayout? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FullscreenService", "Serviço iniciado")

        // Verificação de versão mínima para TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.e("FullscreenService", "ERRO: Android abaixo da API 26. Encerrando serviço.")
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Nossa layout "transparente"
        overlayView = TransparentLayout(this).apply {
            // Garantir que a própria View não seja clicável/focável
            isClickable = false
            isLongClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
        }

        // Flags: não usar FLAG_NOT_TOUCHABLE;
        // Usar NOT_FOCUSABLE + NOT_TOUCH_MODAL para permitir o "click-through"
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(overlayView, layoutParams)
            Log.d("FullscreenService", "Overlay transparente adicionado com sucesso")
        } catch (e: Exception) {
            Log.e("FullscreenService", "Erro ao adicionar overlay: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FullscreenService", "Serviço finalizado")
        overlayView?.let {
            windowManager?.removeView(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
