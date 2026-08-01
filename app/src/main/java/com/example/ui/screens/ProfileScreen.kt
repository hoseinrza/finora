package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.services.BankParserUtils
import com.example.ui.FinoraViewModel
import com.example.ui.components.FinoraGlassCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary

@Composable
fun ProfileScreen(
    viewModel: FinoraViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var testSmsText by remember { mutableStateOf("بلو بانک: واریز 1,200,000 تومان به حساب") }
    var testResultText by remember { mutableStateOf("") }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        testResultText = if (isGranted) "دسترسی پیامک تایید شد. شنود پیامک‌های بانکی فعال است." else "دسترسی پیامک داده نشد."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "پروفایل",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar Card
        FinoraGlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "پروفایل کاربری",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("کاربر Finora", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("تمام داده‌ها فقط روی همین دستگاه ذخیره می‌شوند", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bank Auto-Sync Controls (SMS & Notification Listener)
        FinoraGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("همگام‌سازی خودکار بانکی (۱۰۰٪ آفلاین)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                // SMS Interceptor Permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, contentDescription = "پیامک بانکی", tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("شنود و استخراج پیامک‌های بانکی", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("ثبت خودکار تراکنش با دریافت SMS واریز/برداشت", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { smsPermissionLauncher.launch(android.Manifest.permission.RECEIVE_SMS) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("فعال‌سازی", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // NotificationListenerService Launcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "نوتیفیکیشن بانکی", tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("خوانش نوتیفیکیشن اپ‌های بانکی", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("استخراج تراکنش از بلوبانک، همراه بانک‌ها و...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("تنظیمات دسترسی", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SMS / Notification Parser Tester
        FinoraGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("شبیه‌ساز و تست موتور خوانش متن", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                OutlinedTextField(
                    value = testSmsText,
                    onValueChange = { testSmsText = it },
                    label = { Text("متن پیامک یا نوتیفیکیشن بانکی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Button(
                    onClick = {
                        val parsed = BankParserUtils.parseText(testSmsText)
                        if (parsed != null) {
                            BankParserUtils.processAndSave(context, testSmsText, "تست دستی")
                            testResultText = "✅ تراکنش با موفقیت ثبت شد!\nمبلغ: ${viewModel.formatAmountFa(parsed.amount)} تومان\nنوع: ${parsed.type.titleFa}\nبانک: ${parsed.bankName}\nدسته: ${parsed.category.titleFa}"
                        } else {
                            testResultText = "❌ متنی شناسایی نشد یا الگوی واریز/برداشت در متن یافت نشد."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("تست و ثبت در پایگاه داده", color = Color.White)
                }

                if (testResultText.isNotEmpty()) {
                    Text(
                        text = testResultText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (testResultText.startsWith("✅")) EmeraldGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // General Settings Group
        FinoraGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("تنظیمات برنامه", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                // Dark Mode Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "تم تاریک",
                            tint = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("تم تاریک (Dark Mode)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                // Offline Room DB Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = "ذخیره‌سازی local", tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("ذخیره‌سازی ۱۰۰٪ آفلاین Room DB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Text("فعال", style = MaterialTheme.typography.labelLarge, color = EmeraldGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Info & Version Card
        FinoraGlassCard {
            Column {
                Text("درباره Finora", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("نسخه: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ویژگی‌ها: NotificationListenerService + Bank SMS Parser + Room DB", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
