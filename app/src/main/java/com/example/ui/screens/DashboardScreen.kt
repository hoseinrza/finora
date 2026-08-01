package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.AppNavTab
import com.example.ui.CategorySpend
import com.example.ui.FinoraViewModel
import com.example.ui.components.*
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FinoraViewModel,
    totalBalance: Long,
    totalIncome: Long,
    totalExpense: Long,
    healthScore: Int,
    weeklyCashFlow: com.example.ui.WeeklyCashFlow,
    topSpendingCategories: List<CategorySpend>,
    recentTransactions: List<TransactionEntity>,
    onOpenAddTransaction: () -> Unit,
    onNavigateTab: (AppNavTab) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Hero Balance Card
        item {
            FinoraGradientHeroCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "موجودی کل حساب‌ها",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${viewModel.formatAmountFa(totalBalance)} تومان",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onOpenAddTransaction,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "افزودن تراکنش",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HeroStat(
                            icon = Icons.Default.ArrowUpward,
                            iconTint = EmeraldGreen,
                            iconBg = EmeraldGreen.copy(alpha = 0.18f),
                            label = "درآمد این هفته",
                            value = "+${viewModel.formatAmountFa(totalIncome)} تومان",
                            valueColor = EmeraldGreen
                        )
                        HeroStat(
                            icon = Icons.Default.ArrowDownward,
                            iconTint = RoseRed,
                            iconBg = RoseRed.copy(alpha = 0.18f),
                            label = "هزینه‌های این هفته",
                            value = "-${viewModel.formatAmountFa(totalExpense)} تومان",
                            valueColor = RoseRed
                        )
                    }
                }
            }
        }

        // 2. Quick Action Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    title = "ثبت تراکنش",
                    icon = Icons.Default.AddCircleOutline,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddTransaction
                )
                QuickActionChip(
                    title = "بودجه‌بندی",
                    icon = Icons.Default.PieChart,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppNavTab.BUDGETS_ANALYTICS) }
                )
                QuickActionChip(
                    title = "پس‌انداز",
                    icon = Icons.Default.Savings,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppNavTab.GOALS_ACCOUNTS) }
                )
                QuickActionChip(
                    title = "اعلان‌ها",
                    icon = Icons.Default.NotificationsNone,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(AppNavTab.NOTIFICATIONS) }
                )
            }
        }

        // 3. Financial Health Score
        item {
            FinoraGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "سلامت مالی",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "شاخص سلامت مالی",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = healthScoreMessage(healthScore),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    HealthScoreRing(score = healthScore)
                }
            }
        }

        // 4. Weekly Cash Flow Chart
        item {
            FinoraGlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "روند جریان نقدینگی هفته",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                            Text(" درآمد", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoseRed))
                            Text(" هزینه", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (weeklyCashFlow.income.any { it > 0 } || weeklyCashFlow.expense.any { it > 0 }) {
                        CashFlowBarChart(
                            incomeList = weeklyCashFlow.income,
                            expenseList = weeklyCashFlow.expense,
                            daysFa = weeklyCashFlow.labels
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "این هفته هنوز تراکنشی ثبت نشده است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Top Spending Categories
        if (topSpendingCategories.isNotEmpty()) {
            item {
                FinoraGlassCard {
                    Column {
                        Text(
                            text = "بیشترین هزینه‌ها بر اساس دسته‌بندی",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topSpendingCategories.forEach { spend ->
                                CategorySpendRow(spend = spend, formatAmount = viewModel::formatAmountFa)
                            }
                        }
                    }
                }
            }
        }

        // 6. Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تراکنش‌های اخیر",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = { onNavigateTab(AppNavTab.TRANSACTIONS) }) {
                    Text("مشاهده همه", style = MaterialTheme.typography.labelLarge, color = EmeraldGreen)
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هنوز تراکنشی ثبت نشده است.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(recentTransactions.take(5), key = { it.id }) { tx ->
                SwipeToDeleteContainer(onDelete = { onDeleteTransaction(tx) }) {
                    TransactionItemCard(transaction = tx, viewModel = viewModel)
                }
            }
        }
    }
}

private fun healthScoreMessage(score: Int): String = when {
    score >= 80 -> "وضعیت مدیریت منابع و پس‌انداز شما «بسیار مطلوب» است."
    score >= 60 -> "وضعیت مالی شما در محدوده «مطلوب» قرار دارد."
    score >= 40 -> "وضعیت مالی شما «متوسط» است، مراقب هزینه‌ها باشید."
    else -> "هزینه‌های شما بالاست، بازبینی بودجه توصیه می‌شود."
}

@Composable
private fun RowScope.HeroStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
            Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor)
        }
    }
}

@Composable
private fun CategorySpendRow(spend: CategorySpend, formatAmount: (Long) -> String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = spend.category.titleFa,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatAmount(spend.amount)} تومان",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = spend.ratio.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    viewModel: FinoraViewModel
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val detectedBank = BankRegistry.findBank(transaction.accountName, transaction.title, transaction.description)

    FinoraGlassCard(
        modifier = Modifier.padding(vertical = 2.dp),
        cornerRadius = 14.dp,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isIncome) EmeraldGreen.copy(alpha = 0.15f) else RoseRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (detectedBank != null) {
                        BankLogoBadge(bankName = detectedBank.name, size = 36.dp, showBorder = false)
                    } else {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = transaction.category.titleFa,
                            tint = if (isIncome) EmeraldGreen else RoseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (detectedBank != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            BankChip(bankName = detectedBank.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category.titleFa,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(" • ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = timeFormat.format(Date(transaction.date)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${viewModel.formatAmountFa(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isIncome) EmeraldGreen else RoseRed,
                    maxLines = 1
                )
                Text(
                    text = " ت",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
