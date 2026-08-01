package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategorySpend(val category: TransactionCategory, val amount: Long, val ratio: Float)

data class WeeklyCashFlow(
    val labels: List<String>,
    val income: List<Float>,
    val expense: List<Float>
)

data class TransactionDayGroup(
    val label: String,
    val transactions: List<TransactionEntity>
)

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

enum class AppNavTab(val titleFa: String, val iconName: String) {
    DASHBOARD("داشبورد", "dashboard"),
    TRANSACTIONS("تراکنش‌ها", "receipt"),
    BUDGETS_ANALYTICS("بودجه و تحلیل", "analytics"),
    GOALS_ACCOUNTS("اهداف و حساب‌ها", "savings"),
    NOTIFICATIONS("اعلان‌ها", "notifications"),
    PROFILE("پروفایل", "person")
}

class FinoraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinoraRepository = FinoraRepository(FinoraDatabase.getDatabase(application))

    // Active Tab
    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Dark Theme Toggle State
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Search and Category Filters for Transactions
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<TransactionCategory?>(null)
    val selectedCategoryFilter: StateFlow<TransactionCategory?> = _selectedCategoryFilter.asStateFlow()

    // Transactions Flow
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter
    ) { list, query, type, category ->
        list.filter { tx ->
            val matchesQuery = query.isBlank() || tx.title.contains(query, ignoreCase = true) || (tx.description?.contains(query, ignoreCase = true) == true)
            val matchesType = type == null || tx.type == type
            val matchesCategory = category == null || tx.category == category
            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budgets Flow
    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saving Goals Flow
    val allSavingGoals: StateFlow<List<SavingGoalEntity>> = repository.allSavingGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Accounts Flow
    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications Flow
    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Metrics
    val totalIncome: StateFlow<Long> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalExpense: StateFlow<Long> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBalance: StateFlow<Long> = combine(allAccounts, totalIncome, totalExpense) { accounts, inc, exp ->
        if (accounts.isNotEmpty()) {
            accounts.sumOf { it.balance }
        } else {
            inc - exp
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Financial Health Score (0 - 100)
    val healthScore: StateFlow<Int> = combine(totalIncome, totalExpense, allSavingGoals) { inc, exp, goals ->
        if (inc == 0L) 50 else {
            val savingsRatio = ((inc - exp).toDouble() / inc.toDouble()).coerceIn(0.0, 1.0)
            val baseScore = (savingsRatio * 70).toInt()
            val goalProgressBonus = if (goals.isNotEmpty()) (goals.count { it.isCompleted } * 10) else 10
            (baseScore + goalProgressBonus + 20).coerceIn(10, 98)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85)

    // Top spending categories (for dashboard breakdown)
    val topSpendingCategories: StateFlow<List<CategorySpend>> = allTransactions.map { list ->
        val byCategory = list
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
        val total = byCategory.values.sum()
        byCategory.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { (category, amount) ->
                CategorySpend(
                    category = category,
                    amount = amount,
                    ratio = if (total > 0) (amount.toFloat() / total.toFloat()) else 0f
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Last 7 days income/expense, for the dashboard cash-flow chart
    val weeklyCashFlow: StateFlow<WeeklyCashFlow> = allTransactions.map { list ->
        val dayLabelFormat = SimpleDateFormat("EEEE", Locale("fa"))
        val calendar = Calendar.getInstance()

        val buckets = (6 downTo 0).map { offset ->
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 24 * 3600 * 1000L
            Triple(dayStart, dayEnd, dayLabelFormat.format(Date(dayStart)))
        }

        val income = buckets.map { (start, end, _) ->
            list.filter { it.type == TransactionType.INCOME && it.date in start until end }
                .sumOf { it.amount }.toFloat()
        }
        val expense = buckets.map { (start, end, _) ->
            list.filter { it.type == TransactionType.EXPENSE && it.date in start until end }
                .sumOf { it.amount }.toFloat()
        }
        WeeklyCashFlow(labels = buckets.map { it.third }, income = income, expense = expense)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyCashFlow(emptyList(), emptyList(), emptyList())
    )

    // Filtered transactions grouped by day, for the Transactions screen
    val groupedFilteredTransactions: StateFlow<List<TransactionDayGroup>> = filteredTransactions.map { list ->
        val today = Calendar.getInstance()
        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dayFormat = SimpleDateFormat("d MMMM", Locale("fa"))

        fun labelFor(date: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            return when {
                cal.isSameDay(today) -> "امروز"
                cal.isSameDay(yesterday) -> "دیروز"
                else -> dayFormat.format(Date(date))
            }
        }

        list.groupBy { labelFor(it.date) }
            .map { (label, txs) -> TransactionDayGroup(label, txs) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun addTransaction(
        title: String,
        amount: Long,
        type: TransactionType,
        category: TransactionCategory,
        paymentMethod: PaymentMethod,
        accountName: String,
        description: String?
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                paymentMethod = paymentMethod,
                accountName = accountName,
                description = description
            )
            repository.insertTransaction(entity)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBudget(category: TransactionCategory, limitAmount: Long) {
        viewModelScope.launch {
            repository.insertBudget(BudgetEntity(category = category, amountLimit = limitAmount))
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addGoal(title: String, targetAmount: Long, colorHex: String) {
        viewModelScope.launch {
            repository.insertGoal(SavingGoalEntity(title = title, targetAmount = targetAmount, colorHex = colorHex))
        }
    }

    fun addDepositToGoal(goal: SavingGoalEntity, depositAmount: Long) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + depositAmount
            val updated = goal.copy(
                currentAmount = newAmount,
                isCompleted = newAmount >= goal.targetAmount
            )
            repository.updateGoal(updated)
        }
    }

    fun addAccount(name: String, type: AccountType, initialBalance: Long, accountNumber: String? = null) {
        viewModelScope.launch {
            repository.insertAccount(AccountEntity(name = name, type = type, balance = initialBalance, accountNumber = accountNumber))
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun deleteGoal(goal: SavingGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun simulateLiveBankMessage(rawText: String) {
        viewModelScope.launch {
            com.example.services.BankParserUtils.processAndSave(
                getApplication(),
                rawText,
                "پیامک زنده (شبیه‌ساز)"
            )
        }
    }

    fun formatAmountFa(amount: Long): String {
        return java.text.DecimalFormat("#,###").format(amount)
    }
}
