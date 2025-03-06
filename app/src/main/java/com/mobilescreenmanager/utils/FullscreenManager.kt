package com.mobilescreenmanager.utils

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController

class FullscreenManager(private val context: Context) {

    fun applyFullscreenMode(view: View) {
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
}
