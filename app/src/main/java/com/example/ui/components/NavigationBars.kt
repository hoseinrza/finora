package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinoraTopBar(
    currentTab: AppNavTab,
    unreadNotificationCount: Int = 0,
    isDarkTheme: Boolean = false,
    isSyncingSms: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSyncSmsClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Finora",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FINORA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTab.titleFa,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Sync Recent SMS Button
            IconButton(onClick = onSyncSmsClick, enabled = !isSyncingSms) {
                if (isSyncingSms) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = EmeraldGreen
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "همگام‌سازی از پیامک‌ها",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Theme Toggle Button
            IconButton(onClick = onToggleDarkTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Theme Toggle",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Notification Bell with Badge
            Box(
                modifier = Modifier.clickable { onNotificationClick() },
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$unreadNotificationCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    )
}

private val bottomBarTabs = listOf(
    AppNavTab.DASHBOARD,
    AppNavTab.TRANSACTIONS,
    AppNavTab.BUDGETS_ANALYTICS,
    AppNavTab.GOALS_ACCOUNTS,
    AppNavTab.PROFILE
)

private fun iconFor(tab: AppNavTab): ImageVector = when (tab) {
    AppNavTab.DASHBOARD -> Icons.Default.Dashboard
    AppNavTab.TRANSACTIONS -> Icons.Default.ReceiptLong
    AppNavTab.BUDGETS_ANALYTICS -> Icons.Default.BarChart
    AppNavTab.GOALS_ACCOUNTS -> Icons.Default.Savings
    AppNavTab.PROFILE -> Icons.Default.Person
    else -> Icons.Default.Circle
}

/**
 * Floating pill-shaped bottom navigation. Icon-only - no text labels under any tab; the
 * selected tab is distinguished by a soft rounded highlight and tint color alone.
 */
@Composable
fun FinoraBottomBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomBarTabs.forEach { tab ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    FinoraBottomBarItem(
                        tab = tab,
                        isSelected = currentTab == tab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinoraBottomBarItem(
    tab: AppNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (isSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) EmeraldGreen.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconFor(tab),
            contentDescription = tab.titleFa,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
