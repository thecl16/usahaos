package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.ui.MainAppViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.UsahaAmber100
import com.example.ui.theme.UsahaAmber600
import com.example.ui.theme.UsahaBlue100
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaRed100
import com.example.ui.theme.UsahaRed600
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate200
import com.example.ui.theme.UsahaSlate500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainAppViewModel,
    initialTab: Int = 0,
    onNavigateBack: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val products by viewModel.products.collectAsState()
    val variants by viewModel.variants.collectAsState()
    val stockBalancesMap by viewModel.stockBalancesMap.collectAsState()
    val stockMovements by viewModel.stockMovements.collectAsState()

    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Status Stok, 1: Stock Opname, 2: Riwayat Mutasi

    // Quick Action Stock Dialog
    var quickActionProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quickActionMode by remember { mutableStateOf("IN") } // IN, OUT, ADJUST

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("inventory_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = UsahaNavy900)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Stok & Inventaris",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = UsahaBlue50, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = currentBusiness?.name ?: "",
                                    fontSize = 11.sp,
                                    color = UsahaBlue600,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Pantau saldo, stok masuk, stok keluar, dan stock opname",
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaSlate500
                        )
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabTitles = listOf("Saldo Stok", "Stock Opname", "Riwayat Mutasi")
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == idx) UsahaBlue600 else UsahaSlate500,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("inventory_tab_$idx")
                    )
                }
            }

            // Tab Contents
            when (selectedTab) {
                0 -> StockBalancesTab(
                    products = products,
                    stockBalancesMap = stockBalancesMap,
                    onOpenAction = { prod, mode ->
                        quickActionProduct = prod
                        quickActionMode = mode
                    }
                )
                1 -> StockOpnameTab(
                    products = products.filter { it.trackStock && it.isActive },
                    stockBalancesMap = stockBalancesMap,
                    onSubmitOpname = { prod, physical, notes ->
                        viewModel.recordStockOpname(prod.id, null, physical, notes)
                    }
                )
                2 -> StockMovementHistoryTab(
                    movements = stockMovements,
                    products = products
                )
            }
        }
    }

    // Quick Action Dialog: Stock In / Out / Adjust
    if (quickActionProduct != null) {
        val prod = quickActionProduct!!
        val currentStock = stockBalancesMap[prod.id] ?: 0

        StockActionDialog(
            product = prod,
            currentStock = currentStock,
            mode = quickActionMode,
            onDismiss = { quickActionProduct = null },
            onConfirm = { qty, notes ->
                when (quickActionMode) {
                    "IN" -> viewModel.recordStockIn(prod.id, null, qty, notes) {
                        quickActionProduct = null
                    }
                    "OUT" -> viewModel.recordStockOut(prod.id, null, qty, notes) {
                        quickActionProduct = null
                    }
                    "ADJUST" -> viewModel.recordStockAdjustment(prod.id, null, qty, notes) {
                        quickActionProduct = null
                    }
                }
            }
        )
    }
}

@Composable
fun StockBalancesTab(
    products: List<ProductEntity>,
    stockBalancesMap: Map<String, Int>,
    onOpenAction: (ProductEntity, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, LOW, OUT, IN_STOCK

    val trackableProducts = products.filter { it.trackStock }

    val filtered = trackableProducts.filter { prod ->
        val current = stockBalancesMap[prod.id] ?: 0
        val matchQuery = searchQuery.isBlank() ||
                prod.productName.contains(searchQuery, ignoreCase = true) ||
                prod.productCode.contains(searchQuery, ignoreCase = true)

        val matchStatus = when (filterStatus) {
            "OUT" -> current <= 0
            "LOW" -> current in 1..prod.minStock
            "IN_STOCK" -> current > prod.minStock
            else -> true
        }
        matchQuery && matchStatus
    }

    val lowStockCount = trackableProducts.count { (stockBalancesMap[it.id] ?: 0) in 1..it.minStock }
    val outOfStockCount = trackableProducts.count { (stockBalancesMap[it.id] ?: 0) <= 0 }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Summary Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StockSummaryCard("Total Item", "${trackableProducts.size}", UsahaBlue600, UsahaBlue50, Modifier.weight(1f))
            StockSummaryCard("Stok Menipis", "$lowStockCount", UsahaAmber600, UsahaAmber100, Modifier.weight(1f))
            StockSummaryCard("Stok Habis", "$outOfStockCount", UsahaRed600, UsahaRed100, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari produk untuk cek stok...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = UsahaSlate500) },
            modifier = Modifier.fillMaxWidth().testTag("inventory_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val chips = listOf(
                "ALL" to "Semua (${trackableProducts.size})",
                "OUT" to "Habis ($outOfStockCount)",
                "LOW" to "Menipis ($lowStockCount)",
                "IN_STOCK" to "Aman"
            )
            items(chips) { (status, label) ->
                val isSelected = filterStatus == status
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) UsahaBlue600 else UsahaSlate100,
                    modifier = Modifier.clickable { filterStatus = status }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else UsahaNavy900,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "Tidak ada produk stok",
                    description = if (trackableProducts.isEmpty())
                        "Semua produk Anda saat ini berstatus Non-Stok atau belum ada produk."
                    else "Tidak ada produk dengan kriteria filter yang dipilih."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filtered, key = { it.id }) { product ->
                    val stock = stockBalancesMap[product.id] ?: 0
                    StockItemCard(
                        product = product,
                        currentStock = stock,
                        onStockIn = { onOpenAction(product, "IN") },
                        onStockOut = { onOpenAction(product, "OUT") },
                        onAdjust = { onOpenAction(product, "ADJUST") }
                    )
                }
            }
        }
    }
}

