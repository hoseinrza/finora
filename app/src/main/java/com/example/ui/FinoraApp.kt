package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.example.data.SavingGoalEntity
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.FinoraTheme

@Composable
fun FinoraApp(viewModel: FinoraViewModel) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isSyncingSms by viewModel.isSyncingSms.collectAsState()

    val readSmsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) viewModel.syncSmsTransactions() }

    val onSyncSmsClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.syncSmsTransactions()
        } else {
            readSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    val groupedFilteredTransactions by viewModel.groupedFilteredTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allBudgets by viewModel.allBudgets.collectAsState()
    val allSavingGoals by viewModel.allSavingGoals.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()

    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val weeklyCashFlow by viewModel.weeklyCashFlow.collectAsState()
    val topSpendingCategories by viewModel.topSpendingCategories.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    // Dialog & BottomSheet States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingGoalEntity?>(null) }

    FinoraTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    FinoraTopBar(
                        currentTab = currentTab,
                        unreadNotificationCount = allNotifications.count { !it.isRead },
                        isDarkTheme = isDarkTheme,
                        isSyncingSms = isSyncingSms,
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onNotificationClick = { viewModel.setTab(AppNavTab.NOTIFICATIONS) },
                        onSyncSmsClick = onSyncSmsClick
                    )
                },
                bottomBar = {
                    FinoraBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        AppNavTab.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                totalBalance = totalBalance,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                healthScore = healthScore,
                                weeklyCashFlow = weeklyCashFlow,
                                topSpendingCategories = topSpendingCategories,
                                recentTransactions = allTransactions,
                                onOpenAddTransaction = { showAddTransactionSheet = true },
                                onNavigateTab = { viewModel.setTab(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) }
                            )
                        }

                        AppNavTab.TRANSACTIONS -> {
                            TransactionsScreen(
                                viewModel = viewModel,
                                groupedTransactions = groupedFilteredTransactions,
                                resultCount = filteredTransactions.size,
                                searchQuery = searchQuery,
                                selectedType = selectedTypeFilter,
                                selectedCategory = selectedCategoryFilter,
                                onOpenAddTransaction = { showAddTransactionSheet = true },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) }
                            )
                        }

                        AppNavTab.BUDGETS_ANALYTICS -> {
                            BudgetsScreen(
                                viewModel = viewModel,
                                budgets = allBudgets,
                                transactions = allTransactions,
                                onOpenAddBudget = { showAddBudgetDialog = true },
                                onDeleteBudget = { viewModel.deleteBudget(it) }
                            )
                        }

                        AppNavTab.GOALS_ACCOUNTS -> {
                            SavingGoalsScreen(
                                viewModel = viewModel,
                                goals = allSavingGoals,
                                accounts = allAccounts,
                                onOpenAddGoal = { showAddGoalDialog = true },
                                onOpenDepositGoal = { goal -> selectedGoalForDeposit = goal },
                                onOpenAddAccount = { showAddAccountDialog = true },
                                onDeleteAccount = { viewModel.deleteAccount(it) },
                                onDeleteGoal = { viewModel.deleteGoal(it) }
                            )
                        }

                        AppNavTab.NOTIFICATIONS -> {
                            NotificationsScreen(
                                viewModel = viewModel,
                                notifications = allNotifications,
                                onDeleteNotification = { notif -> viewModel.deleteNotification(notif) },
                                onClearAll = { viewModel.clearAllNotifications() }
                            )
                        }

                        AppNavTab.PROFILE -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { viewModel.toggleDarkTheme() }
                            )
                        }
                    }
                }

                // Modals
                if (showAddTransactionSheet) {
                    AddTransactionBottomSheet(
                        accounts = allAccounts,
                        onDismiss = { showAddTransactionSheet = false },
                        onSave = { title, amount, type, category, pm, acc, desc ->
                            viewModel.addTransaction(title, amount, type, category, pm, acc, desc)
                        }
                    )
                }

                if (showAddBudgetDialog) {
                    AddBudgetDialog(
                        onDismiss = { showAddBudgetDialog = false },
                        onSave = { cat, limit ->
                            viewModel.addBudget(cat, limit)
                        }
                    )
                }

                if (showAddGoalDialog) {
                    AddGoalDialog(
                        onDismiss = { showAddGoalDialog = false },
                        onSave = { title, target, color ->
                            viewModel.addGoal(title, target, color)
                        }
                    )
                }

                if (showAddAccountDialog) {
                    AddAccountDialog(
                        onDismiss = { showAddAccountDialog = false },
                        onSave = { name, type, balance, accNum ->
                            viewModel.addAccount(name, type, balance, accNum)
                        }
                    )
                }

                if (selectedGoalForDeposit != null) {
                    DepositGoalDialog(
                        goal = selectedGoalForDeposit!!,
                        onDismiss = { selectedGoalForDeposit = null },
                        onConfirmDeposit = { amount ->
                            viewModel.addDepositToGoal(selectedGoalForDeposit!!, amount)
                        }
                    )
                }
            }
        }
    }
}
