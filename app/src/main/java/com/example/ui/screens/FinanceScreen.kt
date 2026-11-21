package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ImageStorageManager
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.FinancePeriod
import com.example.ui.FinanceTypeFilter
import com.example.ui.FinanceUiState
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.FullscreenImageDialog
import com.example.ui.utils.formatRupiah
import com.example.ui.utils.formatTransactionDate

@Composable
fun FinanceScreen(
    uiState: FinanceUiState,
    pendingBackupCount: Int,
    imageStorageManager: ImageStorageManager,
    snackbarHostState: SnackbarHostState,
    showTransactionSheet: Boolean,
    onOpenAddTransaction: () -> Unit,
    onDismissTransactionSheet: () -> Unit,
    onPeriodSelected: (FinancePeriod) -> Unit,
    onTypeFilterSelected: (FinanceTypeFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSaveReceiptFromUri: suspend (Uri) -> String?,
    onDeleteUnusedReceipt: (String) -> Unit,
    onSaveTransaction: (
        existingId: String?,
        type: TransactionType,
        amount: Long,
        title: String,
        category: String,
        note: String,
        receiptImageName: String?,
        transactionDate: Long
    ) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenBackupCenter: () -> Unit,
    onOpenThemeCustomizer: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var txPendingDeletion by remember { mutableStateOf<TransactionEntity?>(null) }
    var receiptToView by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onClearStatusMessage()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Keuangan",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Catat pemasukan & pengeluaran • ${uiState.period.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onOpenBackupCenter,
                    shape = CircleShape,
                    color = if (pendingBackupCount > 0) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.testTag("finance_open_backup_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Backup,
                            contentDescription = "Cadangan & Status Cloud",
                            tint = if (pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (pendingBackupCount > 0) "Cadangkan ($pendingBackupCount)" else "Cadangan",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                IconButton(
                    onClick = onOpenThemeCustomizer,
                    modifier = Modifier.testTag("finance_open_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Ubah tema dan latar belakang",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Balance Hero Card
            item {
                BalanceSummaryCard(
                    uiState = uiState,
                    modifier = Modifier.widthIn(max = 720.dp)
                )
            }

            // Period & Type Filters
            item {
                LazyRow(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FinancePeriod.entries) { period ->
                        FilterChip(
                            selected = uiState.period == period,
                            onClick = { onPeriodSelected(period) },
                            label = { Text(period.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("finance_period_${period.name.lowercase()}")
                        )
                    }

                    items(FinanceTypeFilter.entries) { filter ->
                        FilterChip(
                            selected = uiState.typeFilter == filter,
                            onClick = { onTypeFilterSelected(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("finance_type_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            // Expense Breakdown Card
            if (uiState.expenseBreakdown.isNotEmpty() && uiState.typeFilter != FinanceTypeFilter.INCOME) {
                item {
                    ExpenseBreakdownCard(
                        uiState = uiState,
                        modifier = Modifier.widthIn(max = 720.dp)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari transaksi atau kategori…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Hapus pencarian"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .testTag("finance_search_input")
                )
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    FinanceEmptyState(
                        onAddClick = onOpenAddTransaction,
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .padding(vertical = 28.dp)
                    )
                }
            } else {
                items(uiState.transactions, key = { it.id }) { tx ->
                    TransactionRowCard(
                        transaction = tx,
                        imageStorageManager = imageStorageManager,
                        onClick = { editingTransaction = tx },
                        onReceiptClick = { fileName -> receiptToView = fileName },
                        onDelete = { txPendingDeletion = tx },
                        modifier = Modifier.widthIn(max = 720.dp)
                    )
                }
            }
        }
    }

    if (showTransactionSheet || editingTransaction != null) {
        AddEditTransactionSheet(
            existingTransaction = editingTransaction,
            imageStorageManager = imageStorageManager,
            onSaveReceiptFromUri = onSaveReceiptFromUri,
            onDeleteUnusedReceipt = onDeleteUnusedReceipt,
            onSave = { id, type, amount, title, category, note, receiptName, date ->
                onSaveTransaction(id, type, amount, title, category, note, receiptName, date)
                editingTransaction = null
                onDismissTransactionSheet()
            },
            onDismiss = {
                editingTransaction = null
                onDismissTransactionSheet()
            }
        )
    }

    receiptToView?.let { fileName ->
        FullscreenImageDialog(
            imageNames = listOf(fileName),
            initialIndex = 0,
            imageStorageManager = imageStorageManager,
            onDeleteImage = { receiptToView = null },
            onDismiss = { receiptToView = null }
        )
    }

    txPendingDeletion?.let { target ->
        AlertDialog(
            onDismissRequest = { txPendingDeletion = null },
            title = { Text("Hapus Transaksi Ini?") },
            text = { Text("Catatan transaksi \"${target.title}\" akan dihapus dari riwayat keuangan.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(target)
                        txPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_tx_button")
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { txPendingDeletion = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun BalanceSummaryCard(
    uiState: FinanceUiState,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val totalFlow = (uiState.totalIncome + uiState.totalExpense).coerceAtLeast(1L)
    val incomeRatio = (uiState.totalIncome.toFloat() / totalFlow.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primary, primary.copy(alpha = 0.86f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Bersih (${uiState.period.label})",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = primaryContainer.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "${uiState.transactions.size} transaksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = formatRupiah(uiState.netBalance),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier.testTag("finance_net_balance_text")
                )

                // Proportion bar
                if (uiState.totalIncome > 0L || uiState.totalExpense > 0L) {
                    LinearProgressIndicator(
                        progress = { incomeRatio },
                        color = Color(0xFF86EFAC),
                        trackColor = Color(0xFFFCA5A5),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Pemasukan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatRupiah(uiState.totalIncome),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color(0xFF991B1B),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Pengeluaran",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatRupiah(uiState.totalExpense),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownCard(
    uiState: FinanceUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pengeluaran per Kategori",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            uiState.expenseBreakdown.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${formatRupiah(item.amount)} (${(item.fraction * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.fraction },
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRowCard(
    transaction: TransactionEntity,
    imageStorageManager: ImageStorageManager,
    onClick: () -> Unit,
    onReceiptClick: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val badgeBg = if (isIncome) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val badgeTint = if (isIncome) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_card_${transaction.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = badgeBg,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = categoryIconFor(transaction.category),
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.category} • ${formatTransactionDate(transaction.transactionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            transaction.receiptImageName?.let { fileName ->
                val file = remember(fileName) { imageStorageManager.getImageFile(fileName) }
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = file,
                    contentDescription = "Lihat foto struk",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onReceiptClick(fileName) }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatRupiah(transaction.amount, showSign = true, isIncome = isIncome),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isIncome) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_tx_button_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus transaksi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FinanceEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        Text(
            text = "Belum Ada Catatan Keuangan",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Catat pemasukan dan pengeluaran harian beserta foto struk agar arus kas tetap terkontrol.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("empty_add_transaction_button")
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Catat Transaksi Pertama", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun categoryIconFor(category: String): ImageVector = when (category) {
    "Makan" -> Icons.Outlined.Restaurant
    "Transport" -> Icons.Outlined.DirectionsCar
    "Belanja" -> Icons.Outlined.ShoppingBag
    "Tagihan" -> Icons.Outlined.Receipt
    "Kesehatan" -> Icons.Outlined.FavoriteBorder
    "Hiburan" -> Icons.Outlined.Movie
    "Gaji" -> Icons.Outlined.Payments
    "Usaha" -> Icons.Outlined.Storefront
    "Investasi" -> Icons.Outlined.TrendingUp
    "Bonus", "Hadiah" -> Icons.Outlined.Savings
    else -> Icons.Outlined.Category
}
