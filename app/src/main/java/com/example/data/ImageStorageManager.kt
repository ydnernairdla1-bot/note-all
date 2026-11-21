package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

class ImageStorageManager(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "note_images").apply {
            if (!exists()) mkdirs()
        }

    private val cameraCacheDir: File
        get() = File(context.cacheDir, "camera").apply {
            if (!exists()) mkdirs()
        }

    fun getImageFile(fileName: String): File {
        val safeName = File(fileName).name
        return File(imagesDir, safeName)
    }

    fun getShareableUri(fileName: String): Uri? {
        val file = getImageFile(fileName)
        if (!file.exists()) return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun createCameraCaptureUri(): Pair<Uri, File> {
        val tempFile = File(cameraCacheDir, "capture_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return uri to tempFile
    }

    suspend fun saveImageFromUri(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeScaledBitmap(sourceUri, maxDimension = 1920) ?: return@withContext null
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val destFile = getImageFile(fileName)
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            bitmap.recycle()
            fileName
        }.getOrNull()
    }

    suspend fun saveImageFromTempFile(tempFile: File): String? = withContext(Dispatchers.IO) {
        if (!tempFile.exists() || tempFile.length() == 0L) return@withContext null
        val uri = Uri.fromFile(tempFile)
        val savedName = saveImageFromUri(uri)
        tempFile.delete()
        savedName
    }

    suspend fun deleteImageFile(fileName: String) = withContext(Dispatchers.IO) {
        val file = getImageFile(fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    suspend fun deleteImageFiles(fileNames: List<String>) = withContext(Dispatchers.IO) {
        fileNames.forEach { name ->
            val file = getImageFile(name)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    suspend fun calculateTotalMediaBytes(): Long = withContext(Dispatchers.IO) {
        imagesDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun decodeScaledBitmap(uri: Uri, maxDimension: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val largest = max(info.size.width, info.size.height)
                if (largest > maxDimension) {
                    val scale = maxDimension.toFloat() / largest.toFloat()
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1)
                    )
                }
            }
        }

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val origWidth = boundsOptions.outWidth
        val origHeight = boundsOptions.outHeight
        if (origWidth <= 0 || origHeight <= 0) return null

        var sampleSize = 1
        while (max(origWidth / sampleSize, origHeight / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        if (rotationDegrees == 0f) return decoded
        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (rotated != decoded) decoded.recycle()
        return rotated
    }
}
