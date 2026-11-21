package com.example.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRelativeTime(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    if (diff < 60_000L) return "Baru saja"
    val minutes = diff / 60_000L
    if (minutes < 60L) return "$minutes mnt lalu"
    val hours = diff / 3_600_000L
    if (hours < 24L) return "$hours jam lalu"
    val days = diff / 86_400_000L
    if (days == 1L) return "Kemarin"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestampMs))
}

fun formatFullTimestamp(timestampMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestampMs))
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.2f MB", mb)
}
