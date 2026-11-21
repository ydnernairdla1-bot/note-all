package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.ImageStorageManager
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.utils.formatRupiah
import com.example.ui.utils.formatTransactionDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: TransactionEntity?,
    imageStorageManager: ImageStorageManager,
    onSaveReceiptFromUri: suspend (Uri) -> String?,
    onDeleteUnusedReceipt: (String) -> Unit,
    onSave: (
        existingId: String?,
        type: TransactionType,
        amount: Long,
        title: String,
        category: String,
        note: String,
        receiptImageName: String?,
        transactionDate: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var selectedType by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.transactionType ?: TransactionType.EXPENSE)
    }
    var amountText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.amount?.toString() ?: "")
    }
    var title by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.title ?: "")
    }
    var category by remember(existingTransaction) {
        mutableStateOf(
            existingTransaction?.category
                ?: TransactionEntity.EXPENSE_CATEGORIES.first()
        )
    }
    var note by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.note ?: "")
    }
    var receiptImageName by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.receiptImageName)
    }
    var newlyUploadedReceipt by remember { mutableStateOf<String?>(null) }
    var transactionDate by remember(existingTransaction) {
        mutableLongStateOf(existingTransaction?.transactionDate ?: System.currentTimeMillis())
    }
    var isSavingReceipt by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val availableCategories = if (selectedType == TransactionType.EXPENSE) {
        TransactionEntity.EXPENSE_CATEGORIES
    } else {
        TransactionEntity.INCOME_CATEGORIES
    }

    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    isSavingReceipt = true
                    val savedName = onSaveReceiptFromUri(uri)
                    isSavingReceipt = false
                    if (savedName != null) {
                        newlyUploadedReceipt?.let { oldTemp ->
                            if (oldTemp != existingTransaction?.receiptImageName) {
                                onDeleteUnusedReceipt(oldTemp)
                            }
                        }
                        newlyUploadedReceipt = savedName
                        receiptImageName = savedName
                    }
                }
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            newlyUploadedReceipt?.let { tempName ->
                if (tempName != existingTransaction?.receiptImageName) {
                    onDeleteUnusedReceipt(tempName)
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
                text = if (existingTransaction == null) "Catat Transaksi" else "Ubah Transaksi",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Type Toggle (Pengeluaran vs Pemasukan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TransactionType.entries.forEach { txType ->
                    val isSelected = selectedType == txType
                    val containerColor = when {
                        !isSelected -> MaterialTheme.colorScheme.surfaceContainer
                        txType == TransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                    val contentColor = when {
                        !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
                        txType == TransactionType.EXPENSE -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    Surface(
                        onClick = {
                            selectedType = txType
                            val validCats = if (txType == TransactionType.EXPENSE) {
                                TransactionEntity.EXPENSE_CATEGORIES
                            } else {
                                TransactionEntity.INCOME_CATEGORIES
                            }
                            if (category !in validCats) {
                                category = validCats.first()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = containerColor,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) contentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tx_type_${txType.key.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (txType == TransactionType.EXPENSE) {
                                    Icons.Filled.ArrowUpward
                                } else {
                                    Icons.Filled.ArrowDownward
                                },
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = txType.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // Amount Input
            val parsedAmount = amountText.toLongOrNull() ?: 0L
            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    val digitsOnly = raw.filter { it.isDigit() }.take(12)
                    amountText = digitsOnly
                    validationError = null
                },
                label = { Text("Nominal (Rp)") },
                prefix = { Text("Rp ") },
                supportingText = {
                    if (parsedAmount > 0L) {
                        Text("Terbaca: ${formatRupiah(parsedAmount)}")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input")
            )

            // Quick Nominal Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(10_000L to "+10rb", 50_000L to "+50rb", 100_000L to "+100rb").forEach { (delta, label) ->
                    AssistChip(
                        onClick = {
                            val updated = (amountText.toLongOrNull() ?: 0L) + delta
                            amountText = updated.toString()
                            validationError = null
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("quick_amount_${delta}")
                    )
                }
            }

            // Title / Description
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Judul / Keterangan") },
                placeholder = {
                    Text(
                        if (selectedType == TransactionType.EXPENSE) {
                            "Contoh: Belanja mingguan, Kopi pagi"
                        } else {
                            "Contoh: Gaji bulanan, Pembayaran proyek"
                        }
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input")
            )

            // Category Chips
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
                    items(availableCategories) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("tx_category_${item.lowercase()}")
                        )
                    }
                }
            }

            // Date & Optional Receipt Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tx_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatTransactionDate(transactionDate))
                }

                OutlinedButton(
                    onClick = {
                        receiptPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isSavingReceipt,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tx_attach_receipt_button")
                ) {
                    if (isSavingReceipt) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (receiptImageName == null) "Foto Struk" else "Ganti Struk")
                }
            }

            receiptImageName?.let { fileName ->
                val file = remember(fileName) { imageStorageManager.getImageFile(fileName) }
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 96.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Foto struk transaksi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        onClick = {
                            if (fileName == newlyUploadedReceipt && fileName != existingTransaction?.receiptImageName) {
                                onDeleteUnusedReceipt(fileName)
                                newlyUploadedReceipt = null
                            }
                            receiptImageName = null
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(26.dp)
                            .testTag("tx_remove_receipt_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Hapus foto struk",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Optional Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan tambahan (opsional)") },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_note_input")
            )

            validationError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0L) {
                        validationError = "Masukkan nominal lebih dari Rp 0"
                        return@Button
                    }
                    val finalTitle = title.trim().ifBlank { category }
                    onSave(
                        existingTransaction?.id,
                        selectedType,
                        amount,
                        finalTitle,
                        category,
                        note,
                        receiptImageName,
                        transactionDate
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.EXPENSE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("tx_save_button")
            ) {
                Text(
                    text = if (existingTransaction == null) {
                        "Simpan ${selectedType.label}"
                    } else {
                        "Simpan Perubahan"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { transactionDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