@Composable
fun StockItemCard(
    product: ProductEntity,
    currentStock: Int,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onAdjust: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("stock_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.productCode, fontSize = 11.sp, color = UsahaSlate500)
                    Text(product.productName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                    Text("Min. Stok: ${product.minStock} ${product.unit}", fontSize = 11.sp, color = UsahaSlate500)
                }

                // Status Badge
                if (currentStock <= 0) {
                    Surface(color = UsahaRed100, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "HABIS ($currentStock ${product.unit})",
                            color = UsahaRed600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (currentStock <= product.minStock) {
                    Surface(color = UsahaAmber100, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "MENIPIS ($currentStock ${product.unit})",
                            color = UsahaAmber600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(color = UsahaEmerald100, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "AMAN: $currentStock ${product.unit}",
                            color = UsahaEmerald700,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStockIn,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).testTag("stock_in_btn_${product.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = UsahaEmerald700)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stok Masuk", fontSize = 11.sp, color = UsahaEmerald700, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onStockOut,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).testTag("stock_out_btn_${product.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp), tint = UsahaRed600)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stok Keluar", fontSize = 11.sp, color = UsahaRed600, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).testTag("stock_adjust_btn_${product.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = UsahaBlue600)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sesuaikan", fontSize = 11.sp, color = UsahaBlue600, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOpnameTab(
    products: List<ProductEntity>,
    stockBalancesMap: Map<String, Int>,
    onSubmitOpname: (ProductEntity, Int, String) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var physicalCountInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val currentExpectedStock = selectedProduct?.let { stockBalancesMap[it.id] ?: 0 } ?: 0
    val physicalCount = physicalCountInput.toIntOrNull()
    val difference = if (physicalCount != null) physicalCount - currentExpectedStock else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("stock_opname_screen")
    ) {
        // Guidance Box
        Surface(
            color = UsahaBlue50,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null, tint = UsahaBlue600)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Alur Stock Opname UsahaOS", fontWeight = FontWeight.Bold, color = UsahaBlue600, fontSize = 13.sp)
                    Text(
                        "Stok Tercatat → Hitung Fisik → Selisih Otomatis → Konfirmasi Penyesuaian. Setiap perubahan tercatat dalam mutasi stok.",
                        fontSize = 11.sp,
                        color = UsahaNavy900
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Product Selector Dropdown
        Text("Pilih Produk", fontSize = 12.sp, color = UsahaSlate500, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedProduct?.let { "${it.productCode} - ${it.productName}" } ?: "Pilih produk...",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("opname_product_dropdown"),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                products.forEach { prod ->
                    DropdownMenuItem(
                        text = { Text("${prod.productCode} - ${prod.productName}") },
                        onClick = {
                            selectedProduct = prod
                            expandedDropdown = false
                            physicalCountInput = ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-Way Opname Comparison Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Step 1: Expected Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Stok Tercatat di Sistem", fontSize = 12.sp, color = UsahaSlate500)
                        Text(
                            text = "$currentExpectedStock ${selectedProduct?.unit ?: "pcs"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = UsahaNavy900
                        )
                    }
                    Surface(color = UsahaSlate100, shape = RoundedCornerShape(4.dp)) {
                        Text("Buku Stok", fontSize = 10.sp, color = UsahaSlate500, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Step 2: Physical Count Input
                Text("Stok Fisik Sebenarnya", fontSize = 12.sp, color = UsahaSlate500)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = physicalCountInput,
                    onValueChange = { physicalCountInput = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text("Masukkan jumlah fisik hasil hitung") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("opname_physical_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 3: Real-time Difference
                if (physicalCount != null) {
                    val diffColor = if (difference == 0) UsahaEmerald700 else if (difference > 0) UsahaBlue600 else UsahaRed600
                    val diffBg = if (difference == 0) UsahaEmerald100 else if (difference > 0) UsahaBlue50 else UsahaRed100
                    val diffText = if (difference > 0) "+$difference" else "$difference"

                    Surface(color = diffBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hasil Selisih Stok", fontSize = 11.sp, color = diffColor, fontWeight = FontWeight.Bold)
                                Text(
                                    text = when {
                                        difference == 0 -> "Sesuai / Tidak ada selisih"
                                        difference > 0 -> "Surplus fisik (Stok fisik lebih banyak)"
                                        else -> "Minus / Defisit (Stok fisik berkurang)"
                                    },
                                    fontSize = 11.sp,
                                    color = UsahaNavy900
                                )
                            }
                            Text(
                                text = "$diffText ${selectedProduct?.unit ?: "pcs"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = diffColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                Text("Catatan Opname / Alasan Selisih", fontSize = 12.sp, color = UsahaSlate500)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Contoh: Opname akhir bulan, ditemukan barang rusak, dll.") },
                    modifier = Modifier.fillMaxWidth().testTag("opname_notes_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val prod = selectedProduct
                        val phys = physicalCount
                        if (prod != null && phys != null) {
                            onSubmitOpname(prod, phys, notes)
                            physicalCountInput = ""
                            notes = ""
                        }
                    },
                    enabled = selectedProduct != null && physicalCount != null,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("opname_confirm_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Konfirmasi & Sesuaikan Stok", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockMovementHistoryTab(
    movements: List<StockMovementEntity>,
    products: List<ProductEntity>
) {
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val productMap = remember(products) { products.associateBy { it.id } }

    val filteredMovements = movements.filter { m ->
        if (selectedTypeFilter == "ALL") true else m.type == selectedTypeFilter
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Movement Type Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val chips = listOf(
                "ALL" to "Semua Mutasi",
                "IN" to "Masuk (IN)",
                "OUT" to "Keluar (OUT)",
                "SALE" to "Penjualan (SALE)",
                "OPNAME" to "Opname",
                "ADJUSTMENT" to "Penyesuaian"
            )
            items(chips) { (type, label) ->
                val isSelected = selectedTypeFilter == type
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) UsahaBlue600 else UsahaSlate100,
                    modifier = Modifier.clickable { selectedTypeFilter = type }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else UsahaNavy900,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredMovements.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "Belum ada riwayat mutasi stok",
                    description = "Setiap stok masuk, keluar, penjualan, atau opname akan tercatat di sini secara otomatis."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredMovements, key = { it.id }) { movement ->
                    val prod = productMap[movement.productId]
                    val productName = prod?.productName ?: "Produk #${movement.productId.take(6)}"

                    val typeColor = when (movement.type) {
                        "IN" -> UsahaEmerald700
                        "OUT" -> UsahaRed600
                        "SALE" -> UsahaBlue600
                        "OPNAME" -> UsahaAmber600
                        else -> UsahaNavy900
                    }

                    val typeBg = when (movement.type) {
                        "IN" -> UsahaEmerald100
                        "OUT" -> UsahaRed100
                        "SALE" -> UsahaBlue50
                        "OPNAME" -> UsahaAmber100
                        else -> UsahaSlate100
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("movement_card_${movement.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = typeBg, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = movement.type,
                                            color = typeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dateFormat.format(Date(movement.createdAt)),
                                        fontSize = 11.sp,
                                        color = UsahaSlate500
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(productName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                                if (movement.notes.isNotBlank()) {
                                    Text(movement.notes, fontSize = 11.sp, color = UsahaSlate500)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val qtyLabel = if (movement.quantity > 0) "+${movement.quantity}" else "${movement.quantity}"
                                Text(
                                    text = qtyLabel,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = typeColor
                                )
                                Text(
                                    text = "${movement.balanceBefore} → ${movement.balanceAfter}",
                                    fontSize = 11.sp,
                                    color = UsahaSlate500
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
fun StockActionDialog(
    product: ProductEntity,
    currentStock: Int,
    mode: String, // IN, OUT, ADJUST
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, notes: String) -> Unit
) {
    val title = when (mode) {
        "IN" -> "Stok Masuk"
        "OUT" -> "Stok Keluar"
        else -> "Penyesuaian Stok"
    }

    var quantityInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("stock_action_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = UsahaNavy900)
                        Text("${product.productCode} - ${product.productName}", fontSize = 12.sp, color = UsahaSlate500)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(color = UsahaSlate100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stok Saat Ini:", fontSize = 12.sp, color = UsahaSlate500)
                        Text("$currentStock ${product.unit}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (mode == "ADJUST") "Stok Baru Sebenarnya" else "Jumlah Barang",
                    fontSize = 12.sp,
                    color = UsahaSlate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("stock_qty_input"),
                    singleLine = true,
                    placeholder = { Text("Contoh: 10") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Catatan / Keterangan", fontSize = 12.sp, color = UsahaSlate500)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().testTag("stock_notes_input"),
                    placeholder = { Text("Contoh: Pembelian supplier, retur, barang rusak...") }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val qty = quantityInput.toIntOrNull()
                            if (qty != null && qty > 0) {
                                onConfirm(qty, notes)
                            }
                        },
                        enabled = quantityInput.toIntOrNull() != null && quantityInput.toInt() > 0,
                        modifier = Modifier.weight(1f).testTag("stock_action_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun StockSummaryCard(
    title: String,
    value: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textColor)
        }
    }
}
