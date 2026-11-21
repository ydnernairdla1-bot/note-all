package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val key: String, val label: String) {
    EXPENSE("EXPENSE", "Pengeluaran"),
    INCOME("INCOME", "Pemasukan");

    companion object {
        fun fromKey(key: String): TransactionType =
            entries.firstOrNull { it.key == key } ?: EXPENSE
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val amount: Long,
    val title: String,
    val category: String,
    val note: String = "",
    val receiptImageName: String? = null,
    val transactionDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val backedUpAt: Long? = null
) {
    val transactionType: TransactionType
        get() = TransactionType.fromKey(type)

    val isBackedUpUpToDate: Boolean
        get() = backedUpAt != null && backedUpAt >= updatedAt

    companion object {
        val EXPENSE_CATEGORIES = listOf(
            "Makan",
            "Transport",
            "Belanja",
            "Tagihan",
            "Kesehatan",
            "Hiburan",
            "Lainnya"
        )

        val INCOME_CATEGORIES = listOf(
            "Gaji",
            "Usaha",
            "Bonus",
            "Investasi",
            "Hadiah",
            "Lainnya"
        )
    }
}
