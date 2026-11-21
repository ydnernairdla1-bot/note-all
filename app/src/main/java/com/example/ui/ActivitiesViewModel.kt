package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityEntity
import com.example.data.ImageStorageManager
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActivityStatusFilter(val label: String) {
    ALL("Semua"),
    PENDING("Belum Selesai"),
    COMPLETED("Selesai Hari Ini")
}

data class ActivitiesUiState(
    val allActivities: List<ActivityEntity> = emptyList(),
    val filteredActivities: List<ActivityEntity> = emptyList(),
    val statusFilter: ActivityStatusFilter = ActivityStatusFilter.ALL,
    val selectedCategory: String? = null,
    val todayKey: String = ActivityEntity.todayDateKey(),
    val last7Days: List<Pair<String, String>> = ActivityEntity.last7DayKeys(),
    val statusMessage: String? = null
) {
    val totalCount: Int get() = allActivities.size
    val completedTodayCount: Int get() = allActivities.count { it.isCompletedOn(todayKey) }
    val completedMinutesToday: Int
        get() = allActivities.filter { it.isCompletedOn(todayKey) }.sumOf { it.targetMinutes }
    val bestStreak: Int
        get() = allActivities.maxOfOrNull { it.currentStreak(todayKey) } ?: 0
    val completionRatio: Float
        get() = if (totalCount == 0) 0f else (completedTodayCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
}

class ActivitiesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val statusFilter = MutableStateFlow(ActivityStatusFilter.ALL)
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val imageStorageManager: ImageStorageManager
        get() = repository.imageStorageManager

    val uiState: StateFlow<ActivitiesUiState> = combine(
        repository.allActivities,
        statusFilter,
        selectedCategory,
        statusMessage
    ) { activities, filter, category, message ->
        val today = ActivityEntity.todayDateKey()
        val last7 = ActivityEntity.last7DayKeys()

        val filtered = activities.filter { act ->
            val doneToday = act.isCompletedOn(today)
            val matchesStatus = when (filter) {
                ActivityStatusFilter.ALL -> true
                ActivityStatusFilter.PENDING -> !doneToday
                ActivityStatusFilter.COMPLETED -> doneToday
            }
            val matchesCategory = category == null || act.category == category
            matchesStatus && matchesCategory
        }.sortedWith(
            compareBy<ActivityEntity> { it.isCompletedOn(today) }
                .thenByDescending { it.updatedAt }
        )

        ActivitiesUiState(
            allActivities = activities,
            filteredActivities = filtered,
            statusFilter = filter,
            selectedCategory = category,
            todayKey = today,
            last7Days = last7,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivitiesUiState()
    )

    fun onStatusFilterSelected(filter: ActivityStatusFilter) {
        statusFilter.value = filter
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.update { current -> if (current == category) null else category }
    }

    fun toggleCompletedToday(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.toggleActivityCompletedToday(activity)
        }
    }

    fun saveActivity(
        existingId: String?,
        title: String,
        category: String,
        targetMinutes: Int,
        note: String,
        photoImageName: String?,
        markCompletedToday: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveActivity(
                existingId = existingId,
                title = title,
                category = category,
                targetMinutes = targetMinutes,
                note = note,
                photoImageName = photoImageName,
                markCompletedToday = markCompletedToday
            )
            statusMessage.value = if (existingId == null) {
                "Aktivitas \"$title\" ditambahkan"
            } else {
                "Aktivitas diperbarui"
            }
            onComplete()
        }
    }

    fun addQuickPreset(title: String, category: String, targetMinutes: Int) {
        viewModelScope.launch {
            repository.saveActivity(
                existingId = null,
                title = title,
                category = category,
                targetMinutes = targetMinutes,
                note = "",
                photoImageName = null,
                markCompletedToday = false
            )
            statusMessage.value = "\"$title\" ditambahkan ke daftar harian"
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
            statusMessage.value = "Aktivitas dihapus"
        }
    }

    suspend fun saveActivityPhoto(uri: Uri): String? =
        repository.saveImageFromUri(uri)

    fun deleteUnusedPhoto(fileName: String) {
        viewModelScope.launch {
            repository.deleteImageFile(fileName)
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    companion object {
        fun factory(repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActivitiesViewModel(repository) as T
                }
            }
    }
}
