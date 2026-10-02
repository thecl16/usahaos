package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CategoryEntity
import com.example.domain.model.BusinessType
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500

@Composable
fun CategoryDialog(
    categoryToEdit: CategoryEntity? = null,
    businessType: BusinessType,
    onDismiss: () -> Unit,
    onSave: (name: String, prefix: String, businessType: String, description: String, isActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var prefix by remember { mutableStateOf(categoryToEdit?.prefix ?: "") }
    var selectedBusinessType by remember { mutableStateOf(categoryToEdit?.businessType ?: businessType.code) }
    var description by remember { mutableStateOf(categoryToEdit?.description ?: "") }
    var isActive by remember { mutableStateOf(categoryToEdit?.isActive ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cleanPrefix = prefix.trim().uppercase()
    val previewCode = if (cleanPrefix.isNotBlank()) "${cleanPrefix}00000001" else "---00000001"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("category_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(UsahaSlate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Kategori",
                                tint = UsahaNavy900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (categoryToEdit == null) "Tambah Kategori Baru" else "Edit Kategori",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("category_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = UsahaSlate500
                        )
                    }
                }

                Text(
                    text = "Prefix kategori digunakan untuk menghasilkan nomor kode produk otomatis secara berurutan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(16.dp))

                // Category Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori *") },
                    placeholder = { Text("Contoh: Minuman, Makanan, Pakaian") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Code Prefix
                OutlinedTextField(
                    value = prefix,
                    onValueChange = {
                        if (it.length <= 5) {
                            prefix = it.uppercase()
                        }
                    },
                    label = { Text("Prefix Kode Produk (2-5 Huruf) *") },
                    placeholder = { Text("Contoh: BEV, FOD, SNK, FAS") },
                    supportingText = {
                        Text("Huruf kapital unik untuk usaha ini. Menghasilkan kode berurutan.")
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_prefix_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Auto Product Code Preview Banner
                Surface(
                    color = UsahaBlue50,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Preview",
                            tint = UsahaBlue600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pratinjau Kode Produk Otomatis:",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaSlate500
                            )
                            Text(
                                text = previewCode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = UsahaBlue600,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Business Type Selection
                Text(
                    text = "Tipe Usaha Terkait",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    BusinessType.entries.forEach { type ->
                        val isSelected = selectedBusinessType == type.code
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) UsahaBlue600 else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBusinessType = type.code },
                            color = if (isSelected) UsahaBlue50 else Color.White
                        ) {
                            Text(
                                text = type.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) UsahaBlue600 else UsahaSlate500,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_description_input")
                )

                if (categoryToEdit != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status Kategori Aktif",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = UsahaNavy900
                        )
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = UsahaBlue600
                            )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val cleanPfx = prefix.trim().uppercase()
                        if (name.isBlank()) {
                            errorMessage = "Nama kategori wajib diisi"
                        } else if (cleanPfx.length < 2 || cleanPfx.length > 5) {
                            errorMessage = "Prefix harus terdiri dari 2 hingga 5 karakter (contoh: BEV, FOD)"
                        } else {
                            errorMessage = null
                            onSave(name.trim(), cleanPfx, selectedBusinessType, description.trim(), isActive)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("category_save_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (categoryToEdit == null) "Simpan Kategori" else "Perbarui Kategori",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
