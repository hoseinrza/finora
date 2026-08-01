package com.example.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Single entry point for structured error reporting, replacing scattered `printStackTrace()`
 * calls. Every error is logged locally via [Log] (visible in Logcat / adb bug reports) AND
 * recorded to Firebase Crashlytics as a non-fatal so it shows up in production crash reporting
 * without crashing the app.
 *
 * If no `google-services.json` was supplied at build time (see app/build.gradle.kts), the
 * Crashlytics call below is a safe no-op - [logError] never throws because of a missing/invalid
 * Firebase config.
 */
object CrashReporter {

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }

        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("[$tag] $message")
            if (throwable != null) {
                crashlytics.recordException(throwable)
            }
        }
    }

    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        runCatching { FirebaseCrashlytics.getInstance().log("[$tag] WARN: $message") }
    }
}
