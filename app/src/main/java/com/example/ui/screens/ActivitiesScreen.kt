package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ActivityEntity
import com.example.data.ImageStorageManager
import com.example.ui.ActivitiesUiState
import com.example.ui.ActivityStatusFilter
import com.example.ui.components.AddEditActivitySheet
import com.example.ui.components.FullscreenImageDialog

@Composable
fun ActivitiesScreen(
    uiState: ActivitiesUiState,
    pendingBackupCount: Int,
    imageStorageManager: ImageStorageManager,
    snackbarHostState: SnackbarHostState,
    showActivitySheet: Boolean,
    onOpenAddActivity: () -> Unit,
    onDismissActivitySheet: () -> Unit,
    onStatusFilterSelected: (ActivityStatusFilter) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onToggleCompletedToday: (ActivityEntity) -> Unit,
    onAddQuickPreset: (title: String, category: String, targetMinutes: Int) -> Unit,
    onSavePhotoFromUri: suspend (Uri) -> String?,
    onDeleteUnusedPhoto: (String) -> Unit,
    onSaveActivity: (
        existingId: String?,
        title: String,
        category: String,
        targetMinutes: Int,
        note: String,
        photoImageName: String?,
        markCompletedToday: Boolean
    ) -> Unit,
    onDeleteActivity: (ActivityEntity) -> Unit,
    onOpenBackupCenter: () -> Unit,
    onOpenThemeCustomizer: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }
    var activityPendingDeletion by remember { mutableStateOf<ActivityEntity?>(null) }
    var photoToView by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onClearStatusMessage()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Editorial Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aktivitas Harian",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Olahraga, kebiasaan & rutinitas harian",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onOpenBackupCenter,
                    shape = CircleShape,
                    color = if (pendingBackupCount > 0) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.testTag("activities_open_backup_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Backup,
                            contentDescription = "Cadangan & Status Cloud",
                            tint = if (pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (pendingBackupCount > 0) "Cadangkan ($pendingBackupCount)" else "Cadangan",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (pendingBackupCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                IconButton(
                    onClick = onOpenThemeCustomizer,
                    modifier = Modifier.testTag("activities_open_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Ubah tema dan latar belakang",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Daily Progress Hero Card
            item {
                DailyProgressHeroCard(
                    uiState = uiState,
                    modifier = Modifier.widthIn(max = 720.dp)
                )
            }

            // Quick-Add Preset Strip
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tambah Cepat",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ActivityEntity.QUICK_PRESETS) { (title, cat, mins) ->
                            AssistChip(
                                onClick = { onAddQuickPreset(title, cat, mins) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = activityCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(if (mins > 0) "$title (${mins}m)" else title)
                                },
                                modifier = Modifier.testTag("quick_preset_${title.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            // Status & Category Filters
            item {
                LazyRow(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ActivityStatusFilter.entries) { filter ->
                        FilterChip(
                            selected = uiState.statusFilter == filter,
                            onClick = { onStatusFilterSelected(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("activity_status_${filter.name.lowercase()}")
                        )
                    }

                    items(ActivityEntity.AVAILABLE_CATEGORIES) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("activity_cat_filter_${category.lowercase()}")
                        )
                    }
                }
            }

            if (uiState.filteredActivities.isEmpty()) {
                item {
                    val hasAny = uiState.allActivities.isNotEmpty()
                    ActivitiesEmptyState(
                        isFiltered = hasAny,
                        onAddClick = onOpenAddActivity,
                        onResetFilters = {
                            onStatusFilterSelected(ActivityStatusFilter.ALL)
                            onCategorySelected(null)
                        },
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .padding(vertical = 24.dp)
                    )
                }
            } else {
                items(uiState.filteredActivities, key = { it.id }) { activity ->
                    ActivityItemCard(
                        activity = activity,
                        todayKey = uiState.todayKey,
                        last7Days = uiState.last7Days,
                        imageStorageManager = imageStorageManager,
                        onToggleDone = { onToggleCompletedToday(activity) },
                        onClick = { editingActivity = activity },
                        onPhotoClick = { fileName -> photoToView = fileName },
                        onDelete = { activityPendingDeletion = activity },
                        modifier = Modifier.widthIn(max = 720.dp)
                    )
                }
            }
        }
    }

    if (showActivitySheet || editingActivity != null) {
        AddEditActivitySheet(
            existingActivity = editingActivity,
            imageStorageManager = imageStorageManager,
            onSavePhotoFromUri = onSavePhotoFromUri,
            onDeleteUnusedPhoto = onDeleteUnusedPhoto,
            onSave = { id, title, category, mins, note, photoName, markDone ->
                onSaveActivity(id, title, category, mins, note, photoName, markDone)
                editingActivity = null
                onDismissActivitySheet()
            },
            onDismiss = {
                editingActivity = null
                onDismissActivitySheet()
            }
        )
    }

    photoToView?.let { fileName ->
        FullscreenImageDialog(
            imageNames = listOf(fileName),
            initialIndex = 0,
            imageStorageManager = imageStorageManager,
            onDeleteImage = { photoToView = null },
            onDismiss = { photoToView = null }
        )
    }

    activityPendingDeletion?.let { target ->
        AlertDialog(
            onDismissRequest = { activityPendingDeletion = null },
            title = { Text("Hapus Aktivitas Ini?") },
            text = { Text("Aktivitas \"${target.title}\" beserta riwayat streak-nya akan dihapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteActivity(target)
                        activityPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_activity_button")
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { activityPendingDeletion = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun DailyProgressHeroCard(
    uiState: ActivitiesUiState,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primary, primary.copy(alpha = 0.85f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Hari Ini",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "${(uiState.completionRatio * 100).toInt()}% tercapai",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "${uiState.completedTodayCount} / ${uiState.totalCount} Selesai",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier.testTag("activities_today_progress_text")
                )

                LinearProgressIndicator(
                    progress = { uiState.completionRatio },
                    color = Color(0xFF86EFAC),
                    trackColor = Color.White.copy(alpha = 0.22f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Durasi Aktif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${uiState.completedMinutesToday} menit",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = secondary.copy(alpha = 0.95f),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Streak Terbaik",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${uiState.bestStreak} hari",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItemCard(
    activity: ActivityEntity,
    todayKey: String,
    last7Days: List<Pair<String, String>>,
    imageStorageManager: ImageStorageManager,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doneToday = activity.isCompletedOn(todayKey)
    val streak = remember(activity.completedDatesJson, todayKey) { activity.currentStreak(todayKey) }
    val completedSet = remember(activity.completedDatesJson) { activity.completedDateKeys() }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_card_${activity.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (doneToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (doneToday) 1.5.dp else 1.dp,
            color = if (doneToday) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (doneToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = activityCategoryIcon(activity.category),
                        contentDescription = null,
                        tint = if (doneToday) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (doneToday) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (activity.targetMinutes > 0) {
                                "${activity.category} • ${activity.targetMinutes} mnt"
                            } else {
                                activity.category
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (streak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${streak}h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    if (activity.note.isNotBlank()) {
                        Text(
                            text = activity.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                activity.photoImageName?.let { fileName ->
                    val file = remember(fileName) { imageStorageManager.getImageFile(fileName) }
                    Spacer(modifier = Modifier.width(6.dp))
                    AsyncImage(
                        model = file,
                        contentDescription = "Foto aktivitas",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPhotoClick(fileName) }
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_activity_button_${activity.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus aktivitas",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleDone,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_activity_done_${activity.id}")
                ) {
                    Icon(
                        imageVector = if (doneToday) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.RadioButtonUnchecked
                        },
                        contentDescription = if (doneToday) {
                            "Tandai belum selesai hari ini"
                        } else {
                            "Tandai selesai hari ini"
                        },
                        tint = if (doneToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 7-day completion tracker dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                last7Days.forEach { (dateKey, dayInitial) ->
                    val completed = dateKey in completedSet
                    val isToday = dateKey == todayKey
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dayInitial,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        completed -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (completed) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitiesEmptyState(
    isFiltered: Boolean,
    onAddClick: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsRun,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        Text(
            text = if (isFiltered) "Tidak Ada Aktivitas Cocok" else "Belum Ada Aktivitas Harian",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isFiltered) {
                "Coba pilih filter status atau kategori aktivitas lain."
            } else {
                "Mulai catat olahraga, membaca, minum air, atau rutinitas harian Anda untuk membangun streak!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = if (isFiltered) onResetFilters else onAddClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("empty_activity_action_button")
        ) {
            if (!isFiltered) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = if (isFiltered) "Tampilkan Semua" else "Tambah Aktivitas Pertama",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun activityCategoryIcon(category: String): ImageVector = when (category) {
    "Olahraga" -> Icons.Outlined.DirectionsRun
    "Kesehatan" -> Icons.Outlined.FavoriteBorder
    "Belajar" -> Icons.Outlined.MenuBook
    "Ibadah" -> Icons.Outlined.SelfImprovement
    "Produktivitas" -> Icons.Outlined.WorkOutline
    "Hobi" -> Icons.Outlined.Palette
    else -> Icons.Outlined.AutoAwesome
}
