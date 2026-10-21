package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.ui.components.AppBottomNavigation
import com.moneycare.app.ui.components.BottomNavItem
import com.moneycare.app.ui.components.TransactionItem

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateStatistics: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    viewModel: PersonalTransactionViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadInitialData() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Giao dịch cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigation(
                selectedItem = BottomNavItem.TRANSACTIONS,
                onItemSelected = {
                    when (it) {
                        BottomNavItem.HOME -> onNavigateHome()
                        BottomNavItem.TRANSACTIONS -> Unit
                        BottomNavItem.STATISTICS -> onNavigateStatistics()
                        BottomNavItem.PROFILE -> onNavigateProfile()
                    }
                },
                onAddClick = onAddClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Tháng trước")
                }
                Text(monthLabel(state.month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.changeMonth(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Tháng sau")
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo tên giao dịch") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.refresh() })
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedType == null,
                        onClick = { viewModel.selectType(null) },
                        label = { Text("Tất cả") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.selectType(TransactionType.EXPENSE) },
                        label = { Text("Chi tiêu") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == TransactionType.INCOME,
                        onClick = { viewModel.selectType(TransactionType.INCOME) },
                        label = { Text("Thu nhập") }
                    )
                }
            }

            val visibleCategories = state.categories.filter {
                state.selectedType == null || it.type == state.selectedType
            }
            if (visibleCategories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Mọi danh mục") }
                        )
                    }
                    items(visibleCategories, key = { it.categoryId }) { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.categoryId,
                            onClick = { viewModel.selectCategory(category.categoryId) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.transactions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Text("Chưa có giao dịch trong tháng này", modifier = Modifier.padding(top = 12.dp))
                    }
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "${state.totalElements} giao dịch",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    itemsIndexed(state.transactions, key = { _, item -> item.id }) { index, transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                        if (index == state.transactions.lastIndex) {
                            LaunchedEffect(state.page, state.totalPages) { viewModel.loadMore() }
                        }
                    }
                    if (state.isLoadingMore) {
                        item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                    }
                }
            }
        }
    }
}

private fun monthLabel(month: String): String {
    val parts = month.split("-")
    return if (parts.size == 2) "Tháng ${parts[1].toIntOrNull() ?: parts[1]}/${parts[0]}" else month
}
