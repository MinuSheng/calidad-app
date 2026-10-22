package com.sndrlabs.calidadbubble

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvPermissionStatus: TextView
    private lateinit var btnGrantPermission: Button
    private lateinit var btnStartBubble: Button
    private lateinit var btnStopBubble: Button
    private lateinit var btnOpenWeb: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        }

        btnStopBubble.setOnClickListener {
            val stopIntent = Intent(this, BubbleService::class.java).apply {
                action = BubbleService.ACTION_STOP
            }
            startService(stopIntent)
            Toast.makeText(this, "⏹️ Burbuja detenida", Toast.LENGTH_SHORT).show()
        }

        btnOpenWeb.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://minusheng.github.io/calidad-app/"))
            startActivity(browserIntent)
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
            tvPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.neon_purple))
            btnGrantPermission.visibility = View.VISIBLE
        }
    }
}
