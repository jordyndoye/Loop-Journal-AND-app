package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Diagnostics and Crashlytics manager to monitor, log, and report
 * app events, stability telemetry, and non-fatal exceptions.
 */
object CrashReporter {

  private const val TAG = "CrashReporter"
  private val customKeys = mutableMapOf<String, Any>()
  private var isInitialized = false

  fun init(context: Context) {
    try {
      val crashlytics = FirebaseCrashlytics.getInstance()
      setCustomKey("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
      setCustomKey("os_version", Build.VERSION.RELEASE ?: "unknown")
      setCustomKey("sdk_int", Build.VERSION.SDK_INT)
      setCustomKey("today_date", DateTimeUtils.todayIso())

      setupUncaughtExceptionHandler()
      isInitialized = true
      log("CrashReporter initialized successfully on ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})")
    } catch (e: Throwable) {
      Log.w(TAG, "CrashReporter could not be initialized with Crashlytics", e)
    }
  }

  fun log(message: String) {
    Log.d(TAG, message)
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().log(message)
      }
    } catch (_: Throwable) {}
  }

  fun recordException(throwable: Throwable) {
    Log.e(TAG, "Recording non-fatal exception: ${throwable.message}", throwable)
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
      }
    } catch (_: Throwable) {}
  }

  fun setCustomKey(key: String, value: String) {
    customKeys[key] = value
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  fun setCustomKey(key: String, value: Int) {
    customKeys[key] = value
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  fun setCustomKey(key: String, value: Boolean) {
    customKeys[key] = value
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  private fun setupUncaughtExceptionHandler() {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      try {
        setCustomKey("crashing_thread", thread.name)
        log("Fatal crash intercepted on thread: ${thread.name} - ${throwable.message}")
        recordException(throwable)
      } catch (_: Throwable) {}

      // Delegate to default Android crash handler
      previousHandler?.uncaughtException(thread, throwable)
    }
  }
}
