package com.example.data

import kotlinx.coroutines.flow.Flow

class FinoraRepository(private val db: FinoraDatabase) {

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    fun getTransactionsByType(type: TransactionType) = db.transactionDao().getTransactionsByType(type)
    suspend fun insertTransaction(transaction: TransactionEntity) = db.transactionDao().insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = db.transactionDao().deleteTransaction(transaction)

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = db.budgetDao().getAllBudgets()
    suspend fun insertBudget(budget: BudgetEntity) = db.budgetDao().insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = db.budgetDao().updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = db.budgetDao().deleteBudget(budget)

    // Saving Goals
    val allSavingGoals: Flow<List<SavingGoalEntity>> = db.savingGoalDao().getAllSavingGoals()
    suspend fun insertGoal(goal: SavingGoalEntity) = db.savingGoalDao().insertGoal(goal)
    suspend fun updateGoal(goal: SavingGoalEntity) = db.savingGoalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: SavingGoalEntity) = db.savingGoalDao().deleteGoal(goal)

    // Accounts
    val allAccounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()
    suspend fun insertAccount(account: AccountEntity) = db.accountDao().insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = db.accountDao().updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = db.accountDao().deleteAccount(account)

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    suspend fun insertNotification(notification: NotificationEntity) = db.notificationDao().insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = db.notificationDao().markAsRead(id)
    suspend fun deleteNotification(notification: NotificationEntity) = db.notificationDao().deleteNotification(notification)
    suspend fun clearAllNotifications() = db.notificationDao().deleteAllNotifications()
}
