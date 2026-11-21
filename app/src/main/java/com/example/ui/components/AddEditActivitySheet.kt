package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ActivityEntity
import com.example.data.ImageStorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivitySheet(
    existingActivity: ActivityEntity?,
    imageStorageManager: ImageStorageManager,
    onSavePhotoFromUri: suspend (Uri) -> String?,
    onDeleteUnusedPhoto: (String) -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        category: String,
        targetMinutes: Int,
        note: String,
        photoImageName: String?,
        markCompletedToday: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var title by remember(existingActivity) {
        mutableStateOf(existingActivity?.title ?: "")
    }
    var category by remember(existingActivity) {
        mutableStateOf(existingActivity?.category ?: ActivityEntity.AVAILABLE_CATEGORIES.first())
    }
    var minutesText by remember(existingActivity) {
        mutableStateOf((existingActivity?.targetMinutes ?: 30).toString())
    }
    var note by remember(existingActivity) {
        mutableStateOf(existingActivity?.note ?: "")
    }
    var photoImageName by remember(existingActivity) {
        mutableStateOf(existingActivity?.photoImageName)
    }
    var newlyUploadedPhoto by remember { mutableStateOf<String?>(null) }
    var markCompletedToday by remember(existingActivity) {
        mutableStateOf(existingActivity?.isCompletedOn(ActivityEntity.todayDateKey()) ?: false)
    }
    var isSavingPhoto by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    isSavingPhoto = true
                    val savedName = onSavePhotoFromUri(uri)
                    isSavingPhoto = false
                    if (savedName != null) {
                        newlyUploadedPhoto?.let { oldTemp ->
                            if (oldTemp != existingActivity?.photoImageName) {
                                onDeleteUnusedPhoto(oldTemp)
                            }
                        }
                        newlyUploadedPhoto = savedName
                        photoImageName = savedName
                    }
                }
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            newlyUploadedPhoto?.let { tempName ->
                if (tempName != existingActivity?.photoImageName) {
                    onDeleteUnusedPhoto(tempName)
                }
            }
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (existingActivity == null) "Tambah Aktivitas Harian" else "Ubah Aktivitas",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Nama Aktivitas") },
                placeholder = { Text("Contoh: Lari pagi, Gym, Membaca buku") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_title_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(ActivityEntity.AVAILABLE_CATEGORIES) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("activity_category_${item.lowercase()}")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = minutesText,
                onValueChange = { raw ->
                    minutesText = raw.filter { it.isDigit() }.take(4)
                },
                label = { Text("Target Durasi (menit, 0 jika tanpa durasi)") },
                suffix = { Text("menit") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_minutes_input")
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(15, 30, 45, 60).forEach { preset ->
                    AssistChip(
                        onClick = { minutesText = preset.toString() },
                        label = { Text("$preset mnt") },
                        modifier = Modifier.testTag("activity_preset_${preset}m")
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan / target (opsional)") },
                placeholder = { Text("Contoh: 5 km keliling taman, 20 halaman") },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_note_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isSavingPhoto,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("activity_attach_photo_button")
                ) {
                    if (isSavingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoImageName == null) "Foto Aktivitas" else "Ganti Foto")
                }

                if (existingActivity == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = markCompletedToday,
                            onCheckedChange = { markCompletedToday = it },
                            modifier = Modifier.testTag("activity_done_today_checkbox")
                        )
                        Text(
                            text = "Selesai hari ini",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            photoImageName?.let { fileName ->
                val file = remember(fileName) { imageStorageManager.getImageFile(fileName) }
                Box(
                    modifier = Modifier
                        .size(width = 128.dp, height = 96.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Foto aktivitas",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        onClick = {
                            if (fileName == newlyUploadedPhoto && fileName != existingActivity?.photoImageName) {
                                onDeleteUnusedPhoto(fileName)
                                newlyUploadedPhoto = null
                            }
                            photoImageName = null
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Hapus foto",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            validationError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isEmpty()) {
                        validationError = "Masukkan nama aktivitas terlebih dahulu"
                        return@Button
                    }
                    val targetMinutes = minutesText.toIntOrNull() ?: 0
                    onSave(
                        existingActivity?.id,
                        trimmedTitle,
                        category,
                        targetMinutes,
                        note,
                        photoImageName,
                        markCompletedToday
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("activity_save_button")
            ) {
                Text(
                    text = if (existingActivity == null) "Simpan Aktivitas" else "Simpan Perubahan",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
