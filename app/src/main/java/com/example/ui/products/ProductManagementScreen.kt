package com.example.ui.products

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.repository.VariantInput
import com.example.domain.model.BusinessType
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.UsahaBlue100
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald600
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaRed100
import com.example.ui.theme.UsahaRed600
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate300
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductManagementScreen(
    initialSubTab: String = "all",
    business: BusinessEntity?,
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    allVariants: List<ProductVariantEntity>,
    onRequestNextCode: suspend (prefix: String) -> String,
    onGenerateInternalBarcode: suspend () -> String,
    onGetVariantsForProduct: suspend (productId: String) -> List<ProductVariantEntity>,
    onCreateCategory: (name: String, prefix: String, businessType: String, description: String) -> Unit,
    onUpdateCategory: (id: String, name: String, prefix: String, businessType: String, description: String, isActive: Boolean) -> Unit,
    onToggleCategoryStatus: (categoryId: String, isActive: Boolean) -> Unit,
    onCreateProduct: (
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
    ) -> Unit,
    onUpdateProduct: (product: ProductEntity, variants: List<VariantInput>) -> Unit,
    onToggleProductStatus: (productId: String, isActive: Boolean) -> Unit,
    onToggleVariantStatus: (variantId: String, isActive: Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember {
        mutableIntStateOf(
            when (initialSubTab) {
                "categories" -> 1
                "variants" -> 2
                else -> 0
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<Boolean?>(null) } // null: Semua, true: Aktif, false: Nonaktif

    // Dialog states
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    var showProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToEditVariants by remember { mutableStateOf<List<ProductVariantEntity>>(emptyList()) }

    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedProductDetailVariants by remember { mutableStateOf<List<ProductVariantEntity>>(emptyList()) }

    val businessType = business?.let { BusinessType.fromCode(it.type) } ?: BusinessType.RETAIL
    val rupiahFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Filtered Products
    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter, selectedStatusFilter) {
        products.filter { p ->
            val matchesSearch = searchQuery.isBlank() ||
                p.productName.contains(searchQuery, ignoreCase = true) ||
                p.productCode.contains(searchQuery, ignoreCase = true) ||
                (p.sku?.contains(searchQuery, ignoreCase = true) == true) ||
                (p.barcode?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCategory = selectedCategoryFilter == null || p.categoryId == selectedCategoryFilter
            val matchesStatus = selectedStatusFilter == null || p.isActive == selectedStatusFilter

            matchesSearch && matchesCategory && matchesStatus
        }
    }

    // Filtered Categories
    val filteredCategories = remember(categories, searchQuery) {
        categories.filter { c ->
            searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.prefix.contains(searchQuery, ignoreCase = true)
        }
    }

    // Filtered Variants
    val filteredVariants = remember(allVariants, searchQuery) {
        allVariants.filter { v ->
            searchQuery.isBlank() ||
                v.variantName.contains(searchQuery, ignoreCase = true) ||
                (v.sku?.contains(searchQuery, ignoreCase = true) == true) ||
                (v.barcode?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("product_management_screen")
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("products_screen_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali ke Dashboard",
                    tint = UsahaNavy900
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Katalog Produk & Inventaris",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "Kelola kategori, generate kode produk otomatis, SKU, dan varian",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500
                )
            }

            // Quick Add Button
            Button(
                onClick = {
                    if (selectedTab == 1) {
                        categoryToEdit = null
                        showCategoryDialog = true
                    } else {
                        if (categories.isEmpty()) {
                            // If no category exists, open category dialog first
                            categoryToEdit = null
                            showCategoryDialog = true
                        } else {
                            productToEdit = null
                            productToEditVariants = emptyList()
                            showProductDialog = true
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                modifier = Modifier.testTag("products_header_add_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedTab == 1) "Kategori" else "Tambah Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Row: "Semua Produk", "Kategori", "Varian"
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = UsahaSlate100,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            indicator = {}
        ) {
            listOf(
                "Semua Produk (${products.size})" to 0,
                "Kategori (${categories.size})" to 1,
                "Varian (${allVariants.size})" to 2
            ).forEach { (label, idx) ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == idx) UsahaBlue600 else UsahaSlate500,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .background(
                            if (selectedTab == idx) Color.White else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .testTag("product_tab_$idx")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = when (selectedTab) {
                        1 -> "Cari kategori atau prefix..."
                        2 -> "Cari nama varian, SKU, atau barcode..."
                        else -> "Cari nama produk, kode produk, SKU, barcode..."
                    }
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = UsahaSlate500)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Bersihkan", tint = UsahaSlate500)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("product_search_bar")
        )

        // Category Filter Chips (Shown when in "Semua Produk" tab)
        if (selectedTab == 0 && categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                item {
                    val isAll = selectedCategoryFilter == null
                    Surface(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedCategoryFilter = null }
                            .testTag("cat_filter_all"),
                        color = if (isAll) UsahaBlue600 else UsahaSlate100
                    ) {
                        Text(
                            text = "Semua Kategori",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAll) Color.White else UsahaNavy900,
                            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat.id
                    Surface(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedCategoryFilter = if (isSelected) null else cat.id }
                            .testTag("cat_filter_${cat.id}"),
                        color = if (isSelected) UsahaBlue600 else UsahaSlate100
                    ) {
                        Text(
                            text = "${cat.name} (${cat.prefix})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else UsahaNavy900,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TAB CONTENT
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                // Tab 0: Semua Produk
                0 -> {
                    if (products.isEmpty()) {
                        // Empty State Requirement
                        EmptyStateView(
                            icon = Icons.Default.Inventory2,
                            title = "Belum Ada Produk",
                            description = "Tambahkan produk pertama untuk mulai mengelola produk dan stok usaha Anda.",
                            primaryButtonText = "+ Tambah Produk",
                            onPrimaryClick = {
                                if (categories.isEmpty()) {
                                    showCategoryDialog = true
                                } else {
                                    productToEdit = null
                                    productToEditVariants = emptyList()
                                    showProductDialog = true
                                }
                            },
                            testTag = "products_empty_state"
                        )
                    } else if (filteredProducts.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.FilterList,
                            title = "Produk Tidak Ditemukan",
                            description = "Tidak ada produk yang cocok dengan pencarian atau filter yang dipilih.",
                            secondaryButtonText = "Reset Filter",
                            onSecondaryClick = {
                                searchQuery = ""
                                selectedCategoryFilter = null
                                selectedStatusFilter = null
                            },
                            testTag = "products_filter_empty"
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredProducts) { product ->
                                val category = categories.firstOrNull { it.id == product.categoryId }
                                val variantCount = allVariants.count { it.productId == product.id }

                                ProductListItemCard(
                                    product = product,
                                    category = category,
                                    variantCount = variantCount,
                                    currencyFormatter = rupiahFormatter,
                                    onClick = {
                                        coroutineScope.launch {
                                            selectedProductDetailVariants = onGetVariantsForProduct(product.id)
                                            selectedProductForDetail = product
                                        }
                                    },
                                    onEdit = {
                                        coroutineScope.launch {
                                            productToEditVariants = onGetVariantsForProduct(product.id)
                                            productToEdit = product
                                            showProductDialog = true
                                        }
                                    },
                                    onToggleStatus = {
                                        onToggleProductStatus(product.id, !product.isActive)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Tab 1: Kategori
                1 -> {
                    if (categories.isEmpty()) {
                        // Empty State Requirement
                        EmptyStateView(
                            icon = Icons.Default.Category,
                            title = "Belum Ada Kategori",
                            description = "Buat kategori usaha pertama untuk menentukan prefix kode produk otomatis (contoh: BEV, FOD, SNK).",
                            primaryButtonText = "+ Tambah Kategori",
                            onPrimaryClick = {
                                categoryToEdit = null
                                showCategoryDialog = true
                            },
                            testTag = "categories_empty_state"
                        )
                    } else if (filteredCategories.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Search,
                            title = "Kategori Tidak Ditemukan",
                            description = "Tidak ada kategori yang cocok dengan '$searchQuery'.",
                            testTag = "categories_search_empty"
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredCategories) { cat ->
                                val productCount = products.count { it.categoryId == cat.id }

                                CategoryListItemCard(
                                    category = cat,
                                    productCount = productCount,
                                    onEdit = {
                                        categoryToEdit = cat
                                        showCategoryDialog = true
                                    },
                                    onToggleStatus = {
                                        onToggleCategoryStatus(cat.id, !cat.isActive)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Tab 2: Varian
                2 -> {
                    if (allVariants.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Inventory2,
                            title = "Belum Ada Varian Produk",
                            description = "Varian produk (ukuran, warna, rasa, dsb.) dapat ditambahkan langsung saat membuat atau mengedit produk.",
                            primaryButtonText = "+ Buat Produk dengan Varian",
                            onPrimaryClick = {
                                if (categories.isEmpty()) {
                                    showCategoryDialog = true
                                } else {
                                    productToEdit = null
                                    productToEditVariants = emptyList()
                                    showProductDialog = true
                                }
                            },
                            testTag = "variants_empty_state"
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredVariants) { variant ->
                                val parentProduct = products.firstOrNull { it.id == variant.productId }

                                VariantListItemCard(
                                    variant = variant,
                                    parentProduct = parentProduct,
                                    currencyFormatter = rupiahFormatter,
                                    onToggleStatus = {
                                        onToggleVariantStatus(variant.id, !variant.isActive)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Dialog
    if (showCategoryDialog) {
        CategoryDialog(
            categoryToEdit = categoryToEdit,
            businessType = businessType,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, prefix, bType, desc, isActive ->
                showCategoryDialog = false
                if (categoryToEdit == null) {
                    onCreateCategory(name, prefix, bType, desc)
                } else {
                    onUpdateCategory(categoryToEdit!!.id, name, prefix, bType, desc, isActive)
                }
            }
        )
    }

    // Product Dialog
    if (showProductDialog) {
        ProductDialog(
            categories = categories,
            productToEdit = productToEdit,
            initialVariants = productToEditVariants,
            onRequestNextCode = onRequestNextCode,
            onGenerateInternalBarcode = onGenerateInternalBarcode,
            onDismiss = { showProductDialog = false },
            onSave = { catId, name, sku, barcode, unit, buyPrice, sellPrice, minStk, track, desc, vList ->
                showProductDialog = false
                if (productToEdit == null) {
                    onCreateProduct(catId, name, sku, barcode, unit, buyPrice, sellPrice, minStk, track, desc, vList)
                } else {
                    val updated = productToEdit!!.copy(
                        categoryId = catId,
                        productName = name,
                        sku = sku,
                        barcode = barcode,
                        unit = unit,
                        purchasePrice = buyPrice,
                        sellingPrice = sellPrice,
                        minStock = minStk,
                        trackStock = track,
                        description = desc
                    )
                    onUpdateProduct(updated, vList)
                }
            }
        )
    }

    // Product Detail Dialog
    selectedProductForDetail?.let { prod ->
        val cat = categories.firstOrNull { it.id == prod.categoryId }
        ProductDetailDialog(
            product = prod,
            category = cat,
            variants = selectedProductDetailVariants,
            onEdit = {
                selectedProductForDetail = null
                productToEdit = prod
                productToEditVariants = selectedProductDetailVariants
                showProductDialog = true
            },
            onToggleActive = {
                onToggleProductStatus(prod.id, !prod.isActive)
                selectedProductForDetail = prod.copy(isActive = !prod.isActive)
            },
            onDismiss = { selectedProductForDetail = null }
        )
    }
}

@Composable
fun ProductListItemCard(
    product: ProductEntity,
    category: CategoryEntity?,
    variantCount: Int,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("product_item_${product.productCode}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Product Code Pill (Read-only badge)
                Surface(
                    color = UsahaSlate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = product.productCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UsahaNavy900,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    if (category != null) {
                        Surface(
                            color = UsahaBlue50,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaBlue600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Active Status Dot
                    Surface(
                        color = if (product.isActive) UsahaEmerald100 else UsahaRed100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (product.isActive) "Aktif" else "Nonaktif",
                            color = if (product.isActive) UsahaEmerald700 else UsahaRed600,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pricing & SKU summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencyFormatter.format(product.sellingPrice),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = UsahaBlue600
                    )
                    Text(
                        text = " / ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = UsahaSlate500
                    )
                }

                if (!product.sku.isNullOrBlank()) {
                    Text(
                        text = "SKU: ${product.sku}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UsahaSlate500
                    )
                }
            }

            // Extra details footer
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (variantCount > 0) {
                        Surface(
                            color = UsahaSlate100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$variantCount Varian",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaSlate500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (!product.barcode.isNullOrBlank()) {
                        Text(
                            text = "Barcode: ${product.barcode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaSlate400
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Produk",
                        tint = UsahaNavy900,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryListItemCard(
    category: CategoryEntity,
    productCount: Int,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_item_${category.prefix}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UsahaBlue600),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.prefix,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UsahaNavy900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (category.isActive) UsahaEmerald100 else UsahaRed100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (category.isActive) "Aktif" else "Nonaktif",
                            color = if (category.isActive) UsahaEmerald700 else UsahaRed600,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "Prefix: ${category.prefix} • ${category.businessType} • $productCount Produk",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (category.description.isNotBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = UsahaSlate400,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Kategori",
                    tint = UsahaNavy900,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun VariantListItemCard(
    variant: ProductVariantEntity,
    parentProduct: ProductEntity?,
    currencyFormatter: NumberFormat,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("variant_item_${variant.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = variant.variantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "Produk Induk: ${parentProduct?.productName ?: "Produk"} (${parentProduct?.productCode ?: ""})",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500
                )
                if (!variant.sku.isNullOrBlank()) {
                    Text(
                        text = "SKU: ${variant.sku}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UsahaSlate400
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(variant.sellingPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsahaBlue600
                )
                Surface(
                    color = if (variant.isActive) UsahaEmerald100 else UsahaRed100,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (variant.isActive) "Aktif" else "Nonaktif",
                        color = if (variant.isActive) UsahaEmerald700 else UsahaRed600,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
