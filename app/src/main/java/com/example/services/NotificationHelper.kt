package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.logging.CrashReporter

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "finora_bank_channel"
    private const val CHANNEL_NAME = "اعلان‌های بانکی و تراکنش‌ها"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های هوشمند تراکنش‌های بانکی و تحلیل مالی"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, id: Int = System.currentTimeMillis().toInt()) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // Without POST_NOTIFICATIONS, manager.notify() below throws SecurityException on API 33+.
            // The transaction itself is still saved to Room regardless of this notification;
            // this only skips the status-bar banner.
            CrashReporter.logWarning(TAG, "Skipping notification '$title': POST_NOTIFICATIONS not granted")
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_expense)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.notify(id, builder.build())
        } catch (e: Exception) {
            CrashReporter.logError(TAG, "Failed to post notification '$title'", e)
        }
    }
}
