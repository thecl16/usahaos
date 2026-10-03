package com.example.ui.pos

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.repository.CartItem
import com.example.ui.MainAppViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.UsahaAmber100
import com.example.ui.theme.UsahaAmber600
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
import com.example.ui.theme.UsahaSlate200
import com.example.ui.theme.UsahaSlate300
import com.example.ui.theme.UsahaSlate500
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: MainAppViewModel,
    onNavigateBack: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val products by viewModel.products.collectAsState()
    val variants by viewModel.variants.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val stockBalancesMap by viewModel.stockBalancesMap.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val cartDiscount by viewModel.cartDiscount.collectAsState()
    val lastReceipt by viewModel.lastReceipt.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var barcodeInput by remember { mutableStateOf("") }

    // Modal Sheet states
    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var variantPickerProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    val activeProducts = products.filter { it.isActive }

    // Filter products
    val filteredProducts = activeProducts.filter { prod ->
        val matchCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId
        val matchQuery = searchQuery.isBlank() ||
                prod.productName.contains(searchQuery, ignoreCase = true) ||
                prod.productCode.contains(searchQuery, ignoreCase = true) ||
                (prod.sku != null && prod.sku.contains(searchQuery, ignoreCase = true)) ||
                (prod.barcode != null && prod.barcode.contains(searchQuery, ignoreCase = true))
        matchCategory && matchQuery
    }

    val cartTotalCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.subtotal }
    val cartGrandTotal = (cartSubtotal - cartDiscount).coerceAtLeast(0L)

    // Function to handle barcode scan / enter
    fun handleBarcodeSubmit(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return

        // Search in variants first
        val variantMatch = variants.firstOrNull { it.barcode.equals(trimmed, ignoreCase = true) || it.sku.equals(trimmed, ignoreCase = true) }
        if (variantMatch != null) {
            val parent = products.firstOrNull { it.id == variantMatch.productId }
            if (parent != null) {
                viewModel.addToCart(parent, variantMatch, 1)
                barcodeInput = ""
                return
            }
        }

        // Search in products
        val productMatch = activeProducts.firstOrNull {
            it.barcode.equals(trimmed, ignoreCase = true) ||
                    it.sku.equals(trimmed, ignoreCase = true) ||
                    it.productCode.equals(trimmed, ignoreCase = true)
        }
        if (productMatch != null) {
            val prodVariants = variants.filter { it.productId == productMatch.id }
            if (prodVariants.isNotEmpty()) {
                variantPickerProduct = productMatch
            } else {
                viewModel.addToCart(productMatch, null, 1)
            }
            barcodeInput = ""
        } else {
            viewModel.showMessage("Produk dengan kode/barcode '$trimmed' tidak ditemukan", isError = true)
        }
    }

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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("pos_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = UsahaNavy900)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "POS / Kasir",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = UsahaBlue50,
                                shape = RoundedCornerShape(4.dp)
                            ) {
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
                            text = "Pilih produk atau scan barcode untuk transaksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaSlate500
                        )
                    }

                    // Cart Trigger Button with Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (cartItems.isNotEmpty()) UsahaBlue600 else UsahaSlate100,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCartSheet = true }
                            .testTag("pos_cart_trigger_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Keranjang",
                                tint = if (cartItems.isNotEmpty()) Color.White else UsahaNavy900,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cartItems.isNotEmpty()) "$cartTotalCount Item" else "Keranjang",
                                color = if (cartItems.isNotEmpty()) Color.White else UsahaNavy900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Search Bar & Barcode Scanner Input Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("pos_search_input"),
                        placeholder = { Text("Cari produk...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = UsahaSlate500, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_barcode_input"),
                        placeholder = { Text("Scan / Barcode", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = UsahaBlue600, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (barcodeInput.isNotEmpty()) {
                                IconButton(onClick = { handleBarcodeSubmit(barcodeInput) }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = UsahaBlue600)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { handleBarcodeSubmit(barcodeInput) }),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        val isAll = selectedCategoryId == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedCategoryId = null }
                                .testTag("pos_cat_filter_all"),
                            color = if (isAll) UsahaBlue600 else UsahaSlate100,
                            shape = RoundedCornerShape(6.dp)
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

                    items(categories.filter { it.isActive }) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedCategoryId = if (isSelected) null else cat.id }
                                .testTag("pos_cat_filter_${cat.id}"),
                            color = if (isSelected) UsahaBlue600 else UsahaSlate100,
                            shape = RoundedCornerShape(6.dp)
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

            // Products Catalog Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.ShoppingBag,
                        title = if (activeProducts.isEmpty()) "Belum ada produk aktif" else "Tidak ada produk yang cocok",
                        description = if (activeProducts.isEmpty())
                            "Silakan tambahkan produk terlebih dahulu melalui menu Katalog Produk."
                        else "Coba gunakan kata kunci pencarian atau filter kategori lain."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val prodVariants = variants.filter { it.productId == product.id }
                        val stock = stockBalancesMap[product.id] ?: 0

                        ProductPOSCard(
                            product = product,
                            variants = prodVariants,
                            currentStock = stock,
                            onProductClick = {
                                if (prodVariants.isNotEmpty()) {
                                    variantPickerProduct = product
                                } else {
                                    if (product.trackStock && stock <= 0 && !product.allowNegativeStock) {
                                        viewModel.showMessage("Stok ${product.productName} habis!", isError = true)
                                    } else {
                                        viewModel.addToCart(product, null, 1)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Sticky Cart Bar when cart has items
        if (cartItems.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("pos_bottom_cart_bar"),
                shape = RoundedCornerShape(12.dp),
                color = UsahaNavy900,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCartSheet = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UsahaBlue600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$cartTotalCount Barang di Keranjang",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = selectedCustomer?.let { "Pelanggan: ${it.name}" } ?: "Pelanggan Umum",
                                color = UsahaSlate300,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencyFormat.format(cartGrandTotal),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("pos_quick_checkout_btn")
                        ) {
                            Text("Bayar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Cart Details
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("pos_cart_bottom_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keranjang Belanja ($cartTotalCount Item)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UsahaNavy900
                    )
                    TextButton(
                        onClick = {
                            viewModel.clearCart()
                            showCartSheet = false
                        },
                        modifier = Modifier.testTag("pos_clear_cart_btn")
                    ) {
                        Text("Kosongkan", color = UsahaRed600, fontSize = 12.sp)
                    }
                }

                // Customer Selection Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = UsahaSlate100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerDialog = true }
                        .padding(vertical = 8.dp)
                        .testTag("pos_select_customer_trigger")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = UsahaBlue600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pelanggan", fontSize = 10.sp, color = UsahaSlate500)
                            Text(
                                text = selectedCustomer?.name ?: "Pelanggan Umum (Walk-in)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UsahaNavy900
                            )
                        }
                        Text("Ubah", fontSize = 12.sp, color = UsahaBlue600, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems.indices.toList()) { index ->
                        val item = cartItems[index]
                        CartItemRow(
                            item = item,
                            onIncrement = { viewModel.updateCartItemQuantity(index, item.quantity + 1) },
                            onDecrement = { viewModel.updateCartItemQuantity(index, item.quantity - 1) },
                            onRemove = { viewModel.removeCartItem(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Totals
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = 13.sp, color = UsahaSlate500)
                    Text(currencyFormat.format(cartSubtotal), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = UsahaNavy900)
                }

                if (cartDiscount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon Transaksi", fontSize = 13.sp, color = UsahaEmerald700)
                        Text("-${currencyFormat.format(cartDiscount)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = UsahaEmerald700)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Belanja", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                    Text(
                        currencyFormat.format(cartGrandTotal),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = UsahaBlue600
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showCheckoutDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pos_proceed_checkout_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lanjut ke Pembayaran (${currencyFormat.format(cartGrandTotal)})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Checkout & Payment Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            totalAmount = cartGrandTotal,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = { method, amountPaid, notes ->
                viewModel.checkout(
                    paymentMethod = method,
                    amountPaid = amountPaid,
                    notes = notes,
                    onComplete = {
                        showCheckoutDialog = false
                    }
                )
            }
        )
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customers,
            selected = selectedCustomer,
            onSelect = {
                viewModel.setSelectedCustomer(it)
                showCustomerDialog = false
            },
            onCreateNew = { name, phone, email, address ->
                viewModel.createCustomer(name, phone, email, address) {
                    showCustomerDialog = false
                }
            },
            onDismiss = { showCustomerDialog = false }
        )
    }

    // Variant Picker Dialog
    if (variantPickerProduct != null) {
        val prod = variantPickerProduct!!
        val prodVariants = variants.filter { it.productId == prod.id && it.isActive }

        VariantPickerDialog(
            product = prod,
            variants = prodVariants,
            stockBalancesMap = stockBalancesMap,
            onSelect = { v ->
                viewModel.addToCart(prod, v, 1)
                variantPickerProduct = null
            },
            onDismiss = { variantPickerProduct = null }
        )
    }

    // Show Digital Receipt Modal when checkout succeeds
    if (lastReceipt != null) {
        val receipt = lastReceipt!!
        ReceiptDialog(
            business = currentBusiness,
            sale = receipt.sale,
            items = receipt.items,
            payment = receipt.payment,
            onDismiss = { viewModel.clearLastReceipt() },
            onNewTransaction = {
                viewModel.clearLastReceipt()
                viewModel.clearCart()
            }
        )
    }
}

@Composable
fun ProductPOSCard(
    product: ProductEntity,
    variants: List<ProductVariantEntity>,
    currentStock: Int,
    onProductClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onProductClick() }
            .testTag("pos_product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Code & Variant Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.productCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = UsahaSlate500
                )
                if (variants.isNotEmpty()) {
                    Surface(color = UsahaBlue50, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "${variants.size} Varian",
                            fontSize = 10.sp,
                            color = UsahaBlue600,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Selling Price
            Text(
                text = currencyFormat.format(product.sellingPrice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = UsahaBlue600
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stock Indicator Badge
            if (!product.trackStock) {
                Surface(color = UsahaSlate100, shape = RoundedCornerShape(4.dp)) {
                    Text("Non-Stok", fontSize = 10.sp, color = UsahaSlate500, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            } else if (currentStock <= 0) {
                Surface(color = UsahaRed100, shape = RoundedCornerShape(4.dp)) {
                    Text("Habis (0)", fontSize = 10.sp, color = UsahaRed600, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            } else if (currentStock <= product.minStock) {
                Surface(color = UsahaAmber100, shape = RoundedCornerShape(4.dp)) {
                    Text("Menipis ($currentStock ${product.unit})", fontSize = 10.sp, color = UsahaAmber600, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            } else {
                Surface(color = UsahaEmerald100, shape = RoundedCornerShape(4.dp)) {
                    Text("Stok: $currentStock ${product.unit}", fontSize = 10.sp, color = UsahaEmerald700, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = UsahaSlate100,
        border = androidx.compose.foundation.BorderStroke(1.dp, UsahaSlate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                if (item.variantName != null) {
                    Text(
                        text = "Varian: ${item.variantName}",
                        fontSize = 11.sp,
                        color = UsahaBlue600
                    )
                }
                Text(
                    text = currencyFormat.format(item.unitPrice),
                    fontSize = 11.sp,
                    color = UsahaSlate500
                )
            }

            // Qty modifiers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onDecrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp), tint = UsahaNavy900)
                    }
                }

                Text(
                    text = "${item.quantity}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp),
                    color = UsahaNavy900
                )

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onIncrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp), tint = UsahaNavy900)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Subtotal
            Text(
                text = currencyFormat.format(item.subtotal),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = UsahaSlate500, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    totalAmount: Long,
    onDismiss: () -> Unit,
    onConfirmCheckout: (paymentMethod: String, amountPaid: Long, notes: String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    var selectedMethod by remember { mutableStateOf("CASH") } // CASH, BANK_TRANSFER, QRIS, OTHER
    var cashPaidInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amountPaid = if (selectedMethod == "CASH") {
        cashPaidInput.toLongOrNull() ?: 0L
    } else {
        totalAmount
    }

    val isCashValid = selectedMethod != "CASH" || amountPaid >= totalAmount
    val changeAmount = (amountPaid - totalAmount).coerceAtLeast(0L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("pos_checkout_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "Total Belanja: ${currencyFormat.format(totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = UsahaBlue600,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                Text("Metode Pembayaran", fontSize = 12.sp, color = UsahaSlate500, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val methods = listOf(
                        "CASH" to "Tunai",
                        "BANK_TRANSFER" to "Transfer",
                        "QRIS" to "QRIS",
                        "OTHER" to "Debit/EDC"
                    )
                    methods.forEach { (key, label) ->
                        val isSelected = selectedMethod == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedMethod = key
                                    if (key == "CASH" && cashPaidInput.isEmpty()) {
                                        cashPaidInput = "$totalAmount"
                                    }
                                }
                                .testTag("payment_method_$key"),
                            color = if (isSelected) UsahaBlue600 else UsahaSlate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else UsahaNavy900,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CASH Specific controls
                if (selectedMethod == "CASH") {
                    Text("Nominal Diterima", fontSize = 12.sp, color = UsahaSlate500)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cashPaidInput,
                        onValueChange = { cashPaidInput = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pos_cash_paid_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Masukkan nominal uang tunai") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Cash Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickAmounts = listOf(
                            "Pas" to totalAmount,
                            "10k" to 10000L,
                            "20k" to 20000L,
                            "50k" to 50000L,
                            "100k" to 100000L
                        )
                        quickAmounts.forEach { (label, amt) ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { cashPaidInput = "$amt" },
                                color = UsahaSlate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = UsahaBlue600,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kembalian / Change calculation display
                    if (amountPaid < totalAmount) {
                        Surface(color = UsahaRed100, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Kurang ${currencyFormat.format(totalAmount - amountPaid)}",
                                color = UsahaRed600,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        Surface(color = UsahaEmerald100, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kembalian:", color = UsahaEmerald700, fontSize = 13.sp)
                                Text(
                                    currencyFormat.format(changeAmount),
                                    color = UsahaEmerald700,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        color = UsahaBlue50,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = when (selectedMethod) {
                                    "QRIS" -> "Scan kode QRIS usaha Anda. Pembayaran otomatis tercatat lunas."
                                    "BANK_TRANSFER" -> "Instruksikan transfer ke rekening usaha. Konfirmasi bukti transfer."
                                    else -> "Gunakan mesin EDC atau mesin kartu debit/kredit."
                                },
                                fontSize = 12.sp,
                                color = UsahaBlue600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            onConfirmCheckout(selectedMethod, amountPaid, notes)
                        },
                        enabled = isCashValid,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_confirm_payment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerSelectionDialog(
    customers: List<CustomerEntity>,
    selected: CustomerEntity?,
    onSelect: (CustomerEntity?) -> Unit,
    onCreateNew: (name: String, phone: String?, email: String?, address: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("customer_selection_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pilih Pelanggan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                if (!showCreateForm) {
                    // Option 1: Pelanggan Umum
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null) }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected == null) UsahaBlue50 else UsahaSlate100
                    ) {
                        Text(
                            text = "Pelanggan Umum (Walk-in)",
                            fontWeight = if (selected == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected == null) UsahaBlue600 else UsahaNavy900,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(customers) { c ->
                            val isChosen = selected?.id == c.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(c) }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) UsahaBlue50 else UsahaSlate100
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(c.name, fontWeight = FontWeight.Bold, color = UsahaNavy900, fontSize = 13.sp)
                                    if (!c.phone.isNullOrBlank()) {
                                        Text(c.phone, fontSize = 11.sp, color = UsahaSlate500)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showCreateForm = true },
                        modifier = Modifier.fillMaxWidth().testTag("pos_add_new_customer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Pelanggan Baru")
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("No. HP / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showCreateForm = false }, modifier = Modifier.weight(1f)) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onCreateNew(newName, newPhone, null, null)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("save_new_customer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VariantPickerDialog(
    product: ProductEntity,
    variants: List<ProductVariantEntity>,
    stockBalancesMap: Map<String, Int>,
    onSelect: (ProductVariantEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("pos_variant_picker_dialog"),
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
                        Text(product.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = UsahaNavy900)
                        Text("Pilih varian produk", fontSize = 12.sp, color = UsahaSlate500)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(variants) { variant ->
                        val vKey = "${product.id}_${variant.id}"
                        val vStock = stockBalancesMap[vKey] ?: 0

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(variant) }
                                .testTag("variant_option_${variant.id}"),
                            color = UsahaSlate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(variant.variantName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = UsahaNavy900)
                                    Text("Stok: $vStock", fontSize = 11.sp, color = UsahaSlate500)
                                }
                                Text(
                                    text = currencyFormat.format(variant.sellingPrice),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = UsahaBlue600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
