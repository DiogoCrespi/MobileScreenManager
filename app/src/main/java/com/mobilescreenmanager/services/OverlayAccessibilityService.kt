package com.mobilescreenmanager.services

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.mobilescreenmanager.views.TransparentLayout

class OverlayAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: TransparentLayout? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("OverlayAccessibilityService", "Serviço de acessibilidade conectado")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Nossa layout "transparente" (mesma que antes)
        overlayView = TransparentLayout(this).apply {
            // Opcional: deixar visível uma cor semitransparente só pra testes
            // setBackgroundColor(Color.argb(100, 255, 0, 0))
            isClickable = false
            isLongClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
        }

        // Usar TYPE_ACCESSIBILITY_OVERLAY
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(overlayView, layoutParams)
            Log.d("OverlayAccessibilityService", "Overlay adicionado com TYPE_ACCESSIBILITY_OVERLAY")
        } catch (e: Exception) {
            Log.e("OverlayAccessibilityService", "Erro ao adicionar overlay: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Não precisamos fazer nada aqui se só queremos desenhar uma camada
    }

    override fun onInterrupt() {
        // Nada a fazer
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("OverlayAccessibilityService", "Serviço de acessibilidade destruído")
        overlayView?.let {
            windowManager?.removeView(it)
        }
    }
}
