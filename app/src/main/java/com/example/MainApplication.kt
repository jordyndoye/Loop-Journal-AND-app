package com.example

import android.app.Application
import android.util.Log
import com.example.util.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      // Initialize Firebase Crashlytics to monitor stability
      val crashlytics = FirebaseCrashlytics.getInstance()
      crashlytics.setCrashlyticsCollectionEnabled(true)
      CrashReporter.init(this)
      Log.i("MainApplication", "Firebase Crashlytics initialized successfully")
    } catch (t: Throwable) {
      Log.w("MainApplication", "Crashlytics initialization skipped", t)
    }
  }
}
