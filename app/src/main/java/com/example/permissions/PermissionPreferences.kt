package com.example.permissions

import android.content.Context

/**
 * Tracks which runtime permissions the app has already asked the user for at least once.
 *
 * This is what lets [MainActivity] tell the difference between "never asked yet" (just show the
 * system dialog) and "asked before and now denied with `shouldShowRequestPermissionRationale()`
 * returning false" (permanently denied / "don't ask again") - Android's permission APIs don't
 * expose that distinction directly, since `shouldShowRequestPermissionRationale()` is also false
 * before the very first request.
 */
object PermissionPreferences {

    private const val PREFS_NAME = "finora_permission_prefs"
    private const val KEY_PREFIX_REQUESTED = "requested_"

    fun markRequested(context: Context, permission: String) {
        prefs(context).edit().putBoolean(KEY_PREFIX_REQUESTED + permission, true).apply()
    }

    fun hasRequestedBefore(context: Context, permission: String): Boolean {
        return prefs(context).getBoolean(KEY_PREFIX_REQUESTED + permission, false)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
