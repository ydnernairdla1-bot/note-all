package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String = "Umum",
    val colorKey: String = "DEFAULT",
    val isPinned: Boolean = false,
    val imageNamesJson: String = "[]",
    val createdAt: Long,
    val updatedAt: Long,
    val backedUpAt: Long? = null
) {
    fun imageFileNames(): List<String> = parseImageNames(imageNamesJson)

    val isBackedUpUpToDate: Boolean
        get() = backedUpAt != null && backedUpAt >= updatedAt

    companion object {
        val AVAILABLE_CATEGORIES = listOf("Umum", "Pribadi", "Kerja", "Ide", "Penting")

        fun parseImageNames(json: String): List<String> {
            return try {
                val arr = JSONArray(json)
                buildList(arr.length()) {
                    for (i in 0 until arr.length()) {
                        val value = arr.optString(i)
                        if (value.isNotBlank()) add(value)
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun serializeImageNames(names: List<String>): String {
            val arr = JSONArray()
            names.forEach { arr.put(it) }
            return arr.toString()
        }
    }
}
