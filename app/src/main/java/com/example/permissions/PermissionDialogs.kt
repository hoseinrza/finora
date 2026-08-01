package com.example.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Explains why a permission is needed, shown right before (re)requesting it - only reachable when
 * `shouldShowRequestPermissionRationale()` is true (the user denied it once but hasn't blocked it
 * permanently).
 */
@Composable
fun PermissionRationaleDialog(
    explanation: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسترسی مورد نیاز") },
        text = { Text(explanation) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("ادامه") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("فعلاً نه") }
        }
    )
}

/**
 * Shown when a permission has been permanently denied ("don't ask again") - the system will no
 * longer surface its own request dialog, so the only path forward is the app's own Settings page.
 */
@Composable
fun PermissionSettingsRedirectDialog(
    explanation: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("دسترسی رد شده است") },
        text = { Text("$explanation\n\nاین دسترسی به‌صورت دائمی رد شده. برای فعال‌سازی، باید آن را از تنظیمات برنامه فعال کنید.") },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("باز کردن تنظیمات") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}
