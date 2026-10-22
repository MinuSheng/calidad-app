package com.sndrlabs.calidadbubble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.hypot

class BubbleService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "calidad_bubble_channel"
        private const val NOTIFICATION_ID = 1001
        private const val DRAG_THRESHOLD_PX = 10
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupAntiCrashShield()

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            createNotificationChannel()
            startForegroundServiceWithNotification()
            setupProgrammaticViews()
            showBubbleHead()
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
            val channelName = try { getString(R.string.channel_name) } catch (_: Throwable) { "Burbuja Calidad" }
            val channelDesc = try { getString(R.string.channel_desc) } catch (_: Throwable) { "Notificación de servicio persistente" }
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = channelDesc
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
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

            // Icono del sistema 100% garantizado en Android 14/15 One UI
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Burbuja de Calidad Activa")
                .setContentText("Toca para abrir la aplicación de calidad")
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

            // 2. Construcción 100% programática de la Burbuja Flotante
            val bubbleFrame = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(bubbleSize, bubbleSize)
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.parseColor("#D946EF"), Color.parseColor("#6D28D9"))
                ).apply {
                    shape = GradientDrawable.OVAL
                    setStroke(dpToPx(2), Color.parseColor("#22D3EE"))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = dpToPx(6).toFloat()
                }
            }

            val bubbleText = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                text = "⚡"
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                setTextColor(Color.WHITE)
            }
            bubbleFrame.addView(bubbleText)
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

            // 4. Construcción 100% programática del Panel Expandido
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
                    setStroke(dpToPx(2), Color.parseColor("#7C3AED"))
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
                text = "⚡ Calidad App v4.0.25"
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

            // Contenedor dinámico del WebView (el WebView se crea de forma diferida en expandToPanel)
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
            }

            wv.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress < 100) {
                        progressBar?.visibility = View.VISIBLE
                        progressBar?.progress = newProgress
                    } else {
                        progressBar?.visibility = View.GONE
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
            // Inicialización diferida (lazy) del WebView al expandir por primera vez
            ensureWebViewInitialized()

            if (isBubbleAttached && bubbleHeadView != null) {
                windowManager.removeView(bubbleHeadView)
                isBubbleAttached = false
            }
            if (!isPanelAttached && bubblePanelView != null) {
                windowManager.addView(bubblePanelView, panelParams)
                isPanelAttached = true
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
            }
            isExpanded = false
        } catch (t: Throwable) {
            android.util.Log.e("CalidadBubble", "Error en collapseToBubble", t)
            showToast("Error al minimizar: ${t.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
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
}
