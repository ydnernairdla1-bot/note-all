package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ImageStorageManager
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class SaveStatus {
    IDLE,
    SAVING,
    SAVED
}

data class NoteEditorUiState(
    val noteId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Umum",
    val colorKey: String = "DEFAULT",
    val isPinned: Boolean = false,
    val imageNames: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val backedUpAt: Long? = null,
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val isLoading: Boolean = true,
    val isAttachingImages: Boolean = false,
    val errorMessage: String? = null
)

class NoteEditorViewModel(
    private val noteId: String,
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    val imageStorageManager: ImageStorageManager
        get() = repository.imageStorageManager

    private var autoSaveJob: Job? = null
    private var loadedEntity: NoteEntity? = null
    private var isDeleted = false

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            val entity = repository.getNoteById(noteId) ?: repository.createDraftNote()
            loadedEntity = entity
            _uiState.value = NoteEditorUiState(
                noteId = entity.id,
                title = entity.title,
                content = entity.content,
                category = entity.category,
                colorKey = entity.colorKey,
                isPinned = entity.isPinned,
                imageNames = entity.imageFileNames(),
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                backedUpAt = entity.backedUpAt,
                saveStatus = SaveStatus.SAVED,
                isLoading = false
            )
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category, saveStatus = SaveStatus.SAVING) }
        saveImmediately()
    }

    fun onColorChange(colorKey: String) {
        _uiState.update { it.copy(colorKey = colorKey, saveStatus = SaveStatus.SAVING) }
        saveImmediately()
    }

    fun onTogglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned, saveStatus = SaveStatus.SAVING) }
        saveImmediately()
    }

    fun addImagesFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val currentId = _uiState.value.noteId
        if (currentId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAttachingImages = true, saveStatus = SaveStatus.SAVING) }
            persistCurrentFields()
            val added = repository.addImagesFromUris(currentId, uris)
            val refreshed = repository.getNoteById(currentId)
            _uiState.update { state ->
                state.copy(
                    imageNames = refreshed?.imageFileNames() ?: (state.imageNames + added),
                    updatedAt = refreshed?.updatedAt ?: System.currentTimeMillis(),
                    isAttachingImages = false,
                    saveStatus = SaveStatus.SAVED,
                    errorMessage = if (added.isEmpty()) "Gagal memproses gambar yang dipilih" else null
                )
            }
        }
    }

    fun addImageFromCameraFile(tempFile: File) {
        val currentId = _uiState.value.noteId
        if (currentId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAttachingImages = true, saveStatus = SaveStatus.SAVING) }
            persistCurrentFields()
            val saved = repository.addImageFromTempCameraFile(currentId, tempFile)
            val refreshed = repository.getNoteById(currentId)
            _uiState.update { state ->
                state.copy(
                    imageNames = refreshed?.imageFileNames() ?: state.imageNames,
                    updatedAt = refreshed?.updatedAt ?: System.currentTimeMillis(),
                    isAttachingImages = false,
                    saveStatus = SaveStatus.SAVED,
                    errorMessage = if (saved == null) "Gagal menyimpan foto dari kamera" else null
                )
            }
        }
    }

    fun removeImage(fileName: String) {
        val currentId = _uiState.value.noteId
        if (currentId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(saveStatus = SaveStatus.SAVING) }
            persistCurrentFields()
            repository.removeImageFromNote(currentId, fileName)
            val refreshed = repository.getNoteById(currentId)
            _uiState.update { state ->
                state.copy(
                    imageNames = refreshed?.imageFileNames() ?: state.imageNames.filterNot { it == fileName },
                    updatedAt = refreshed?.updatedAt ?: System.currentTimeMillis(),
                    saveStatus = SaveStatus.SAVED
                )
            }
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        isDeleted = true
        autoSaveJob?.cancel()
        viewModelScope.launch {
            val current = repository.getNoteById(_uiState.value.noteId) ?: buildEntityFromState()
            repository.deleteNote(current)
            onDeleted()
        }
    }

    fun flushAndExit(onDone: () -> Unit) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            if (!isDeleted) {
                val state = _uiState.value
                if (state.title.isBlank() && state.content.isBlank() && state.imageNames.isEmpty()) {
                    repository.deleteEmptyNoteIfBlank(state.noteId)
                } else {
                    persistCurrentFields()
                }
            }
            onDone()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(450L)
            persistCurrentFields()
        }
    }

    private fun saveImmediately() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            persistCurrentFields()
        }
    }

    private suspend fun persistCurrentFields() {
        if (isDeleted) return
        val state = _uiState.value
        if (state.noteId.isBlank()) return
        val entity = buildEntityFromState()
        repository.saveNote(entity)
        loadedEntity = entity
        _uiState.update { it.copy(updatedAt = entity.updatedAt, saveStatus = SaveStatus.SAVED) }
    }

    private fun buildEntityFromState(): NoteEntity {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        return NoteEntity(
            id = state.noteId,
            title = state.title,
            content = state.content,
            category = state.category,
            colorKey = state.colorKey,
            isPinned = state.isPinned,
            imageNamesJson = NoteEntity.serializeImageNames(state.imageNames),
            createdAt = state.createdAt,
            updatedAt = now,
            backedUpAt = state.backedUpAt
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (!isDeleted) {
            val state = _uiState.value
            if (state.noteId.isNotBlank()) {
                kotlinx.coroutines.CoroutineScope(NonCancellable).launch {
                    if (state.title.isBlank() && state.content.isBlank() && state.imageNames.isEmpty()) {
                        repository.deleteEmptyNoteIfBlank(state.noteId)
                    } else {
                        repository.saveNote(buildEntityFromState())
                    }
                }
            }
        }
    }

    companion object {
        fun factory(noteId: String, repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteEditorViewModel(noteId, repository) as T
                }
            }
    }
}
