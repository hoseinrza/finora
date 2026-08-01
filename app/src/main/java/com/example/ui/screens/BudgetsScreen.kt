package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.data.BudgetEntity
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.FinoraViewModel
import com.example.ui.components.FinoraGlassCard
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed

@Composable
fun BudgetsScreen(
    viewModel: FinoraViewModel,
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    onOpenAddBudget: () -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    val spentByCategory = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "بودجه و تحلیل",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Summary Card
        item {
            val totalLimit = budgets.sumOf { it.amountLimit }
            val totalSpent = budgets.sumOf { spentByCategory[it.category] ?: 0L }
            val overallRatio = if (totalLimit > 0) (totalSpent.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f) else 0f

            FinoraGlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مدیریت بودجه‌های ماهانه",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = onOpenAddBudget,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "افزودن بودجه", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بودجه جدید", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }

                    if (budgets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = overallRatio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (overallRatio > 0.9f) RoseRed else EmeraldGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "جمع مصرف: ${viewModel.formatAmountFa(totalSpent)} تومان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "جمع سقف: ${viewModel.formatAmountFa(totalLimit)} تومان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تعریف سقف مصرف برای دسته‌بندی‌های مختلف جهت جلوگیری از هزینه‌های اضافی و افزایش پس‌انداز.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Budget Items List
        if (budgets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هنوز بودجه‌ای تعریف نکرده‌اید.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(budgets, key = { it.id }) { budget ->
                val spent = spentByCategory[budget.category] ?: 0L
                val ratio = if (budget.amountLimit > 0) (spent.toFloat() / budget.amountLimit.toFloat()).coerceIn(0f, 1f) else 0f
                val isOverLimit = spent > budget.amountLimit
                val isNearLimit = ratio > 0.75f

                SwipeToDeleteContainer(onDelete = { onDeleteBudget(budget) }) {
                    FinoraGlassCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isOverLimit) RoseRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isOverLimit) Icons.Default.Warning else Icons.Default.PieChart,
                                            contentDescription = budget.category.titleFa,
                                            tint = if (isOverLimit) RoseRed else EmeraldGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = budget.category.titleFa,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isOverLimit) "تخطی از سقف بودجه!" else if (isNearLimit) "نزدیک به سقف بودجه" else "در محدوده ایمن",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOverLimit) RoseRed else if (isNearLimit) AmberAccent else EmeraldGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = ratio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isOverLimit) RoseRed else if (isNearLimit) AmberAccent else EmeraldGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "مصرف: ${viewModel.formatAmountFa(spent)} تومان",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "سقف: ${viewModel.formatAmountFa(budget.amountLimit)} تومان",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
