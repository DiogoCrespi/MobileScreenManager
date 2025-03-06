package com.mobilescreenmanager

import android.Manifest
import android.app.AlertDialog
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobilescreenmanager.services.FullscreenService
import com.mobilescreenmanager.services.ScreenManagerService
import com.mobilescreenmanager.services.ScreenOrientationService
import com.mobilescreenmanager.ui.SettingsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartRotation = findViewById<Button>(R.id.btnStartRotation)
        val btnStartFullscreen = findViewById<Button>(R.id.btnStartFullscreen)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnStartScreenManager = findViewById<Button>(R.id.btnStartScreenManager)

        Log.d("MainActivity", "Iniciando MainActivity")

        requestPermissions()

        btnStartRotation.setOnClickListener {
            Log.d("MainActivity", "Botão Rotação clicado")
            startScreenService(ScreenOrientationService::class.java, "Rotação de tela ativada com sucesso!")
        }

        btnStartFullscreen.setOnClickListener {
            Log.d("MainActivity", "Botão Tela Cheia clicado")
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startScreenService(FullscreenService::class.java, "Modo tela cheia ativado!")
            }
        }

        btnSettings.setOnClickListener {
            Log.d("MainActivity", "Botão Configurações clicado")
            try {
                startActivity(Intent(this, SettingsActivity::class.java))
            } catch (e: Exception) {
                showAlert("Erro ao abrir configurações: ${e.message}")
            }
        }

        btnStartScreenManager.setOnClickListener {
            Log.d("MainActivity", "Botão Gerenciador de Tela clicado")
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startScreenService(ScreenManagerService::class.java, "Gerenciamento de tela iniciado!")
            }
        }
    }
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Ative o serviço de acessibilidade para permitir sobreposições!", Toast.LENGTH_LONG).show()
    }

    private fun requestPermissions() {
        try {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } catch (e: Exception) {
            showAlert("Erro ao solicitar permissões: ${e.message}")
        }
    }

    private fun requestOverlayPermission() {
        Log.d("MainActivity", "Solicitando permissão de sobreposição")
        Toast.makeText(this, "Permissão de sobreposição necessária!", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_CODE_OVERLAY)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showPermissionDeniedDialog()
            }
        }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão necessária")
            .setMessage("O aplicativo precisa da permissão de notificações para funcionar corretamente. Deseja conceder agora?")
            .setPositiveButton("Sim") { _, _ -> openAppSettings() }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showAlert(message: String) {
        Log.e("MainActivity", message)
        AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun <T> startScreenService(serviceClass: Class<T>, successMessage: String) {
        try {
            if (!isServiceRunning(serviceClass)) {
                val intent = Intent(this, serviceClass)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                showAlert(successMessage)
                Log.d("MainActivity", "$successMessage iniciado com sucesso")
            } else {
                showAlert("O serviço já está em execução!")
                Log.d("MainActivity", "Serviço já em execução: ${serviceClass.name}")
            }
        } catch (e: Exception) {
            showAlert("Erro ao iniciar serviço: ${e.message}")
        }
    }

    private fun <T> isServiceRunning(serviceClass: Class<T>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val REQUEST_CODE_OVERLAY = 1001
    }
}
