package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinoraFormSheet(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))

            content()

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("انصراف")
                }

                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(confirmLabel, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddTransactionBottomSheet(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Long,
        type: TransactionType,
        category: TransactionCategory,
        paymentMethod: PaymentMethod,
        accountName: String,
        description: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.BANK_CARD) }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "حساب اصلی") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    FinoraFormSheet(
        title = "ثبت تراکنش جدید",
        onDismiss = onDismiss,
        confirmLabel = "ثبت تراکنش",
        onConfirm = {
            val amount = amountText.toLongOrNull()
            when {
                title.isBlank() -> errorMessage = "لطفاً عنوان را وارد کنید"
                amount == null || amount <= 0 -> errorMessage = "لطفاً مبلغ معتبر وارد کنید"
                else -> {
                    onSave(title, amount, selectedType, selectedCategory, selectedPayment, selectedAccount, description.ifBlank { null })
                    onDismiss()
                }
            }
        }
    ) {
        // Type Selector Chips (Income / Expense / Transfer)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionType.entries.forEach { type ->
                val isSelected = selectedType == type
                val activeBg = when (type) {
                    TransactionType.INCOME -> EmeraldGreen
                    TransactionType.EXPENSE -> RoseRed
                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.secondary
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    label = { Text(type.titleFa, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeBg,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان تراکنش") },
            placeholder = { Text("مثلاً خرید هایپرمارکت") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { char -> char.isDigit() } },
            label = { Text("مبلغ (به تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("دسته‌بندی:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TransactionCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.titleFa, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("روش پرداخت:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PaymentMethod.entries) { pm ->
                FilterChip(
                    selected = selectedPayment == pm,
                    onClick = { selectedPayment = pm },
                    label = { Text(pm.titleFa, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("بانک / حساب:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        val defaultBanks = listOf("بلوبانک", "بانک سامان", "بانک ملی", "بانک ملت", "بانک پاسارگاد", "بانک تجارت", "کارت به کارت")
        val availableAccounts = (accounts.map { it.name } + defaultBanks).distinct()

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(availableAccounts) { accName ->
                FilterChip(
                    selected = selectedAccount == accName,
                    onClick = { selectedAccount = accName },
                    leadingIcon = {
                        BankLogoBadge(bankName = accName, size = 18.dp, showBorder = false)
                    },
                    label = { Text(accName, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("توضیحات (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        val error = errorMessage
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = RoseRed, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (TransactionCategory, Long) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var limitText by remember { mutableStateOf("") }

    FinoraFormSheet(
        title = "تعریف بودجه جدید",
        onDismiss = onDismiss,
        confirmLabel = "ثبت بودجه",
        confirmEnabled = (limitText.toLongOrNull() ?: 0L) > 0,
        onConfirm = {
            val limit = limitText.toLongOrNull()
            if (limit != null && limit > 0) {
                onSave(selectedCategory, limit)
                onDismiss()
            }
        }
    ) {
        Text("انتخاب دسته‌بندی:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TransactionCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.titleFa, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = limitText,
            onValueChange = { limitText = it.filter { c -> c.isDigit() } },
            label = { Text("سقف بودجه ماهانه (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    FinoraFormSheet(
        title = "ایجاد هدف پس‌انداز جدید",
        onDismiss = onDismiss,
        confirmLabel = "ایجاد هدف",
        confirmEnabled = title.isNotBlank() && (targetText.toLongOrNull() ?: 0L) > 0,
        onConfirm = {
            val target = targetText.toLongOrNull()
            if (title.isNotBlank() && target != null && target > 0) {
                onSave(title, target, "#10B981")
                onDismiss()
            }
        }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان هدف (مثلاً خرید لپ‌تاپ)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = targetText,
            onValueChange = { targetText = it.filter { c -> c.isDigit() } },
            label = { Text("مبلغ هدف (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun DepositGoalDialog(
    goal: SavingGoalEntity,
    onDismiss: () -> Unit,
    onConfirmDeposit: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    FinoraFormSheet(
        title = "واریز به «${goal.title}»",
        onDismiss = onDismiss,
        confirmLabel = "واریز",
        confirmEnabled = (amountText.toLongOrNull() ?: 0L) > 0,
        onConfirm = {
            val deposit = amountText.toLongOrNull()
            if (deposit != null && deposit > 0) {
                onConfirmDeposit(deposit)
                onDismiss()
            }
        }
    ) {
        Text("مبلغ واریز به صندوق پس‌انداز را وارد کنید:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() } },
            label = { Text("مبلغ واریزی (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (String, AccountType, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var balanceText by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    FinoraFormSheet(
        title = "افزودن حساب / کارت جدید",
        onDismiss = onDismiss,
        confirmLabel = "افزودن حساب",
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            if (name.isNotBlank()) {
                val balance = balanceText.toLongOrNull() ?: 0L
                onSave(name, selectedType, balance, accountNumber.ifBlank { null })
                onDismiss()
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("نام حساب یا بانک (مثلاً بلوبانک، سامان)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("نوع حساب:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AccountType.entries) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.titleFa, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = balanceText,
            onValueChange = { balanceText = it.filter { c -> c.isDigit() } },
            label = { Text("موجودی اولیه (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("شماره کارت یا حساب (اختیاری)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
