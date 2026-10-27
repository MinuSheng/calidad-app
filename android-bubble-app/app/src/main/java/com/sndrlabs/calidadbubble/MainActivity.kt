package com.sndrlabs.calidadbubble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
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

    // Componentes de la consola de diagnóstico de logs
    private lateinit var layoutLogHeader: LinearLayout
    private lateinit var layoutLogContent: LinearLayout
    private lateinit var tvLogTitle: TextView
    private lateinit var tvLogCount: TextView
    private lateinit var tvLogToggleIcon: TextView
    private lateinit var scrollLogViewer: ScrollView
    private lateinit var tvLogViewer: TextView
    private lateinit var btnCopyLogs: Button
    private lateinit var btnClearLogs: Button

    private var isLogExpanded = true
    private val logListener: () -> Unit = {
        runOnUiThread {
            renderLogs()
        }
    }

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

            // Inicializar sección de logs y diagnóstico
            layoutLogHeader = findViewById(R.id.layoutLogHeader)
            layoutLogContent = findViewById(R.id.layoutLogContent)
            tvLogTitle = findViewById(R.id.tvLogTitle)
            tvLogCount = findViewById(R.id.tvLogCount)
            tvLogToggleIcon = findViewById(R.id.tvLogToggleIcon)
            scrollLogViewer = findViewById(R.id.scrollLogViewer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scrollLogViewer.isNestedScrollingEnabled = true
            }
            tvLogViewer = findViewById(R.id.tvLogViewer)
            btnCopyLogs = findViewById(R.id.btnCopyLogs)
            btnClearLogs = findViewById(R.id.btnClearLogs)

            layoutLogHeader.setOnClickListener {
                isLogExpanded = !isLogExpanded
                layoutLogContent.visibility = if (isLogExpanded) View.VISIBLE else View.GONE
                tvLogToggleIcon.text = if (isLogExpanded) "▲" else "▼"
            }

            btnCopyLogs.setOnClickListener {
                try {
                    val logsText = AppLogManager.exportLogs()
                    if (logsText.isBlank()) {
                        Toast.makeText(this, "No hay registros para copiar", Toast.LENGTH_SHORT).show()
                    } else {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("CalidadBubbleLogs", logsText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "📋 Registros copiados al portapapeles", Toast.LENGTH_SHORT).show()
                    }
                } catch (t: Throwable) {
                    Toast.makeText(this, "Error al copiar: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }

            btnClearLogs.setOnClickListener {
                AppLogManager.clearLogs()
                Toast.makeText(this, "🗑️ Registros limpiados", Toast.LENGTH_SHORT).show()
            }

            AppLogManager.addListener(logListener)
            renderLogs()
            AppLogManager.log("SERVICE", "MainActivity iniciada (v4.0.31)")

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
                        AppLogManager.log("SERVICE", "Iniciando BubbleService...")
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
                    AppLogManager.log("ERROR", "Error al iniciar burbuja: ${t.message}")
                    Toast.makeText(this, "Error al iniciar burbuja: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }

            btnStopBubble.setOnClickListener {
                try {
                    val stopIntent = Intent(this, BubbleService::class.java).apply {
                        action = BubbleService.ACTION_STOP
                    }
                    AppLogManager.log("SERVICE", "Deteniendo BubbleService...")
                    startService(stopIntent)
                    Toast.makeText(this, "⏹️ Burbuja detenida", Toast.LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    AppLogManager.log("ERROR", "Error al detener: ${t.message}")
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
            AppLogManager.log("ERROR", "Error en onCreate MainActivity: ${t.message}")
            Toast.makeText(this, "Error inicio: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun renderLogs() {
        val entries = AppLogManager.getLogs()
        tvLogCount.text = "(${entries.size})"
        if (entries.isEmpty()) {
            tvLogViewer.text = "[SISTEMA] Sin eventos registrados aún."
            return
        }

        val ssb = SpannableStringBuilder()
        for (entry in entries) {
            val start = ssb.length
            val line = "${entry.timestamp} ${entry.tag} ${entry.message}\n"
            ssb.append(line)

            val tagUpper = entry.tag.uppercase()
            val msgLower = entry.message.lowercase()

            val color = when {
                tagUpper.contains("ERROR") || msgLower.contains("error") || msgLower.contains("crash") || msgLower.contains("fallo") ->
                    Color.parseColor("#F87171") // Rojo coral
                tagUpper.contains("SUCCESS") || msgLower.contains("completad") || msgLower.contains("activo") || msgLower.contains("éxito") || msgLower.contains("exito") ->
                    Color.parseColor("#34D399") // Verde menta
                tagUpper.contains("PICKER") ->
                    Color.parseColor("#38BDF8") // Azul cielo
                tagUpper.contains("SERVICE") ->
                    Color.parseColor("#00F2FE") // Cian neón
                tagUpper.contains("WEB") ->
                    Color.parseColor("#C084FC") // Violeta suave
                else ->
                    Color.parseColor("#94A3B8") // Slate neutro
            }

            ssb.setSpan(
                ForegroundColorSpan(color),
                start,
                start + line.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        tvLogViewer.text = ssb
        scrollLogViewer.post {
            scrollLogViewer.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setupAntiCrashShield() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CalidadBubble", "Crash interceptado en ${thread.name}", throwable)
            AppLogManager.log("ERROR", "Crash en ${thread.name}: ${throwable.localizedMessage ?: throwable.javaClass.simpleName}")
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
        renderLogs()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogManager.removeListener(logListener)
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
