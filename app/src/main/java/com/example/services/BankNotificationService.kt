package com.example.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class BankNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val combinedText = "$title $text $bigText".trim()

        if (combinedText.isNotEmpty()) {
            val appPackageName = sbn.packageName ?: "اپلیکیشن بانکی"
            BankParserUtils.processAndSave(applicationContext, combinedText, "نوتیفیکیشن ($appPackageName)")
        }
    }
}
