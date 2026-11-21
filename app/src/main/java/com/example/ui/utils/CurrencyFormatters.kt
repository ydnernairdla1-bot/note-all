package com.example.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val indonesianLocale = Locale("id", "ID")

fun formatRupiah(amount: Long, showSign: Boolean = false, isIncome: Boolean = true): String {
    val formatter = NumberFormat.getNumberInstance(indonesianLocale)
    val absFormatted = formatter.format(kotlin.math.abs(amount))
    return when {
        showSign -> if (isIncome) "+Rp $absFormatted" else "-Rp $absFormatted"
        amount < 0 -> "-Rp $absFormatted"
        else -> "Rp $absFormatted"
    }
}

fun formatTransactionDate(timestampMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
    return sdf.format(Date(timestampMs))
}
