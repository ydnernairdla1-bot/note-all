package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ImageStorageManager
import com.example.data.NoteRepository
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FinancePeriod(val label: String) {
    THIS_MONTH("Bulan Ini"),
    LAST_7_DAYS("7 Hari"),
    ALL_TIME("Semua")
}

enum class FinanceTypeFilter(val label: String) {
    ALL("Semua"),
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran")
}

data class CategoryBreakdown(
    val category: String,
    val amount: Long,
    val fraction: Float
)

data class FinanceUiState(
    val period: FinancePeriod = FinancePeriod.THIS_MONTH,
    val typeFilter: FinanceTypeFilter = FinanceTypeFilter.ALL,
    val searchQuery: String = "",
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val expenseBreakdown: List<CategoryBreakdown> = emptyList(),
    val statusMessage: String? = null
) {
    val netBalance: Long get() = totalIncome - totalExpense
}

class FinanceViewModel(private val repository: NoteRepository) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(FinancePeriod.THIS_MONTH)
    private val selectedTypeFilter = MutableStateFlow(FinanceTypeFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val statusMessage = MutableStateFlow<String?>(null)

    val imageStorageManager: ImageStorageManager
        get() = repository.imageStorageManager

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        selectedPeriod,
        selectedTypeFilter,
        searchQuery,
        statusMessage
    ) { allTx, period, typeFilter, query, message ->
        val periodStartMs = calculatePeriodStartMs(period)
        val periodTx = if (periodStartMs == null) {
            allTx
        } else {
            allTx.filter { it.transactionDate >= periodStartMs }
        }

        val totalIncome = periodTx
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = periodTx
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val expenseBreakdown = if (totalExpense > 0L) {
            periodTx
                .filter { it.transactionType == TransactionType.EXPENSE }
                .groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    CategoryBreakdown(
                        category = cat,
                        amount = sum,
                        fraction = (sum.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f)
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
        } else {
            emptyList()
        }

        val trimmed = query.trim().lowercase()
        val filteredList = periodTx.filter { tx ->
            val matchesType = when (typeFilter) {
                FinanceTypeFilter.ALL -> true
                FinanceTypeFilter.INCOME -> tx.transactionType == TransactionType.INCOME
                FinanceTypeFilter.EXPENSE -> tx.transactionType == TransactionType.EXPENSE
            }
            val matchesQuery = trimmed.isEmpty() ||
                tx.title.lowercase().contains(trimmed) ||
                tx.category.lowercase().contains(trimmed) ||
                tx.note.lowercase().contains(trimmed)
            matchesType && matchesQuery
        }

        FinanceUiState(
            period = period,
            typeFilter = typeFilter,
            searchQuery = query,
            transactions = filteredList,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            expenseBreakdown = expenseBreakdown,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    fun onPeriodSelected(period: FinancePeriod) {
        selectedPeriod.value = period
    }

    fun onTypeFilterSelected(filter: FinanceTypeFilter) {
        selectedTypeFilter.value = filter
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun saveTransaction(
        existingId: String?,
        type: TransactionType,
        amount: Long,
        title: String,
        category: String,
        note: String,
        receiptImageName: String?,
        transactionDate: Long,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveTransaction(
                existingId = existingId,
                type = type,
                amount = amount,
                title = title,
                category = category,
                note = note,
                receiptImageName = receiptImageName,
                transactionDate = transactionDate
            )
            statusMessage.value = if (existingId == null) {
                "${type.label} tersimpan"
            } else {
                "Transaksi diperbarui"
            }
            onComplete()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            statusMessage.value = "Transaksi dihapus"
        }
    }

    suspend fun saveReceiptImage(uri: Uri): String? =
        repository.saveImageFromUri(uri)

    fun deleteUnusedReceipt(fileName: String) {
        viewModelScope.launch {
            repository.deleteImageFile(fileName)
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private fun calculatePeriodStartMs(period: FinancePeriod): Long? {
        val cal = Calendar.getInstance()
        return when (period) {
            FinancePeriod.ALL_TIME -> null
            FinancePeriod.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            FinancePeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
    }

    companion object {
        fun factory(repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinanceViewModel(repository) as T
                }
            }
    }
}
