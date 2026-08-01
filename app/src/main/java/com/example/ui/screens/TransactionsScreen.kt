package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.TransactionCategory
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.FinoraViewModel
import com.example.ui.TransactionDayGroup
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinoraViewModel,
    groupedTransactions: List<TransactionDayGroup>,
    resultCount: Int,
    searchQuery: String,
    selectedType: TransactionType?,
    selectedCategory: TransactionCategory?,
    onOpenAddTransaction: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تراکنش‌ها",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledIconButton(
                onClick = onOpenAddTransaction,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تراکنش", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("جستجوی تراکنش‌ها...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Segmented Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { viewModel.setTypeFilter(null) },
                label = { Text("همه", style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedType == TransactionType.INCOME,
                onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                label = { Text("درآمد", style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                label = { Text("هزینه", style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoseRed,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("همه دسته‌ها", style = MaterialTheme.typography.labelMedium) }
                )
            }

            items(TransactionCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { viewModel.setCategoryFilter(if (selectedCategory == cat) null else cat) },
                    label = { Text(cat.titleFa, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$resultCount تراکنش یافت شد",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (groupedTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "بدون تراکنش",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تراکنشی با این مشخصات یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                groupedTransactions.forEach { group ->
                    item(key = "header_${group.label}") {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(group.transactions, key = { it.id }) { tx ->
                        SwipeToDeleteContainer(onDelete = { onDeleteTransaction(tx) }) {
                            TransactionItemCard(transaction = tx, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
