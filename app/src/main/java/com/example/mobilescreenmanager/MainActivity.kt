package com.mobilescreenmanager

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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

        requestPermissions()

        btnStartRotation.setOnClickListener {
            try {
                startService(Intent(this, ScreenOrientationService::class.java))
                showAlert("Rotação de tela ativada com sucesso!")
            } catch (e: Exception) {
                showAlert("Erro ao ativar rotação de tela: ${e.message}")
            }
        }

        btnStartFullscreen.setOnClickListener {
            try {
                startService(Intent(this, FullscreenService::class.java))
                showAlert("Modo tela cheia ativado!")
            } catch (e: Exception) {
                showAlert("Erro ao ativar modo tela cheia: ${e.message}")
            }
        }

        btnSettings.setOnClickListener {
            try {
                startActivity(Intent(this, SettingsActivity::class.java))
            } catch (e: Exception) {
                showAlert("Erro ao abrir configurações: ${e.message}")
            }
        }

        btnStartScreenManager.setOnClickListener {
            try {
                startService(Intent(this, ScreenManagerService::class.java))
                showAlert("Gerenciamento de tela iniciado!")
            } catch (e: Exception) {
                showAlert("Erro ao iniciar gerenciamento de tela: ${e.message}")
            }
        }
    }

    private fun requestPermissions() {
        try {
            // Permissão para sobrepor tela
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Permissão de sobreposição necessária!", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }

            // Permissão para notificações (Android 13+)
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
            .setPositiveButton("Sim") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
