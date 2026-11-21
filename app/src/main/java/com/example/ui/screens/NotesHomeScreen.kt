package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ImageStorageManager
import com.example.data.NoteEntity
import com.example.ui.HomeUiState
import com.example.ui.NotesFilter
import com.example.ui.components.NoteCard
import com.example.ui.theme.BrandTitleTextStyle

@Composable
fun NotesHomeScreen(
    uiState: HomeUiState,
    imageStorageManager: ImageStorageManager,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (NotesFilter) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onToggleGridView: () -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNewNote: () -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onOpenBackupCenter: () -> Unit,
    onOpenThemeCustomizer: () -> Unit,
    onDismissLocalNotice: () -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notePendingDeletion by remember { mutableStateOf<NoteEntity?>(null) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onClearStatusMessage()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
            // Top Editorial Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = BrandTitleTextStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.totalNotesCount} catatan • ${uiState.totalImagesCount} foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onOpenBackupCenter,
                        shape = CircleShape,
                        color = if (uiState.pendingBackupCount > 0) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("open_backup_center_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Backup,
                                contentDescription = "Cadangan & Status Cloud",
                                tint = if (uiState.pendingBackupCount > 0) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (uiState.pendingBackupCount > 0) {
                                    "Cadangkan (${uiState.pendingBackupCount})"
                                } else {
                                    "Cadangan"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.pendingBackupCount > 0) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenThemeCustomizer,
                        modifier = Modifier.testTag("open_theme_customizer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Ubah tema dan latar belakang",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = onToggleGridView,
                        modifier = Modifier.testTag("toggle_layout_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) {
                                Icons.Outlined.ViewAgenda
                            } else {
                                Icons.Outlined.GridView
                            },
                            contentDescription = if (uiState.isGridView) {
                                "Ubah ke tampilan daftar"
                            } else {
                                "Ubah ke tampilan grid"
                            },
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Honest Local-Only / Backup Banner
            AnimatedVisibility(visible = uiState.showLocalNoticeBanner) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Penyimpanan Lokal Aktif",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sinkronisasi Firebase tidak diaktifkan. Catatan tersimpan lokal; ekspor ZIP untuk cadangan ke Drive.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = onOpenBackupCenter,
                            modifier = Modifier.testTag("banner_backup_action")
                        ) {
                            Text("Cadangkan")
                        }
                        IconButton(
                            onClick = onDismissLocalNotice,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("dismiss_local_banner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup pemberitahuan",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_notes_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Hapus pencarian"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("search_notes_input")
            )

            // Filter & Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotesFilter.entries) { filter ->
                    val selected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }

                items(NoteEntity.AVAILABLE_CATEGORIES) { category ->
                    val selected = uiState.selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${category.lowercase()}")
                    )
                }
            }

            // Notes Content or Empty State
            if (uiState.filteredNotes.isEmpty()) {
                val isFilteredEmpty = uiState.allNotes.isNotEmpty()
                EmptyNotesState(
                    isSearchOrFilterResult = isFilteredEmpty,
                    onCreateNewNote = onCreateNewNote,
                    onResetFilters = {
                        onSearchQueryChange("")
                        onFilterSelected(NotesFilter.ALL)
                        onCategorySelected(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val columns = when {
                        !uiState.isGridView -> 1
                        maxWidth >= 840.dp -> 4
                        maxWidth >= 600.dp -> 3
                        else -> 2
                    }

                    if (columns == 1) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    imageStorageManager = imageStorageManager,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { notePendingDeletion = note },
                                    modifier = Modifier.widthIn(max = 700.dp)
                                )
                            }
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(columns),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 4.dp,
                                bottom = 96.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    imageStorageManager = imageStorageManager,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { notePendingDeletion = note }
                                )
                            }
                        }
                    }
                }
            }
        }

    notePendingDeletion?.let { target ->
        AlertDialog(
            onDismissRequest = { notePendingDeletion = null },
            title = { Text(stringResource(R.string.delete_note_confirm_title)) },
            text = { Text(stringResource(R.string.delete_note_confirm_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target)
                        notePendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_note_button")
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { notePendingDeletion = null },
                    modifier = Modifier.testTag("cancel_delete_note_button")
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyNotesState(
    isSearchOrFilterResult: Boolean,
    onCreateNewNote: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            if (!isSearchOrFilterResult) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_notes),
                    contentDescription = "Ilustrasi buku catatan kosong",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 220.dp, height = 165.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = if (isSearchOrFilterResult) {
                    stringResource(R.string.empty_search_title)
                } else {
                    stringResource(R.string.empty_notes_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearchOrFilterResult) {
                    stringResource(R.string.empty_search_subtitle)
                } else {
                    stringResource(R.string.empty_notes_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (isSearchOrFilterResult) {
                Button(
                    onClick = onResetFilters,
                    modifier = Modifier.testTag("reset_filters_button")
                ) {
                    Text("Tampilkan Semua Catatan")
                }
            } else {
                Button(
                    onClick = onCreateNewNote,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_create_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buat Catatan Pertama",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
