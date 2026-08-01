package com.example.permissions

/**
 * What, if anything, the UI should show the user about a pending runtime permission.
 *
 * At most one of these is shown at a time, keyed by the permission it concerns:
 * - [Rationale]: we're about to (re)request [permission] and should first explain why, because
 *   the system has indicated the user is receptive to an explanation
 *   (`shouldShowRequestPermissionRationale() == true`, i.e. they denied it before but haven't
 *   permanently blocked it).
 * - [PermanentlyDenied]: [permission] was denied and the system will no longer show its own
 *   request dialog for it - the only way forward is the app's Settings page.
 */
sealed interface PermissionUiState {
    data object None : PermissionUiState
    data class Rationale(val permission: String, val explanation: String) : PermissionUiState
    data class PermanentlyDenied(val permission: String, val explanation: String) : PermissionUiState
}

fun explanationFor(permission: String): String = when (permission) {
    android.Manifest.permission.RECEIVE_SMS ->
        "Finora از پیامک‌های بانکی برای ثبت خودکار تراکنش‌ها استفاده می‌کند. بدون این دسترسی باید تراکنش‌ها را دستی وارد کنید."
    android.Manifest.permission.READ_SMS ->
        "Finora برای همگام‌سازی تراکنش‌ها از پیامک‌های بانکی اخیر شما به این دسترسی نیاز دارد."
    android.Manifest.permission.POST_NOTIFICATIONS ->
        "Finora با این دسترسی می‌تواند هنگام ثبت خودکار یک تراکنش به شما اطلاع دهد."
    else -> "این دسترسی برای عملکرد صحیح برنامه لازم است."
}
