package com.sndrlabs.calidadbubble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import kotlin.math.hypot

class BubbleService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "calidad_bubble_channel"
        private const val DOWNLOADS_CHANNEL_ID = "calidad_downloads_channel"
        private const val NOTIFICATION_ID = 1001
        private const val DRAG_THRESHOLD_PX = 10
        var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    }

    private lateinit var windowManager: WindowManager
    private var bubbleHeadView: View? = null
    private var bubblePanelView: View? = null
    private var webContainer: FrameLayout? = null
    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var isExpanded = false
    private var isBubbleAttached = false
    private var isPanelAttached = false

    // Monitor nativo horario de Production Pack
    val confirmedPackHours: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())
    private var lastVibratedHour: String? = null
    private val packMonitorHandler = Handler(Looper.getMainLooper())
    private val packMonitorRunnable = object : Runnable {
        override fun run() {
            checkHourlyProductionPack()
            packMonitorHandler.postDelayed(this, 30_000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupAntiCrashShield()

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            createNotificationChannel()
            startForegroundServiceWithNotification()
            AppLogManager.log("SERVICE", "BubbleService creado (v4.0.30)")
            setupProgrammaticViews()
            showBubbleHead()

            // Iniciar monitor horario de Production Pack (cada 30s)
            packMonitorHandler.post(packMonitorRunnable)
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error crítico en onCreate de BubbleService", t)
            showToast("Error en BubbleService: ${t.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_STOP -> {
                    stopSelf()
                    return START_NOT_STICKY
                }
                ACTION_START -> {
                    showBubbleHead()
                }
                else -> {
                    showBubbleHead()
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en onStartCommand", t)
            showToast("Error al activar: ${t.message}")
        }
        return START_STICKY
    }

    private fun setupAntiCrashShield() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CalidadBubble", "Crash interceptado en ${thread.name}", throwable)
            showToast("Calidad Bubble: ${throwable.localizedMessage ?: throwable.javaClass.simpleName}")
        }
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            } catch (_: Throwable) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Canal persistente del servicio
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Burbuja Calidad",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación de servicio persistente"
                setShowBadge(false)
            }
            manager?.createNotificationChannel(channel)

            // Canal para descargas nativas
            val dlChannel = NotificationChannel(
                DOWNLOADS_CHANNEL_ID,
                "Descargas Calidad App",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de archivos generados y descargados"
                setShowBadge(true)
            }
            manager?.createNotificationChannel(dlChannel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        try {
            val openAppIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Burbuja de Calidad Activa")
                .setContentText("Toca para abrir la aplicación de calidad (v4.0.30)")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en startForegroundServiceWithNotification", t)
            showToast("Error en notificación: ${t.message}")
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun canDrawOverlaysSafe(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun setupProgrammaticViews() {
        try {
            val bubbleSize = dpToPx(62)
            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            // 1. LayoutParams de la Burbuja
            bubbleParams = WindowManager.LayoutParams(
                bubbleSize,
                bubbleSize,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = dpToPx(16)
                y = dpToPx(120)
            }

            // 2. Construcción programática de la Burbuja Flotante con app_logo oficial
            val bubbleFrame = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(bubbleSize, bubbleSize)
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1E293B"))
                ).apply {
                    shape = GradientDrawable.OVAL
                    setStroke(dpToPx(2), Color.parseColor("#00F2FE"))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = dpToPx(6).toFloat()
                }
            }

            val bubbleLogo = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(R.drawable.app_logo)
                scaleType = ImageView.ScaleType.FIT_CENTER
                val p = dpToPx(8)
                setPadding(p, p, p, p)
                contentDescription = "Logo Calidad App"
            }
            bubbleFrame.addView(bubbleLogo)
            bubbleHeadView = bubbleFrame
            setupBubbleTouchListener()

            // 3. LayoutParams del Panel Expandido
            val metrics = resources.displayMetrics
            val panelWidth = (metrics.widthPixels * 0.92f).toInt()
            val panelHeight = (metrics.heightPixels * 0.82f).toInt()

            panelParams = WindowManager.LayoutParams(
                panelWidth,
                panelHeight,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            // 4. Panel Expandido con Borde Cian Neón y Dark Slate
            val panelContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(16).toFloat()
                    setColor(Color.parseColor("#0F172A"))
                    setStroke(dpToPx(2), Color.parseColor("#00F2FE"))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = dpToPx(10).toFloat()
                    clipToOutline = true
                }
            }

            // Barra superior (Header)
            val headerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(48)
                )
                setBackgroundColor(Color.parseColor("#1E293B"))
                setPadding(dpToPx(14), 0, dpToPx(10), 0)
            }

            val tvTitle = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "⚡ Calidad App v4.0.30"
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                typeface = Typeface.DEFAULT_BOLD
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER_VERTICAL
            }
            headerLayout.addView(tvTitle)

            val btnReload = createHeaderButton("🔄") {
                webView?.reload()
            }
            val btnMinimize = createHeaderButton("➖") {
                collapseToBubble()
            }
            val btnClose = createHeaderButton("❌") {
                collapseToBubble()
            }

            headerLayout.addView(btnReload)
            headerLayout.addView(btnMinimize)
            headerLayout.addView(btnClose)
            setupHeaderDragListener(headerLayout)
            panelContainer.addView(headerLayout)

            // Barra de progreso horizontal
            val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(3)
                )
                max = 100
                visibility = View.GONE
            }
            progressBar = pb
            panelContainer.addView(pb)

            // Contenedor dinámico del WebView
            val container = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                setBackgroundColor(Color.parseColor("#0F172A"))
            }
            webContainer = container
            panelContainer.addView(container)

            bubblePanelView = panelContainer

        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en setupProgrammaticViews", t)
            showToast("Error al inicializar vistas: ${t.message}")
        }
    }

    private fun createHeaderButton(iconText: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            val btnSize = dpToPx(34)
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                marginStart = dpToPx(6)
            }
            text = iconText
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#334155"))
            }
            setOnClickListener { onClick() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleTouchListener() {
        val headView = bubbleHeadView ?: return

        headView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = bubbleParams.x
                        initialY = bubbleParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (hypot(dx.toDouble(), dy.toDouble()) > DRAG_THRESHOLD_PX) {
                            isDragging = true
                        }
                        if (isDragging) {
                            bubbleParams.x = initialX + dx
                            bubbleParams.y = initialY + dy
                            if (isBubbleAttached && bubbleHeadView != null) {
                                try {
                                    windowManager.updateViewLayout(bubbleHeadView, bubbleParams)
                                } catch (_: Throwable) {}
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            expandToPanel()
                        } else {
                            snapToNearestEdge()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHeaderDragListener(headerLayout: View) {
        headerLayout.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = panelParams.x
                        startY = panelParams.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()
                        panelParams.x = startX + dx
                        panelParams.y = startY + dy
                        if (isPanelAttached && bubblePanelView != null) {
                            try {
                                windowManager.updateViewLayout(bubblePanelView, panelParams)
                            } catch (_: Throwable) {}
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun snapToNearestEdge() {
        val headView = bubbleHeadView ?: return
        val screenWidth = resources.displayMetrics.widthPixels
        val bubbleSize = dpToPx(62)
        val margin = dpToPx(12)

        val bubbleCenterX = bubbleParams.x + (bubbleSize / 2)
        bubbleParams.x = if (bubbleCenterX < screenWidth / 2) {
            margin
        } else {
            screenWidth - bubbleSize - margin
        }

        val screenHeight = resources.displayMetrics.heightPixels
        val minY = dpToPx(40)
        val maxY = screenHeight - bubbleSize - dpToPx(50)
        if (bubbleParams.y < minY) bubbleParams.y = minY
        if (bubbleParams.y > maxY) bubbleParams.y = maxY

        if (isBubbleAttached) {
            try {
                windowManager.updateViewLayout(headView, bubbleParams)
            } catch (_: Throwable) {}
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun ensureWebViewInitialized() {
        if (webView != null) return
        val container = webContainer ?: return

        try {
            val wv: WebView = try {
                val themed = ContextThemeWrapper(
                    applicationContext,
                    android.R.style.Theme_DeviceDefault_NoActionBar
                )
                WebView(themed)
            } catch (t: Throwable) {
                android.util.Log.w("CalidadBubble", "Fallback directo a WebView(applicationContext)", t)
                WebView(applicationContext)
            }

            wv.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                allowFileAccess = true
                allowContentAccess = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = false
                displayZoomControls = false
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
            }

            // Exponer puente nativo enriquecido hacia Javascript
            wv.addJavascriptInterface(AndroidBridge(this@BubbleService), "AndroidBridge")

            // WebChromeClient con redirección de consola, soporte de FilePicker nativo y barra de progreso
            wv.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress < 100) {
                        progressBar?.visibility = View.VISIBLE
                        progressBar?.progress = newProgress
                    } else {
                        progressBar?.visibility = View.GONE
                    }
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    if (consoleMessage != null) {
                        val level = consoleMessage.messageLevel()
                        val tag = when (level) {
                            ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                            ConsoleMessage.MessageLevel.WARNING -> "WARN"
                            else -> "WEB"
                        }
                        AppLogManager.log(tag, consoleMessage.message())
                    }
                    return super.onConsoleMessage(consoleMessage)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    try {
                        AppLogManager.log("PICKER", "Solicitud de archivo iniciada")
                        if (fileUploadCallback != null) {
                            AppLogManager.log("PICKER", "Cancelando callback pendiente previo")
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = null
                        }
                        fileUploadCallback = filePathCallback
                        val intent = Intent(this@BubbleService, FilePickerActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            val types = fileChooserParams?.acceptTypes
                            if (!types.isNullOrEmpty() && types[0].isNotBlank()) {
                                putExtra(FilePickerActivity.EXTRA_MIME_TYPE, types[0])
                            }
                        }
                        startActivity(intent)
                        return true
                    } catch (e: Throwable) {
                        AppLogManager.log("ERROR", "Error al lanzar FilePickerActivity: ${e.message}")
                        fileUploadCallback?.onReceiveValue(null)
                        fileUploadCallback = null
                        return false
                    }
                }
            }

            wv.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        view?.loadUrl("file:///android_asset/index.html")
                    }
                }
            }

            val targetUrl = if (isNetworkAvailable()) {
                "https://minusheng.github.io/calidad-app/"
            } else {
                "file:///android_asset/index.html"
            }
            wv.loadUrl(targetUrl)

            webView = wv
            container.removeAllViews()
            container.addView(
                wv,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error al crear WebView diferido", t)
            showToast("Error al inicializar WebView: ${t.message}")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNet = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNet) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Throwable) {
            false
        }
    }

    private fun showBubbleHead() {
        try {
            if (!canDrawOverlaysSafe()) {
                showToast("⚠️ Calidad Bubble: Se requiere permiso de superposición")
                stopSelf()
                return
            }
            if (!isBubbleAttached && bubbleHeadView != null) {
                windowManager.addView(bubbleHeadView, bubbleParams)
                isBubbleAttached = true
                AppLogManager.log("SERVICE", "Burbuja flotante visible")
            }
            if (isPanelAttached && bubblePanelView != null) {
                windowManager.removeView(bubblePanelView)
                isPanelAttached = false
            }
            isExpanded = false
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en showBubbleHead", t)
            showToast("Error al mostrar burbuja: ${t.message}")
        }
    }

    private fun expandToPanel() {
        try {
            if (!canDrawOverlaysSafe()) {
                showToast("⚠️ Permiso de superposición no disponible")
                stopSelf()
                return
            }
            ensureWebViewInitialized()

            if (isBubbleAttached && bubbleHeadView != null) {
                windowManager.removeView(bubbleHeadView)
                isBubbleAttached = false
            }
            if (!isPanelAttached && bubblePanelView != null) {
                windowManager.addView(bubblePanelView, panelParams)
                isPanelAttached = true
                AppLogManager.log("SERVICE", "Panel expandido visible")
            }
            isExpanded = true
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en expandToPanel", t)
            showToast("Error al abrir panel: ${t.message}")
        }
    }

    private fun collapseToBubble() {
        try {
            if (isPanelAttached && bubblePanelView != null) {
                windowManager.removeView(bubblePanelView)
                isPanelAttached = false
            }
            if (!isBubbleAttached && bubbleHeadView != null) {
                windowManager.addView(bubbleHeadView, bubbleParams)
                isBubbleAttached = true
                AppLogManager.log("SERVICE", "Burbuja flotante visible")
            }
            isExpanded = false
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en collapseToBubble", t)
            showToast("Error al minimizar: ${t.message}")
        }
    }

    // Monitor periódico de Production Pack: revisa si el reloj marca hora en punto (:00)
    private fun checkHourlyProductionPack() {
        try {
            val calendar = Calendar.getInstance()
            val mm = calendar.get(Calendar.MINUTE)
            val hh = calendar.get(Calendar.HOUR_OF_DAY)
            val horaStr = String.format(Locale.US, "%02d:00", hh)

            if (mm == 0) {
                // Excluir horas oficiales de inicio de turno
                if (horaStr == "07:00" || horaStr == "15:00" || horaStr == "23:00") {
                    return
                }
                if (lastVibratedHour == horaStr) {
                    return
                }
                if (confirmedPackHours.contains(horaStr)) {
                    return
                }
                // Emitir vibración nativa de alerta horaria [0, 500, 250, 500] ms
                ejecutarVibracionPackNativa()
                lastVibratedHour = horaStr
                AppLogManager.log("PACK", "Alerta nativa de falta de Production Pack emitida ($horaStr)")
            } else {
                if (lastVibratedHour != null && mm >= 2) {
                    lastVibratedHour = null
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en checkHourlyProductionPack", t)
        }
    }

    fun ejecutarVibracionPackNativa() {
        try {
            val pattern = longArrayOf(0, 500, 250, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(pattern, -1)
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error al ejecutar alerta de vibración", t)
        }
    }

    // Guardado nativo a MediaStore.Downloads con fallback a Environment
    fun guardarArchivoNativo(base64Data: String, nombreArchivo: String, mimeType: String) {
        try {
            val cleanBase64 = if (base64Data.contains(",")) {
                base64Data.substringAfter(",")
            } else {
                base64Data
            }
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = contentResolver
                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val itemUri = resolver.insert(collection, contentValues)
                if (itemUri != null) {
                    resolver.openOutputStream(itemUri)?.use { os ->
                        os.write(bytes)
                        os.flush()
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)
                }
                itemUri
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, nombreArchivo)
                FileOutputStream(file).use { fos ->
                    fos.write(bytes)
                    fos.flush()
                }
                try {
                    FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
                } catch (_: Throwable) {
                    Uri.fromFile(file)
                }
            }

            AppLogManager.log("DOWNLOAD", "Archivo guardado en Descargas: $nombreArchivo (${bytes.size} bytes)")
            showToast("📥 Guardado en Descargas: $nombreArchivo")

            if (uri != null) {
                dispararNotificacionDescarga(uri, nombreArchivo, mimeType)
            }
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error al guardar archivo en Descargas", t)
            AppLogManager.log("ERROR", "Error guardarArchivo: ${t.message}")
            showToast("Error al descargar archivo: ${t.message}")
        }
    }

    private fun dispararNotificacionDescarga(uri: Uri, nombreArchivo: String, mimeType: String) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = Intent.createChooser(viewIntent, "Abrir $nombreArchivo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                (System.currentTimeMillis() % 100000).toInt(),
                chooserIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notification = NotificationCompat.Builder(this, DOWNLOADS_CHANNEL_ID)
                .setContentTitle("Archivo descargado: $nombreArchivo")
                .setContentText("Toca para abrir o compartir el archivo")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notifId = (System.currentTimeMillis() % 100000).toInt() + 2000
            notificationManager?.notify(notifId, notification)
        } catch (t: Throwable) {
            AppLogManager.log("ERROR", "Error al disparar notificación de descarga: ${t.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            packMonitorHandler.removeCallbacks(packMonitorRunnable)
            if (isBubbleAttached && bubbleHeadView != null) {
                windowManager.removeView(bubbleHeadView)
                isBubbleAttached = false
            }
            if (isPanelAttached && bubblePanelView != null) {
                windowManager.removeView(bubblePanelView)
                isPanelAttached = false
            }
            webView?.destroy()
            webView = null
        } catch (_: Throwable) {}
    }

    // Puente de Comunicación Nativa hacia JavaScript
    class AndroidBridge(private val service: BubbleService) {
        @JavascriptInterface
        fun vibrateAlert() {
            service.ejecutarVibracionPackNativa()
        }

        @JavascriptInterface
        fun confirmarPackRegistrado(hora: String) {
            try {
                val h = hora.trim()
                service.confirmedPackHours.add(h)
                AppLogManager.log("PACK", "Pack registrado confirmado: $h")
            } catch (t: Throwable) {
                AppLogManager.log("ERROR", "Error confirmarPackRegistrado: ${t.message}")
            }
        }

        @JavascriptInterface
        fun guardarArchivo(base64Data: String, nombreArchivo: String, mimeType: String) {
            try {
                service.guardarArchivoNativo(base64Data, nombreArchivo, mimeType)
            } catch (t: Throwable) {
                AppLogManager.log("ERROR", "Error guardarArchivo: ${t.message}")
            }
        }
    }
}
