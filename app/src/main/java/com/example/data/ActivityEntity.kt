package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String = "Olahraga",
    val targetMinutes: Int = 30,
    val note: String = "",
    val photoImageName: String? = null,
    val completedDatesJson: String = "[]",
    val createdAt: Long,
    val updatedAt: Long,
    val backedUpAt: Long? = null
) {
    fun completedDateKeys(): Set<String> = parseDateKeys(completedDatesJson)

    fun isCompletedOn(dateKey: String): Boolean = dateKey in completedDateKeys()

    fun currentStreak(todayKey: String = todayDateKey()): Int {
        val dates = completedDateKeys()
        if (dates.isEmpty()) return 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        if (todayKey !in dates) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (true) {
            val key = sdf.format(cal.time)
            if (key in dates) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    val isBackedUpUpToDate: Boolean
        get() = backedUpAt != null && backedUpAt >= updatedAt

    companion object {
        val AVAILABLE_CATEGORIES = listOf(
            "Olahraga",
            "Kesehatan",
            "Belajar",
            "Ibadah",
            "Produktivitas",
            "Hobi",
            "Lainnya"
        )

        val QUICK_PRESETS = listOf(
            Triple("Lari / Jogging", "Olahraga", 30),
            Triple("Workout / Gym", "Olahraga", 45),
            Triple("Jalan Kaki", "Kesehatan", 20),
            Triple("Membaca Buku", "Belajar", 20),
            Triple("Minum Air 2L", "Kesehatan", 0)
        )

        fun todayDateKey(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun last7DayKeys(): List<Pair<String, String>> {
            val keySdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val labelSdf = SimpleDateFormat("EEEEE", Locale("id", "ID"))
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            return buildList(7) {
                repeat(7) {
                    add(keySdf.format(cal.time) to labelSdf.format(cal.time).uppercase())
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        fun parseDateKeys(json: String): Set<String> {
            return try {
                val arr = JSONArray(json)
                buildSet(arr.length()) {
                    for (i in 0 until arr.length()) {
                        val value = arr.optString(i)
                        if (value.isNotBlank()) add(value)
                    }
                }
            } catch (_: Exception) {
                emptySet()
            }
        }

        fun serializeDateKeys(dates: Set<String>): String {
            val arr = JSONArray()
            dates.sorted().forEach { arr.put(it) }
            return arr.toString()
        }
    }
}
