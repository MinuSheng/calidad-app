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
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlin.math.hypot

class BubbleService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "calidad_bubble_channel"
        private const val NOTIFICATION_ID = 2026
        private const val DRAG_THRESHOLD_PX = 10
    }

    private lateinit var windowManager: WindowManager
    private var bubbleHeadView: View? = null
    private var bubblePanelView: View? = null
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
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForegroundServiceWithNotification()
        setupViews()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
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
            .setContentText("Toca para abrir la aplicación de calidad")
            .setSmallIcon(R.drawable.ic_bubble_bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun setupViews() {
        val inflater = LayoutInflater.from(this)

        // 1. Burbuja Flotante Colapsada (62dp)
        bubbleHeadView = inflater.inflate(R.layout.layout_bubble_head, null)
        val bubbleSize = dpToPx(62)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

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

        setupBubbleTouchListener()

        // 2. Panel Messenger Expandido (92% ancho, 82% alto)
        bubblePanelView = inflater.inflate(R.layout.layout_bubble_panel, null)
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

        setupPanelViews()
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
                                windowManager.updateViewLayout(bubbleHeadView, bubbleParams)
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
            windowManager.updateViewLayout(headView, bubbleParams)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupPanelViews() {
        val panel = bubblePanelView ?: return

        val btnReload: ImageButton = panel.findViewById(R.id.btnReload)
        val btnMinimize: ImageButton = panel.findViewById(R.id.btnMinimize)
        val btnClose: ImageButton = panel.findViewById(R.id.btnClose)
        val tvHeaderTitle: TextView = panel.findViewById(R.id.tvHeaderTitle)
        val headerLayout: View = panel.findViewById(R.id.panelHeader)
        progressBar = panel.findViewById(R.id.webProgressBar)
        webView = panel.findViewById(R.id.panelWebView)

        tvHeaderTitle.text = "⚡ Calidad App (SNDR LABS)"

        btnReload.setOnClickListener {
            webView?.reload()
        }

        btnMinimize.setOnClickListener {
            collapseToBubble()
        }

        btnClose.setOnClickListener {
            collapseToBubble()
        }

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
                            windowManager.updateViewLayout(bubblePanelView, panelParams)
                        }
                        return true
                    }
                }
                return false
            }
        })

        configureWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        val wv = webView ?: return

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
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showBubbleHead() {
        if (!isBubbleAttached && bubbleHeadView != null) {
            windowManager.addView(bubbleHeadView, bubbleParams)
            isBubbleAttached = true
        }
        if (isPanelAttached && bubblePanelView != null) {
            windowManager.removeView(bubblePanelView)
            isPanelAttached = false
        }
        isExpanded = false
    }

    private fun expandToPanel() {
        if (isBubbleAttached && bubbleHeadView != null) {
            windowManager.removeView(bubbleHeadView)
            isBubbleAttached = false
        }
        if (!isPanelAttached && bubblePanelView != null) {
            windowManager.addView(bubblePanelView, panelParams)
            isPanelAttached = true
        }
        isExpanded = true
    }

    private fun collapseToBubble() {
        if (isPanelAttached && bubblePanelView != null) {
            windowManager.removeView(bubblePanelView)
            isPanelAttached = false
        }
        if (!isBubbleAttached && bubbleHeadView != null) {
            windowManager.addView(bubbleHeadView, bubbleParams)
            isBubbleAttached = true
        }
        isExpanded = false
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (isBubbleAttached && bubbleHeadView != null) {
                windowManager.removeView(bubbleHeadView)
            }
            if (isPanelAttached && bubblePanelView != null) {
                windowManager.removeView(bubblePanelView)
            }
            webView?.destroy()
        } catch (_: Exception) {}
    }
}
