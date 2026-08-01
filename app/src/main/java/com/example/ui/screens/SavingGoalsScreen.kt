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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.AccountEntity
import com.example.data.SavingGoalEntity
import com.example.ui.FinoraViewModel
import com.example.ui.components.BankLogoBadge
import com.example.ui.components.FinoraGlassCard
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyPrimaryLight

private enum class GoalsAccountsTab(val titleFa: String) {
    ACCOUNTS("حساب‌ها"),
    GOALS("اهداف پس‌انداز")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingGoalsScreen(
    viewModel: FinoraViewModel,
    goals: List<SavingGoalEntity>,
    accounts: List<AccountEntity>,
    onOpenAddGoal: () -> Unit,
    onOpenDepositGoal: (SavingGoalEntity) -> Unit,
    onOpenAddAccount: () -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    onDeleteGoal: (SavingGoalEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(GoalsAccountsTab.ACCOUNTS) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "اهداف و حساب‌ها",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            GoalsAccountsTab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalsAccountsTab.entries.size),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = EmeraldGreen, activeContentColor = Color.White)
                ) {
                    Text(tab.titleFa, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            GoalsAccountsTab.ACCOUNTS -> AccountsTabContent(
                viewModel = viewModel,
                accounts = accounts,
                onOpenAddAccount = onOpenAddAccount,
                onDeleteAccount = onDeleteAccount
            )
            GoalsAccountsTab.GOALS -> GoalsTabContent(
                viewModel = viewModel,
                goals = goals,
                onOpenAddGoal = onOpenAddGoal,
                onOpenDepositGoal = onOpenDepositGoal,
                onDeleteGoal = onDeleteGoal
            )
        }
    }
}

@Composable
private fun AccountsTabContent(
    viewModel: FinoraViewModel,
    accounts: List<AccountEntity>,
    onOpenAddAccount: () -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${accounts.size} حساب فعال",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onOpenAddAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن حساب", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حساب جدید", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }

        if (accounts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "هنوز حسابی اضافه نکرده‌اید. برای شروع یک حساب یا کیف پول اضافه کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(accounts, key = { it.id }) { acc ->
                SwipeToDeleteContainer(onDelete = { onDeleteAccount(acc) }) {
                    AccountCard(viewModel = viewModel, account = acc)
                }
            }
        }
    }
}

@Composable
private fun AccountCard(viewModel: FinoraViewModel, account: AccountEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyPrimary, NavyPrimaryLight)))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BankLogoBadge(bankName = account.name, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(account.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                        Text(account.type.titleFa, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                    }
                }
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = account.name, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("موجودی", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            Text(
                "${viewModel.formatAmountFa(account.balance)} ${account.currency}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            if (account.accountNumber != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(account.accountNumber, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun GoalsTabContent(
    viewModel: FinoraViewModel,
    goals: List<SavingGoalEntity>,
    onOpenAddGoal: () -> Unit,
    onOpenDepositGoal: (SavingGoalEntity) -> Unit,
    onDeleteGoal: (SavingGoalEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goals.size} هدف پس‌انداز",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onOpenAddGoal,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "هدف جدید", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("هدف جدید", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هدف پس‌اندازی ثبت نشده است.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val ratio = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
                val percent = (ratio * 100).toInt()

                SwipeToDeleteContainer(onDelete = { onDeleteGoal(goal) }) {
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Savings, contentDescription = goal.title, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = goal.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$percent٪ محقق شده",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldGreen
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onOpenDepositGoal(goal) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "واریز", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("واریز", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = ratio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = EmeraldGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "پس‌انداز: ${viewModel.formatAmountFa(goal.currentAmount)} تومان",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "هدف: ${viewModel.formatAmountFa(goal.targetAmount)} تومان",
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
