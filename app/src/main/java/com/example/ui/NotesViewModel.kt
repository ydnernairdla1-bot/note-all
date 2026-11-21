package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityEntity
import com.example.data.ImageStorageManager
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotesFilter(val label: String) {
    ALL("Semua"),
    PINNED("Disematkan"),
    WITH_IMAGES("Bergambar")
}

data class HomeUiState(
    val allNotes: List<NoteEntity> = emptyList(),
    val filteredNotes: List<NoteEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val allActivities: List<ActivityEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: NotesFilter = NotesFilter.ALL,
    val selectedCategory: String? = null,
    val isGridView: Boolean = true,
    val lastBackupAt: Long? = null,
    val totalMediaBytes: Long = 0L,
    val isBackupBusy: Boolean = false,
    val statusMessage: String? = null,
    val showLocalNoticeBanner: Boolean = true
) {
    val totalNotesCount: Int get() = allNotes.size
    val totalTransactionsCount: Int get() = allTransactions.size
    val totalActivitiesCount: Int get() = allActivities.size
    val totalImagesCount: Int
        get() = allNotes.sumOf { it.imageFileNames().size } +
            allTransactions.count { !it.receiptImageName.isNullOrBlank() } +
            allActivities.count { !it.photoImageName.isNullOrBlank() }
    val pendingBackupCount: Int
        get() = allNotes.count { !it.isBackedUpUpToDate } +
            allTransactions.count { !it.isBackedUpUpToDate } +
            allActivities.count { !it.isBackedUpUpToDate }
}

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(NotesFilter.ALL)
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val totalMediaBytes = MutableStateFlow(0L)
    private val isBackupBusy = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val showLocalNoticeBanner = MutableStateFlow(true)

    val imageStorageManager: ImageStorageManager
        get() = repository.imageStorageManager

    private val filterParams = combine(
        searchQuery,
        selectedFilter,
        selectedCategory
    ) { query, filter, category ->
        Triple(query, filter, category)
    }

    private val backupStats = combine(
        repository.lastBackupAt,
        totalMediaBytes,
        isBackupBusy,
        statusMessage,
        showLocalNoticeBanner
    ) { lastBackup, mediaBytes, busy, message, showBanner ->
        BackupOverlayState(lastBackup, mediaBytes, busy, message, showBanner)
    }

    private val recordsFlow = combine(
        repository.allNotes,
        repository.allTransactions,
        repository.allActivities
    ) { notes, transactions, activities ->
        Triple(notes, transactions, activities)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        recordsFlow,
        repository.isGridView,
        filterParams,
        backupStats
    ) { (notes, transactions, activities), isGrid, (query, filter, category), stats ->
        val trimmedQuery = query.trim().lowercase()
        val filtered = notes.filter { note ->
            val matchesFilter = when (filter) {
                NotesFilter.ALL -> true
                NotesFilter.PINNED -> note.isPinned
                NotesFilter.WITH_IMAGES -> note.imageFileNames().isNotEmpty()
            }
            val matchesCategory = category == null || note.category == category
            val matchesQuery = trimmedQuery.isEmpty() ||
                note.title.lowercase().contains(trimmedQuery) ||
                note.content.lowercase().contains(trimmedQuery) ||
                note.category.lowercase().contains(trimmedQuery)

            matchesFilter && matchesCategory && matchesQuery
        }

        HomeUiState(
            allNotes = notes,
            filteredNotes = filtered,
            allTransactions = transactions,
            allActivities = activities,
            searchQuery = query,
            selectedFilter = filter,
            selectedCategory = category,
            isGridView = isGrid,
            lastBackupAt = stats.lastBackupAt,
            totalMediaBytes = stats.totalMediaBytes,
            isBackupBusy = stats.isBackupBusy,
            statusMessage = stats.statusMessage,
            showLocalNoticeBanner = stats.showLocalNoticeBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        refreshStorageStats()
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            totalMediaBytes.value = repository.calculateTotalMediaBytes()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterSelected(filter: NotesFilter) {
        selectedFilter.value = filter
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.update { current -> if (current == category) null else category }
    }

    fun toggleGridView() {
        viewModelScope.launch {
            repository.setGridView(!uiState.value.isGridView)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.togglePin(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            refreshStorageStats()
            statusMessage.value = "Catatan dihapus"
        }
    }

    fun createNewNote(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val draft = repository.createDraftNote()
            onCreated(draft.id)
        }
    }

    fun exportBackup(destUri: Uri) {
        viewModelScope.launch {
            isBackupBusy.value = true
            val result = repository.exportBackupToUri(destUri)
            isBackupBusy.value = false
            result.onSuccess { info ->
                statusMessage.value = "Cadangan berhasil: ${info.noteCount} catatan, ${info.transactionCount} transaksi, ${info.activityCount} aktivitas & ${info.imageCount} foto"
            }.onFailure { err ->
                statusMessage.value = "Gagal mengekspor cadangan: ${err.localizedMessage ?: "Kesalahan tidak diketahui"}"
            }
        }
    }

    fun importBackup(sourceUri: Uri) {
        viewModelScope.launch {
            isBackupBusy.value = true
            val result = repository.importBackupFromUri(sourceUri)
            isBackupBusy.value = false
            refreshStorageStats()
            result.onSuccess { info ->
                statusMessage.value = "Pulih: ${info.noteCount} catatan, ${info.transactionCount} transaksi, ${info.activityCount} aktivitas & ${info.imageCount} foto"
            }.onFailure { err ->
                statusMessage.value = "Gagal memulihkan cadangan: ${err.localizedMessage ?: "File tidak valid"}"
            }
        }
    }

    fun dismissLocalBanner() {
        showLocalNoticeBanner.value = false
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private data class BackupOverlayState(
        val lastBackupAt: Long?,
        val totalMediaBytes: Long,
        val isBackupBusy: Boolean,
        val statusMessage: String?,
        val showLocalNoticeBanner: Boolean
    )

    companion object {
        fun factory(repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotesViewModel(repository) as T
                }
            }
    }
}
