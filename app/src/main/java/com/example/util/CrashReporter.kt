package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Robust Crashlytics and telemetry manager to monitor, log, and report
 * unexpected app terminations and non-fatal errors across user devices.
 */
object CrashReporter {

  private const val TAG = "CrashReporter"
  private var isInitialized = false

  fun init(context: Context) {
    try {
      val crashlytics = FirebaseCrashlytics.getInstance()
      crashlytics.setCrashlyticsCollectionEnabled(true)

      // Set standard diagnostics keys
      crashlytics.setCustomKey("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
      crashlytics.setCustomKey("os_version", Build.VERSION.RELEASE ?: "unknown")
      crashlytics.setCustomKey("sdk_int", Build.VERSION.SDK_INT)
      crashlytics.setCustomKey("today_date", DateTimeUtils.todayIso())

      setupUncaughtExceptionHandler(crashlytics)
      isInitialized = true
      log("CrashReporter initialized successfully on ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})")
    } catch (e: Throwable) {
      Log.w(TAG, "Crashlytics could not be initialized or is running in non-Firebase environment", e)
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
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  fun setCustomKey(key: String, value: Int) {
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  fun setCustomKey(key: String, value: Boolean) {
    try {
      if (isInitialized) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
      }
    } catch (_: Throwable) {}
  }

  private fun setupUncaughtExceptionHandler(crashlytics: FirebaseCrashlytics) {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      try {
        crashlytics.setCustomKey("crashing_thread", thread.name)
        crashlytics.log("Fatal crash intercepted on thread: ${thread.name} - ${throwable.message}")
        crashlytics.recordException(throwable)
      } catch (_: Throwable) {}

      // Delegate to default Android crash handler
      previousHandler?.uncaughtException(thread, throwable)
    }
  }
}
