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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.repository.VariantInput
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate300
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    categories: List<CategoryEntity>,
    productToEdit: ProductEntity? = null,
    initialVariants: List<ProductVariantEntity> = emptyList(),
    onRequestNextCode: suspend (prefix: String) -> String,
    onGenerateInternalBarcode: suspend () -> String,
    onDismiss: () -> Unit,
    onSave: (
        categoryId: String,
        name: String,
        sku: String?,
        barcode: String?,
        unit: String,
        purchasePrice: Long,
        sellingPrice: Long,
        minStock: Int,
        trackStock: Boolean,
        description: String,
        variants: List<VariantInput>
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember {
        mutableStateOf(
            if (productToEdit != null) {
                categories.firstOrNull { it.id == productToEdit.categoryId } ?: categories.firstOrNull()
            } else {
                categories.firstOrNull()
            }
        )
    }

    var categoryExpanded by remember { mutableStateOf(false) }

    var productCode by remember { mutableStateOf(productToEdit?.productCode ?: "") }
    var productName by remember { mutableStateOf(productToEdit?.productName ?: "") }
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var isInternalBarcode by remember { mutableStateOf(productToEdit?.barcode?.startsWith("200") == true) }

    var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }
    var unitExpanded by remember { mutableStateOf(false) }
    val standardUnits = listOf("pcs", "porsi", "cup", "box", "kg", "pack", "meter", "botol", "jam")

    var purchasePriceStr by remember { mutableStateOf(productToEdit?.purchasePrice?.toString() ?: "0") }
    var sellingPriceStr by remember { mutableStateOf(productToEdit?.sellingPrice?.toString() ?: "0") }
    var minStockStr by remember { mutableStateOf(productToEdit?.minStock?.toString() ?: "0") }
    var trackStock by remember { mutableStateOf(productToEdit?.trackStock ?: true) }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }

    val variants = remember {
        mutableStateListOf<VariantInput>().apply {
            addAll(initialVariants.map {
                VariantInput(
                    id = it.id,
                    variantName = it.variantName,
                    sku = it.sku,
                    barcode = it.barcode,
                    purchasePrice = it.purchasePrice,
                    sellingPrice = it.sellingPrice
                )
            })
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // When category changes and adding a new product, automatically request the next product code!
    LaunchedEffect(selectedCategory) {
        if (productToEdit == null && selectedCategory != null) {
            val code = onRequestNextCode(selectedCategory!!.prefix)
            productCode = code
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("product_dialog"),
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
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Produk",
                                tint = UsahaNavy900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (productToEdit == null) "Tambah Produk Baru" else "Edit Produk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("product_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = UsahaSlate500
                        )
                    }
                }

                Text(
                    text = "Lengkapi data produk. Kode produk internal akan dibuatkan secara otomatis oleh sistem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Category Selector
                Text(
                    text = "1. Kategori Produk *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.name} [Prefix: ${it.prefix}]" } ?: "Pilih Kategori",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("product_category_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(cat.name, fontWeight = FontWeight.SemiBold)
                                        Surface(
                                            color = UsahaBlue50,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = cat.prefix,
                                                color = UsahaBlue600,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Automatic Product Code Display (Read-Only)
                Text(
                    text = "2. Kode Produk Internal (Otomatis & Read-Only)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = UsahaSlate100,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Read-only",
                            tint = UsahaSlate500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = productCode.ifEmpty { "Pilih kategori untuk generate kode..." },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (productCode.isNotEmpty()) UsahaNavy900 else UsahaSlate400,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Dihasilkan otomatis oleh sistem (tidak dapat diubah manual).",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaSlate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Product Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nama Produk *") },
                    placeholder = { Text("Contoh: Kopi Susu Aren, Kaos Polos Hitam") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. SKU & Barcode
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU (Opsional)") },
                        placeholder = { Text("BEV-KOP-S") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_sku_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Satuan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("product_unit_input")
                        )

                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            standardUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barcode input with Generate Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = {
                            barcode = it
                            isInternalBarcode = false
                        },
                        label = { Text("Barcode / EAN (Opsional)") },
                        placeholder = { Text("Scan atau masukkan barcode...") },
                        leadingIcon = {
                            Icon(Icons.Default.QrCode, contentDescription = "Barcode", tint = UsahaSlate500)
                        },
                        trailingIcon = {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val newBarcode = onGenerateInternalBarcode()
                                        barcode = newBarcode
                                        isInternalBarcode = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("product_generate_barcode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generate",
                                    modifier = Modifier.size(14.dp),
                                    tint = UsahaBlue600
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate", fontSize = 11.sp, color = UsahaBlue600)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_barcode_input")
                    )

                    if (isInternalBarcode && barcode.isNotBlank()) {
                        Surface(
                            color = UsahaEmerald100,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "✓ Internal Barcode (Bukan GS1 resmi)",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaEmerald700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Prices
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) purchasePriceStr = it },
                        label = { Text("Harga Beli / HPP") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_purchase_price_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) sellingPriceStr = it },
                        label = { Text("Harga Jual *") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_selling_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Stock Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) minStockStr = it },
                        label = { Text("Minimum Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_min_stock_input")
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kelola Stok",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = UsahaNavy900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (trackStock) "Aktif" else "Nonaktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = UsahaSlate500,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = trackStock,
                                onCheckedChange = { trackStock = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = UsahaBlue600
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Produk (Opsional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_description_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 7. Product Variants Section
                Surface(
                    color = UsahaSlate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Varian Produk (${variants.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = UsahaNavy900
                                )
                                Text(
                                    text = "Ukuran, rasa, warna, atau tingkatan harga berbeda",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UsahaSlate500
                                )
                            }

                            Button(
                                onClick = {
                                    val defSell = sellingPriceStr.toLongOrNull() ?: 0L
                                    val defBuy = purchasePriceStr.toLongOrNull() ?: 0L
                                    variants.add(
                                        VariantInput(
                                            variantName = "",
                                            sku = "",
                                            barcode = "",
                                            purchasePrice = defBuy,
                                            sellingPrice = defSell
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                                modifier = Modifier.testTag("product_add_variant_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Varian", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Varian", fontSize = 12.sp)
                            }
                        }

                        if (variants.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            variants.forEachIndexed { idx, v ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, UsahaSlate300),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = v.variantName,
                                                onValueChange = { newName ->
                                                    variants[idx] = v.copy(variantName = newName)
                                                },
                                                label = { Text("Nama Varian *") },
                                                placeholder = { Text("Regular, Large, XL, Hitam") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { variants.removeAt(idx) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Varian",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = v.sku ?: "",
                                                onValueChange = { newSku ->
                                                    variants[idx] = v.copy(sku = newSku)
                                                },
                                                label = { Text("SKU Varian") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = v.sellingPrice.toString(),
                                                onValueChange = { newSell ->
                                                    val amount = newSell.filter { it.isDigit() }.toLongOrNull() ?: 0L
                                                    variants[idx] = v.copy(sellingPrice = amount)
                                                },
                                                label = { Text("Harga Jual") },
                                                prefix = { Text("Rp ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val cat = selectedCategory
                        val nameTrimmed = productName.trim()
                        val buyPrice = purchasePriceStr.toLongOrNull() ?: 0L
                        val sellPrice = sellingPriceStr.toLongOrNull() ?: 0L
                        val minStk = minStockStr.toIntOrNull() ?: 0

                        if (cat == null) {
                            errorMessage = "Silakan pilih kategori terlebih dahulu"
                        } else if (nameTrimmed.isBlank()) {
                            errorMessage = "Nama produk wajib diisi"
                        } else if (sellPrice <= 0L && variants.isEmpty()) {
                            errorMessage = "Harga jual harus lebih besar dari 0"
                        } else if (variants.any { it.variantName.isBlank() }) {
                            errorMessage = "Nama pada semua varian wajib diisi"
                        } else {
                            errorMessage = null
                            onSave(
                                cat.id,
                                nameTrimmed,
                                sku.trim().ifBlank { null },
                                barcode.trim().ifBlank { null },
                                unit.trim().ifBlank { "pcs" },
                                buyPrice,
                                sellPrice,
                                minStk,
                                trackStock,
                                description.trim(),
                                variants.toList()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("product_save_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (productToEdit == null) "Simpan Produk" else "Perbarui Produk",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
