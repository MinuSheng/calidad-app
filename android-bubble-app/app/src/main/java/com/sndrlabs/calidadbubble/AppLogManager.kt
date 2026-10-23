package com.sndrlabs.calidadbubble

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogManager {
    data class LogEntry(
        val timestamp: String,
        val tag: String,
        val message: String
    ) {
        fun format(): String = "$timestamp $tag $message"
    }

    private const val MAX_LOGS = 150
    private val logs = mutableListOf<LogEntry>()
    private val listeners = mutableListOf<() -> Unit>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val mainHandler = Handler(Looper.getMainLooper())

    @Synchronized
    fun log(tag: String, message: String) {
        val normalizedTag = if (tag.startsWith("[") && tag.endsWith("]")) tag else "[$tag]"
        val timestamp = timeFormat.format(Date())
        val entry = LogEntry(timestamp, normalizedTag, message)

        android.util.Log.i("CalidadBubble", "$timestamp $normalizedTag $message")

        synchronized(logs) {
            if (logs.size >= MAX_LOGS) {
                logs.removeAt(0)
            }
            logs.add(entry)
        }

        notifyListeners()
    }

    @Synchronized
    fun getLogs(): List<LogEntry> {
        synchronized(logs) {
            return ArrayList(logs)
        }
    }

    @Synchronized
    fun exportLogs(): String {
        synchronized(logs) {
            return logs.joinToString("\n") { it.format() }
        }
    }

    @Synchronized
    fun clearLogs() {
        synchronized(logs) {
            logs.clear()
        }
        notifyListeners()
    }

    fun addListener(listener: () -> Unit) {
        synchronized(listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    fun removeListener(listener: () -> Unit) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    private fun notifyListeners() {
        mainHandler.post {
            val callbacks = synchronized(listeners) { ArrayList(listeners) }
            for (listener in callbacks) {
                try {
                    listener.invoke()
                } catch (_: Throwable) {}
            }
        }
    }
}
