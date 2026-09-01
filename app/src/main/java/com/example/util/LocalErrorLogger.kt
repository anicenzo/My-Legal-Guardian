package com.example.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 100% Offline Structured Error Logger.
 * Strictly guarantees ZERO network transmission.
 * Records diagnostic events to local internal cache only.
 */
object LocalErrorLogger {

    private const val TAG = "LocalErrorLogger"
    private const val MAX_LOG_ENTRIES = 50
    private const val LOG_FILE_NAME = "local_audit_errors.log"

    data class ErrorEntry(
        val timestamp: String,
        val component: String,
        val message: String,
        val stackTrace: String?
    )

    private val inMemoryLog = mutableListOf<ErrorEntry>()

    fun record(context: Context, component: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val stack = throwable?.stackTraceToString()?.take(500)
        val entry = ErrorEntry(timestamp, component, message, stack)

        synchronized(inMemoryLog) {
            if (inMemoryLog.size >= MAX_LOG_ENTRIES) {
                inMemoryLog.removeAt(0)
            }
            inMemoryLog.add(entry)
        }

        Log.e(TAG, "[$component] $message", throwable)

        try {
            val file = File(context.cacheDir, LOG_FILE_NAME)
            file.appendText("[$timestamp] [$component] $message\n${stack ?: ""}\n---\n")
        } catch (e: Exception) {
            // Local file write failed; in-memory log remains available
        }
    }

    fun getRecentErrors(): List<ErrorEntry> = synchronized(inMemoryLog) { inMemoryLog.toList() }

    fun clear(context: Context) {
        synchronized(inMemoryLog) { inMemoryLog.clear() }
        try {
            File(context.cacheDir, LOG_FILE_NAME).delete()
        } catch (_: Exception) {}
    }
}
