package com.checktracker.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checktracker.core.formatMoney
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ЧекТрекер") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить чек")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SummaryCard(state)
            if (state.loading) {
                Text(
                    "Загрузка…",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                ExpenseList(
                    expenses = state.expenses,
                    onDelete = viewModel::deleteExpense,
                )
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { merchant, amount, category ->
                viewModel.addExpense(merchant, amount, category)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun SummaryCard(state: MainUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Расходы за месяц", style = MaterialTheme.typography.labelMedium)
            Text(
                formatMoney(state.monthTotalMinor, "RUB"),
                style = MaterialTheme.typography.headlineMedium,
            )
            state.monthTotalUsd?.let { usd ->
                Text("≈ $usd USD", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "За неделю: ${formatMoney(state.weekTotalMinor, "RUB")}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ExpenseList(expenses: List<Expense>, onDelete: (Expense) -> Unit) {
    if (expenses.isEmpty()) {
        Text(
            "Пока нет чеков. Нажмите «+», чтобы добавить первый.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    LazyColumn {
        items(expenses, key = { it.id }) { expense ->
            ListItem(
                headlineContent = { Text(expense.merchant) },
                supportingContent = {
                    Text("${expense.category.emoji} ${expense.category.label} · ${formatDate(expense.timestamp)}")
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatMoney(expense.amountMinor, expense.currency))
                        IconButton(onClick = { onDelete(expense) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (merchant: String, amount: String, category: Category) -> Unit,
) {
    var merchant by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.GROCERIES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый чек") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Магазин") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма, ₽") },
                    singleLine = true,
                )
                Spacer(Modifier.height(4.dp))
                Category.entries.chunked(3).forEach { rowCategories ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowCategories.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text("${c.emoji} ${c.label}") },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(merchant, amount, category) },
                enabled = merchant.isNotBlank() && amount.isNotBlank(),
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMM", Locale("ru")).format(Date(timestamp))
