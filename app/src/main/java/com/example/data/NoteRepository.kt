package com.example.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao,
    private val transactionDao: TransactionDao,
    private val activityDao: ActivityDao,
    val imageStorageManager: ImageStorageManager,
    private val backupArchiveManager: BackupArchiveManager,
    private val preferencesStore: PreferencesStore
) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.observeAllNotes()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.observeAllTransactions()
    val allActivities: Flow<List<ActivityEntity>> = activityDao.observeAllActivities()
    val isGridView: Flow<Boolean> = preferencesStore.isGridView
    val lastBackupAt: Flow<Long?> = preferencesStore.lastBackupAt

    fun observeNoteById(id: String): Flow<NoteEntity?> = noteDao.observeNoteById(id)

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun createDraftNote(): NoteEntity {
        val now = System.currentTimeMillis()
        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = "",
            content = "",
            category = "Umum",
            colorKey = "DEFAULT",
            isPinned = false,
            imageNamesJson = "[]",
            createdAt = now,
            updatedAt = now,
            backedUpAt = null
        )
        noteDao.upsertNote(note)
        return note
    }

    suspend fun saveNote(note: NoteEntity) {
        val updated = note.copy(updatedAt = System.currentTimeMillis())
        noteDao.upsertNote(updated)
    }

    suspend fun togglePin(note: NoteEntity) {
        noteDao.upsertNote(
            note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(note: NoteEntity) {
        val images = note.imageFileNames()
        noteDao.deleteNoteById(note.id)
        if (images.isNotEmpty()) {
            imageStorageManager.deleteImageFiles(images)
        }
    }

    suspend fun deleteEmptyNoteIfBlank(noteId: String) {
        val current = noteDao.getNoteById(noteId) ?: return
        if (current.title.isBlank() && current.content.isBlank() && current.imageFileNames().isEmpty()) {
            noteDao.deleteNoteById(noteId)
        }
    }

    suspend fun addImagesFromUris(noteId: String, uris: List<Uri>): List<String> {
        val current = noteDao.getNoteById(noteId) ?: return emptyList()
        val existing = current.imageFileNames().toMutableList()
        val added = mutableListOf<String>()
        for (uri in uris) {
            val savedName = imageStorageManager.saveImageFromUri(uri)
            if (savedName != null) {
                existing.add(savedName)
                added.add(savedName)
            }
        }
        if (added.isNotEmpty()) {
            noteDao.upsertNote(
                current.copy(
                    imageNamesJson = NoteEntity.serializeImageNames(existing),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return added
    }

    suspend fun addImageFromTempCameraFile(noteId: String, tempFile: File): String? {
        val current = noteDao.getNoteById(noteId) ?: return null
        val savedName = imageStorageManager.saveImageFromTempFile(tempFile) ?: return null
        val existing = current.imageFileNames().toMutableList().apply { add(savedName) }
        noteDao.upsertNote(
            current.copy(
                imageNamesJson = NoteEntity.serializeImageNames(existing),
                updatedAt = System.currentTimeMillis()
            )
        )
        return savedName
    }

    suspend fun removeImageFromNote(noteId: String, fileName: String) {
        val current = noteDao.getNoteById(noteId) ?: return
        val updatedList = current.imageFileNames().filterNot { it == fileName }
        noteDao.upsertNote(
            current.copy(
                imageNamesJson = NoteEntity.serializeImageNames(updatedList),
                updatedAt = System.currentTimeMillis()
            )
        )
        imageStorageManager.deleteImageFile(fileName)
    }

    suspend fun saveTransaction(
        existingId: String?,
        type: TransactionType,
        amount: Long,
        title: String,
        category: String,
        note: String,
        receiptImageName: String?,
        transactionDate: Long
    ) {
        val now = System.currentTimeMillis()
        val existing = existingId?.let { transactionDao.getTransactionById(it) }
        if (existing?.receiptImageName != null && existing.receiptImageName != receiptImageName) {
            imageStorageManager.deleteImageFile(existing.receiptImageName)
        }
        val entity = TransactionEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            type = type.key,
            amount = amount,
            title = title.trim(),
            category = category,
            note = note.trim(),
            receiptImageName = receiptImageName,
            transactionDate = transactionDate,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            backedUpAt = null
        )
        transactionDao.upsertTransaction(entity)
    }

    suspend fun saveImageFromUri(uri: Uri): String? =
        imageStorageManager.saveImageFromUri(uri)

    suspend fun deleteImageFile(fileName: String) {
        imageStorageManager.deleteImageFile(fileName)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransactionById(transaction.id)
        transaction.receiptImageName?.let { imageStorageManager.deleteImageFile(it) }
    }

    suspend fun saveActivity(
        existingId: String?,
        title: String,
        category: String,
        targetMinutes: Int,
        note: String,
        photoImageName: String?,
        markCompletedToday: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val existing = existingId?.let { activityDao.getActivityById(it) }
        if (existing?.photoImageName != null && existing.photoImageName != photoImageName) {
            imageStorageManager.deleteImageFile(existing.photoImageName)
        }
        val dates = existing?.completedDateKeys()?.toMutableSet() ?: mutableSetOf()
        if (markCompletedToday) {
            dates.add(ActivityEntity.todayDateKey())
        }
        val entity = ActivityEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            targetMinutes = targetMinutes.coerceAtLeast(0),
            note = note.trim(),
            photoImageName = photoImageName,
            completedDatesJson = ActivityEntity.serializeDateKeys(dates),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            backedUpAt = null
        )
        activityDao.upsertActivity(entity)
    }

    suspend fun toggleActivityCompletedToday(activity: ActivityEntity) {
        val today = ActivityEntity.todayDateKey()
        val dates = activity.completedDateKeys().toMutableSet()
        if (today in dates) {
            dates.remove(today)
        } else {
            dates.add(today)
        }
        activityDao.upsertActivity(
            activity.copy(
                completedDatesJson = ActivityEntity.serializeDateKeys(dates),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        activityDao.deleteActivityById(activity.id)
        activity.photoImageName?.let { imageStorageManager.deleteImageFile(it) }
    }

    suspend fun setGridView(isGrid: Boolean) {
        preferencesStore.setGridView(isGrid)
    }

    suspend fun calculateTotalMediaBytes(): Long =
        imageStorageManager.calculateTotalMediaBytes()

    suspend fun exportBackupToUri(destUri: Uri): Result<BackupResult> = runCatching {
        val notesSnapshot = noteDao.getAllNotesSnapshot()
        val txSnapshot = transactionDao.getAllTransactionsSnapshot()
        val actSnapshot = activityDao.getAllActivitiesSnapshot()
        val result = backupArchiveManager.exportBackup(destUri, notesSnapshot, txSnapshot, actSnapshot)
        noteDao.markAllBackedUp(result.timestamp)
        transactionDao.markAllBackedUp(result.timestamp)
        activityDao.markAllBackedUp(result.timestamp)
        preferencesStore.setLastBackupAt(result.timestamp)
        result
    }

    suspend fun importBackupFromUri(sourceUri: Uri): Result<BackupResult> = runCatching {
        val payload = backupArchiveManager.importBackup(sourceUri)
        if (payload.notes.isNotEmpty()) {
            noteDao.upsertNotes(payload.notes)
        }
        if (payload.transactions.isNotEmpty()) {
            transactionDao.upsertTransactions(payload.transactions)
        }
        if (payload.activities.isNotEmpty()) {
            activityDao.upsertActivities(payload.activities)
        }
        preferencesStore.setLastBackupAt(payload.result.timestamp)
        payload.result
    }
}
