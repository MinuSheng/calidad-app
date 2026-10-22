package com.sndrlabs.calidadbubble

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnGrantPermission: Button
    private lateinit var btnStartBubble: Button
    private lateinit var btnStopBubble: Button
    private lateinit var btnOpenWeb: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAntiCrashShield()

        try {
            setContentView(R.layout.activity_main)

            tvPermissionStatus = findViewById(R.id.tvPermissionStatus)
            btnGrantPermission = findViewById(R.id.btnGrantPermission)
            btnStartBubble = findViewById(R.id.btnStartBubble)
            btnStopBubble = findViewById(R.id.btnStopBubble)
            btnOpenWeb = findViewById(R.id.btnOpenWeb)

            btnGrantPermission.setOnClickListener {
                requestOverlayPermission()
            }

            btnStartBubble.setOnClickListener {
                try {
                    checkAndRequestNotificationsPermission()
                    if (checkOverlayPermission()) {
                        val serviceIntent = Intent(this, BubbleService::class.java).apply {
                            action = BubbleService.ACTION_START
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(this, serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                        Toast.makeText(this, "⚡ Burbuja activada en segundo plano", Toast.LENGTH_SHORT).show()
                        moveTaskToBack(true)
                    } else {
                        Toast.makeText(this, "⚠️ Requiere permiso de superposición", Toast.LENGTH_LONG).show()
                        requestOverlayPermission()
                    }
                } catch (t: Throwable) {
                    android.util.Log.e("CalidadBubble", "Error al iniciar burbuja", t)
                    Toast.makeText(this, "Error al iniciar burbuja: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }

            btnStopBubble.setOnClickListener {
                try {
                    val stopIntent = Intent(this, BubbleService::class.java).apply {
                        action = BubbleService.ACTION_STOP
                    }
                    startService(stopIntent)
                    Toast.makeText(this, "⏹️ Burbuja detenida", Toast.LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    Toast.makeText(this, "Error al detener: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }

            btnOpenWeb.setOnClickListener {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://minusheng.github.io/calidad-app/"))
                    startActivity(browserIntent)
                } catch (t: Throwable) {
                    Toast.makeText(this, "Error navegador: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }

            checkAndRequestNotificationsPermission()
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en onCreate MainActivity", t)
            Toast.makeText(this, "Error inicio: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAntiCrashShield() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CalidadBubble", "Crash interceptado en ${thread.name}", throwable)
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(
                        applicationContext,
                        "Error en MainActivity: ${throwable.localizedMessage ?: throwable.javaClass.simpleName}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: Throwable) {}
            }
        }
    }

    private fun checkAndRequestNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1002)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUI()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "✅ Permiso de superposición ya otorgado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePermissionUI() {
        val hasPermission = checkOverlayPermission()
        if (hasPermission) {
            tvPermissionStatus.text = "● PERMISO DE SUPERPOSICIÓN: ACTIVO"
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_green))
            btnGrantPermission.visibility = View.GONE
        } else {
            tvPermissionStatus.text = "● PERMISO DE SUPERPOSICIÓN: REQUERIDO"
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
            btnGrantPermission.visibility = View.VISIBLE
        }
    }
}
