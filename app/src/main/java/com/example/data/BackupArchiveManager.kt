package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupResult(
    val noteCount: Int,
    val transactionCount: Int,
    val activityCount: Int,
    val imageCount: Int,
    val timestamp: Long
)

data class ImportedBackupPayload(
    val notes: List<NoteEntity>,
    val transactions: List<TransactionEntity>,
    val activities: List<ActivityEntity>,
    val result: BackupResult
)

class BackupArchiveManager(
    private val context: Context,
    private val imageStorageManager: ImageStorageManager
) {
    suspend fun exportBackup(
        destUri: Uri,
        notes: List<NoteEntity>,
        transactions: List<TransactionEntity>,
        activities: List<ActivityEntity>
    ): BackupResult = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val rootJson = JSONObject().apply {
            put("version", 3)
            put("exportedAt", timestamp)
            put("notes", JSONArray().apply {
                notes.forEach { note ->
                    put(
                        JSONObject().apply {
                            put("id", note.id)
                            put("title", note.title)
                            put("content", note.content)
                            put("category", note.category)
                            put("colorKey", note.colorKey)
                            put("isPinned", note.isPinned)
                            put("imageNames", JSONArray(note.imageFileNames()))
                            put("createdAt", note.createdAt)
                            put("updatedAt", note.updatedAt)
                        }
                    )
                }
            })
            put("transactions", JSONArray().apply {
                transactions.forEach { tx ->
                    put(
                        JSONObject().apply {
                            put("id", tx.id)
                            put("type", tx.type)
                            put("amount", tx.amount)
                            put("title", tx.title)
                            put("category", tx.category)
                            put("note", tx.note)
                            if (!tx.receiptImageName.isNullOrBlank()) {
                                put("receiptImageName", tx.receiptImageName)
                            }
                            put("transactionDate", tx.transactionDate)
                            put("createdAt", tx.createdAt)
                            put("updatedAt", tx.updatedAt)
                        }
                    )
                }
            })
            put("activities", JSONArray().apply {
                activities.forEach { act ->
                    put(
                        JSONObject().apply {
                            put("id", act.id)
                            put("title", act.title)
                            put("category", act.category)
                            put("targetMinutes", act.targetMinutes)
                            put("note", act.note)
                            if (!act.photoImageName.isNullOrBlank()) {
                                put("photoImageName", act.photoImageName)
                            }
                            put("completedDates", JSONArray(act.completedDateKeys().sorted()))
                            put("createdAt", act.createdAt)
                            put("updatedAt", act.updatedAt)
                        }
                    )
                }
            })
        }

        var exportedImages = 0
        val writtenImageNames = mutableSetOf<String>()

        val outputStream = context.contentResolver.openOutputStream(destUri)
            ?: throw IllegalStateException("Tidak dapat membuka file tujuan cadangan.")

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("notes.json"))
            zipOut.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()

            fun writeImageIfPresent(fileName: String) {
                if (fileName.isBlank() || !writtenImageNames.add(fileName)) return
                val file = imageStorageManager.getImageFile(fileName)
                if (file.exists()) {
                    zipOut.putNextEntry(ZipEntry("images/${file.name}"))
                    FileInputStream(file).use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                    exportedImages++
                }
            }

            notes.forEach { note ->
                note.imageFileNames().forEach(::writeImageIfPresent)
            }
            transactions.forEach { tx ->
                tx.receiptImageName?.let(::writeImageIfPresent)
            }
            activities.forEach { act ->
                act.photoImageName?.let(::writeImageIfPresent)
            }
        }

        BackupResult(
            noteCount = notes.size,
            transactionCount = transactions.size,
            activityCount = activities.size,
            imageCount = exportedImages,
            timestamp = timestamp
        )
    }

    suspend fun importBackup(sourceUri: Uri): ImportedBackupPayload =
        withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: throw IllegalStateException("Tidak dapat membaca file cadangan.")

            var notesJsonString: String? = null
            var restoredImages = 0

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && !name.contains("..")) {
                        when {
                            name == "notes.json" -> {
                                notesJsonString = zipIn.readBytes().toString(Charsets.UTF_8)
                            }
                            name.startsWith("images/") -> {
                                val safeFileName = File(name).name
                                if (safeFileName.isNotBlank()) {
                                    val destFile = imageStorageManager.getImageFile(safeFileName)
                                    FileOutputStream(destFile).use { out ->
                                        zipIn.copyTo(out)
                                    }
                                    restoredImages++
                                }
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val jsonText = notesJsonString
                ?: throw IllegalArgumentException("File ZIP bukan cadangan CloudNote yang valid.")
            val root = JSONObject(jsonText)
            val notesArray = root.optJSONArray("notes") ?: JSONArray()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val actArray = root.optJSONArray("activities") ?: JSONArray()
            val now = System.currentTimeMillis()

            val restoredNotes = buildList(notesArray.length()) {
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    val imageArr = obj.optJSONArray("imageNames") ?: JSONArray()
                    val imageNames = buildList {
                        for (j in 0 until imageArr.length()) {
                            val img = imageArr.optString(j)
                            if (img.isNotBlank()) add(File(img).name)
                        }
                    }
                    add(
                        NoteEntity(
                            id = id,
                            title = obj.optString("title"),
                            content = obj.optString("content"),
                            category = obj.optString("category").ifBlank { "Umum" },
                            colorKey = obj.optString("colorKey").ifBlank { "DEFAULT" },
                            isPinned = obj.optBoolean("isPinned", false),
                            imageNamesJson = NoteEntity.serializeImageNames(imageNames),
                            createdAt = obj.optLong("createdAt", now),
                            updatedAt = obj.optLong("updatedAt", now),
                            backedUpAt = now
                        )
                    )
                }
            }

            val restoredTransactions = buildList(txArray.length()) {
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    val rawReceipt = obj.optString("receiptImageName")
                        .takeIf { it.isNotBlank() }
                        ?.let { File(it).name }
                    add(
                        TransactionEntity(
                            id = id,
                            type = obj.optString("type").ifBlank { TransactionType.EXPENSE.key },
                            amount = obj.optLong("amount", 0L).coerceAtLeast(0L),
                            title = obj.optString("title"),
                            category = obj.optString("category").ifBlank { "Lainnya" },
                            note = obj.optString("note"),
                            receiptImageName = rawReceipt,
                            transactionDate = obj.optLong("transactionDate", now),
                            createdAt = obj.optLong("createdAt", now),
                            updatedAt = obj.optLong("updatedAt", now),
                            backedUpAt = now
                        )
                    )
                }
            }

            val restoredActivities = buildList(actArray.length()) {
                for (i in 0 until actArray.length()) {
                    val obj = actArray.getJSONObject(i)
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    val rawPhoto = obj.optString("photoImageName")
                        .takeIf { it.isNotBlank() }
                        ?.let { File(it).name }
                    val datesArr = obj.optJSONArray("completedDates") ?: JSONArray()
                    val dateSet = buildSet {
                        for (j in 0 until datesArr.length()) {
                            val d = datesArr.optString(j)
                            if (d.isNotBlank()) add(d)
                        }
                    }
                    add(
                        ActivityEntity(
                            id = id,
                            title = obj.optString("title"),
                            category = obj.optString("category").ifBlank { "Olahraga" },
                            targetMinutes = obj.optInt("targetMinutes", 30).coerceAtLeast(0),
                            note = obj.optString("note"),
                            photoImageName = rawPhoto,
                            completedDatesJson = ActivityEntity.serializeDateKeys(dateSet),
                            createdAt = obj.optLong("createdAt", now),
                            updatedAt = obj.optLong("updatedAt", now),
                            backedUpAt = now
                        )
                    )
                }
            }

            ImportedBackupPayload(
                notes = restoredNotes,
                transactions = restoredTransactions,
                activities = restoredActivities,
                result = BackupResult(
                    noteCount = restoredNotes.size,
                    transactionCount = restoredTransactions.size,
                    activityCount = restoredActivities.size,
                    imageCount = restoredImages,
                    timestamp = now
                )
            )
        }
}
