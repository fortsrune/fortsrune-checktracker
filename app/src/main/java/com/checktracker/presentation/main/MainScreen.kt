package com.checktracker.presentation.main

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checktracker.core.formatMoney
import com.checktracker.domain.model.Category
import com.checktracker.domain.model.CurrencyOption
import com.checktracker.domain.model.Expense
import com.checktracker.presentation.theme.HeaderGradient
import com.checktracker.presentation.theme.categoryColor
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Эмодзи на выбор при создании своей категории
private val EMOJI_PRESETS = listOf(
    "🛒", "☕", "🚌", "🏠", "💊", "🎬", "👕", "🎁",
    "🐶", "✈️", "📚", "🎮", "💡", "⛽", "🛠️", "💳",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAccountsDialog by rememberSaveable { mutableStateOf(false) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("ЧекТрекер", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAccountsDialog = true }) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Счета",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Default.Savings, contentDescription = "Бюджет",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showCurrencyDialog = true }) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = "Валюта",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.exportCsv().onSuccess { csv -> shareCsv(context, csv) }
                                .onFailure { snackbarHostState.showSnackbar("Не удалось сформировать экспорт") }
                        }
                    }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Экспорт CSV",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить чек")
            }
        },
    ) { padding ->
        // Скроллируемый контент между шапкой и фиксированной нижней навигацией
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                HeaderCard(state = state, onBudgetClick = { showBudgetDialog = true })
            }
            item {
                FiltersBlock(
                    state = state,
                    onSearch = viewModel::setSearchQuery,
                    onSelectAccount = viewModel::selectAccount,
                    onSelectCategory = viewModel::selectCategory,
                )
            }
            if (state.loading) {
                item {
                    Text(
                        "Загрузка…",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (state.expenses.isEmpty()) {
                item { EmptyState() }
            } else {
                items(state.expenses, key = { it.id }) { expense ->
                    SwipeToDeleteCard(
                        expense = expense,
                        state = state,
                        onDelete = viewModel::deleteExpense,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            state = state,
            onDismiss = { showAddDialog = false },
            onConfirm = { merchant, amount, categoryId, accountId, currency ->
                viewModel.addExpense(merchant, amount, categoryId, accountId, currency)
                showAddDialog = false
            },
            onAddCategory = viewModel::addCategory,
        )
    }

    if (showAccountsDialog) {
        AccountsDialog(
            state = state,
            onDismiss = { showAccountsDialog = false },
            onAddAccount = viewModel::addAccount,
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudgetMinor = state.budgetMinor,
            currency = state.displayCurrency,
            onDismiss = { showBudgetDialog = false },
            onSave = {
                viewModel.setBudget(it)
                showBudgetDialog = false
            },
        )
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            current = state.displayCurrency,
            onSelect = viewModel::setDisplayCurrency,
            onDismiss = { showCurrencyDialog = false },
        )
    }
}

// Градиентная шапка с итогами месяца и прогрессом бюджета
@Composable
private fun HeaderCard(state: MainUiState, onBudgetClick: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(HeaderGradient))
                .padding(24.dp),
        ) {
            Column {
                Text(
                    "Расходы за месяц",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatMoney(state.monthTotalMinor, state.displayCurrency.code),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallStat("Сегодня", formatMoney(state.todayTotalMinor, state.displayCurrency.code))
                    SmallStat("Неделя", formatMoney(state.weekTotalMinor, state.displayCurrency.code))
                }
            }
        }

        // Прогресс бюджета: зелёный → жёлтый → красный
        AnimatedVisibility(visible = state.budgetMinor > 0) {
            if (state.budgetMinor > 0) {
                BudgetCard(
                    spentMinor = state.monthTotalMinor,
                    budgetMinor = state.budgetMinor,
                    currencyCode = state.displayCurrency.code,
                    onClick = onBudgetClick,
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(spentMinor: Long, budgetMinor: Long, currencyCode: String, onClick: () -> Unit) {
    val ratio = if (budgetMinor > 0) (spentMinor.toFloat() / budgetMinor).coerceIn(0f, 1f) else 0f
    val barColor = when {
        ratio < 0.7f -> Color(0xFF43A047)
        ratio < 1f -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 600),
        label = "budget",
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Месячный лимит",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${formatMoney(spentMinor, currencyCode)} из ${formatMoney(budgetMinor, currencyCode)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRatio)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(barColor),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (spentMinor <= budgetMinor) {
                    "Осталось: ${formatMoney(budgetMinor - spentMinor, currencyCode)}"
                } else {
                    "Превышение на ${formatMoney(spentMinor - budgetMinor, currencyCode)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = barColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SmallStat(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

// Поиск и фильтры по счетам и категориям
@Composable
private fun FiltersBlock(
    state: MainUiState,
    onSearch: (String) -> Unit,
    onSelectAccount: (Long?) -> Unit,
    onSelectCategory: (Long?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск по магазину…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(Modifier.height(8.dp))
        // Фильтр по счетам
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = state.selectedAccountId == null,
                onClick = { onSelectAccount(null) },
                label = { Text("Все счета") },
            )
            state.accounts.forEach { account ->
                FilterChip(
                    selected = state.selectedAccountId == account.id,
                    onClick = {
                        onSelectAccount(if (state.selectedAccountId == account.id) null else account.id)
                    },
                    label = { Text(account.name) },
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // Фильтр по категориям
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = state.selectedCategoryId == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Все категории") },
            )
            state.categories.forEach { category ->
                FilterChip(
                    selected = state.selectedCategoryId == category.id,
                    onClick = {
                        onSelectCategory(if (state.selectedCategoryId == category.id) null else category.id)
                    },
                    label = { Text("${category.emoji} ${category.name}") },
                )
            }
        }
    }
}

// Свайп влево для удаления чека
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    expense: Expense,
    state: MainUiState,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(expense)
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        },
    ) {
        ExpenseCard(expense = expense, state = state)
    }
}

// Карточка одного расхода с цветным аватаром категории
@Composable
private fun ExpenseCard(expense: Expense, state: MainUiState) {
    val category = state.categories.firstOrNull { it.id == expense.categoryId }
    val account = state.accounts.firstOrNull { it.id == expense.accountId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(categoryColor(category).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(category?.emoji ?: "📦", fontSize = 19.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.merchant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    buildString {
                        append(category?.name ?: "Другое")
                        account?.let { append(" · ")
                            append(it.name) }
                        append(" · ${formatDate(expense.timestamp)}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                formatMoney(expense.amountMinor, expense.currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Пустое состояние с плавным появлением
@Composable
private fun EmptyState() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "emptyAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🧾", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Чеков пока нет",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Нажмите «+», чтобы добавить первый расход",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Диалог добавления чека: сетка категорий 3 в ряд, выбор счёта и валюты
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    state: MainUiState,
    onDismiss: () -> Unit,
    onConfirm: (merchant: String, amount: String, categoryId: Long, accountId: Long, currency: CurrencyOption) -> Unit,
    onAddCategory: (name: String, emoji: String) -> Unit,
) {
    var merchant by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf(state.categories.firstOrNull()?.id ?: 0L) }
    var selectedAccountId by rememberSaveable { mutableStateOf(state.accounts.firstOrNull()?.id ?: 0L) }
    var currency by rememberSaveable { mutableStateOf(CurrencyOption.BYN) }
    var showNewCategoryDialog by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый чек", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Магазин") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CurrencyOption.entries.forEach { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text("${c.symbol} ${c.code}") },
                        )
                    }
                }
                Text("Категория", style = MaterialTheme.typography.labelLarge)
                // Сетка категорий: по 3 плитки в ряд, с ровными отступами
                state.categories.chunked(3).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowCategories.forEach { category ->
                            CategoryTile(
                                category = category,
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                TextButton(onClick = { showNewCategoryDialog = true }) {
                    Text("+ Своя категория")
                }
                Text("Счёт", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.accounts.forEach { account ->
                        FilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = { Text(account.name) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(merchant, amount, selectedCategoryId, selectedAccountId, currency) },
                enabled = merchant.isNotBlank() && amount.isNotBlank(),
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )

    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, emoji ->
                onAddCategory(name, emoji)
                showNewCategoryDialog = false
            },
        )
    }
}

// Плитка категории для сетки
@Composable
private fun CategoryTile(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(category.emoji, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// Диалог создания своей категории: название + выбор эмодзи
@Composable
private fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf(EMOJI_PRESETS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая категория", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Text("Иконка", style = MaterialTheme.typography.labelLarge)
                EMOJI_PRESETS.chunked(4).forEach { rowEmojis ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowEmojis.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (emoji == preset) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        },
                                    )
                                    .clickable(onClick = { emoji = preset }),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(preset, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, emoji) },
                enabled = name.isNotBlank(),
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

// Диалог управления счетами: список с балансами и форма добавления
@Composable
private fun AccountsDialog(
    state: MainUiState,
    onDismiss: () -> Unit,
    onAddAccount: (name: String, initialBalance: String, currency: CurrencyOption) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(CurrencyOption.BYN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Счета", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.accounts.forEach { account ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(account.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatMoney(
                                state.accountBalances[account.id] ?: 0L,
                                state.displayCurrency.code,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (state.accounts.isEmpty()) {
                    Text(
                        "Счетов пока нет. Добавьте первый ниже.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например, «Карта Беларусбанка»)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Начальный баланс") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CurrencyOption.entries.forEach { c ->
                        FilterChip(
                            selected = currency == c,
                            onClick = { currency = c },
                            label = { Text("${c.symbol} ${c.code}") },
                        )
                    }
                }
                TextButton(
                    onClick = {
                        onAddAccount(name, balance, currency)
                        name = ""
                        balance = ""
                    },
                    enabled = name.isNotBlank(),
                ) { Text("+ Добавить счёт") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Готово") }
        },
    )
}

// Диалог месячного лимита расходов
@Composable
private fun BudgetDialog(
    currentBudgetMinor: Long,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by rememberSaveable {
        mutableStateOf(
            if (currentBudgetMinor > 0) String.format(Locale.getDefault(), "%.2f", currentBudgetMinor / 100.0) else "",
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Месячный лимит", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Укажите лимит расходов на месяц в ${currency.label.lowercase()} — прогресс будет виден на главном экране.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Лимит, ${currency.code}") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                TextButton(onClick = { onSave("0") }) {
                    Text("Убрать лимит")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text) },
                enabled = text.isNotBlank(),
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDialog(
    current: CurrencyOption,
    onSelect: (CurrencyOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Валюта отображения", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyOption.entries.forEach { currency ->
                    Card(
                        onClick = {
                            onSelect(currency)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currency == current) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                currency.symbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    currency.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    currency.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
    )
}

// Экспорт: сохраняем CSV в папку приложения и открываем меню «Поделиться»
private fun shareCsv(context: Context, csv: String) {
    runCatching {
        val dir = context.getExternalFilesDir(null)
        val file = File(dir, "checktracker_export.csv")
        file.writeText(csv)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ЧекТрекер — экспорт расходов")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "Экспорт расходов"))
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMMM", Locale("ru")).format(Date(timestamp))
