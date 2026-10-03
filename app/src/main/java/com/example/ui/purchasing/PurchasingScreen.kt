package com.example.ui.purchasing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.repository.PurchaseItemInput
import com.example.data.local.entity.PayableEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.ui.MainAppViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasingScreen(
    viewModel: MainAppViewModel,
    initialTab: Int = 0,
    onNavigateBack: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val payables by viewModel.payables.collectAsState()
    val products by viewModel.products.collectAsState()

    var tab by remember { mutableStateOf(initialTab.coerceIn(0, 2)) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var selectedPayable by remember { mutableStateOf<PayableEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembelian & Hutang") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showSupplierDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Tambah supplier")
                    }
                    IconButton(onClick = { showPurchaseDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Pembelian baru")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Pembelian") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Hutang") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Supplier") })
            }

            when (tab) {
                0 -> PurchaseList(
                    purchases = purchases,
                    onAdd = { showPurchaseDialog = true }
                )

                1 -> PayableList(
                    payables = payables,
                    onPay = { selectedPayable = it }
                )

                else -> SupplierList(
                    suppliers = suppliers,
                    onAdd = { showSupplierDialog = true }
                )
            }
        }
    }

    if (showSupplierDialog) {
        SupplierDialog(
            onDismiss = { showSupplierDialog = false },
            onSave = { name, phone, email, address ->
                viewModel.createSupplier(
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    onComplete = { showSupplierDialog = false }
                )
            }
        )
    }

    if (showPurchaseDialog) {
        PurchaseDialog(
            suppliers = suppliers,
            products = products,
            onDismiss = { showPurchaseDialog = false },
            onSave = { supplierId, supplierName, invoice, product, qty, cost, paid ->
                viewModel.createPurchase(
                    supplierId = supplierId,
                    supplierName = supplierName,
                    invoiceNumber = invoice,
                    items = listOf(
                        PurchaseItemInput(
                            productId = product.id,
                            productName = product.productName,
                            quantity = qty,
                            unitCost = cost
                        )
                    ),
                    paidAmount = paid,
                    onComplete = { showPurchaseDialog = false }
                )
            }
        )
    }

    selectedPayable?.let { payable ->
        PayDialog(
            payable = payable,
            onDismiss = { selectedPayable = null },
            onPay = { amount ->
                viewModel.payPayable(
                    payableId = payable.id,
                    amount = amount,
                    paymentMethod = "CASH",
                    onComplete = { selectedPayable = null }
                )
            }
        )
    }
}

@Composable
private fun PurchaseList(
    purchases: List<com.example.data.local.entity.PurchaseEntity>,
    onAdd: () -> Unit
) {
    if (purchases.isEmpty()) {
        EmptyPurchasing("Belum ada pembelian.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.padding(4.dp))
                Text("Pembelian Baru")
            }
        }

        items(purchases, key = { it.id }) { purchase ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(purchase.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                    Text(
                        purchase.supplierName.ifBlank { "Tanpa supplier" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(formatRupiah(purchase.totalAmount))
                    Text(
                        "Status: ${purchase.paymentStatus}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PayableList(
    payables: List<PayableEntity>,
    onPay: (PayableEntity) -> Unit
) {
    if (payables.isEmpty()) {
        EmptyPurchasing("Belum ada hutang supplier.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(payables, key = { it.id }) { payable ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        payable.supplierName.ifBlank { "Supplier" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Sisa: ${formatRupiah(payable.amount - payable.paidAmount)}")
                    Text("Status: ${payable.status}")
                    Spacer(Modifier.height(8.dp))
                    if (payable.status != "PAID") {
                        OutlinedButton(onClick = { onPay(payable) }) {
                            Icon(Icons.Default.Payment, null)
                            Spacer(Modifier.padding(4.dp))
                            Text("Bayar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierList(
    suppliers: List<SupplierEntity>,
    onAdd: () -> Unit
) {
    if (suppliers.isEmpty()) {
        EmptyPurchasing("Belum ada supplier.", onAdd)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.padding(4.dp))
                Text("Tambah Supplier")
            }
        }

        items(suppliers, key = { it.id }) { supplier ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(supplier.name, style = MaterialTheme.typography.titleMedium)
                    supplier.phone?.let { Text(it) }
                    supplier.email?.let { Text(it) }
                    supplier.address?.let { Text(it) }
                }
            }
        }
    }
}

@Composable
private fun EmptyPurchasing(
    message: String,
    onAdd: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        onAdd?.let {
            Button(onClick = it) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.padding(4.dp))
                Text("Tambah")
            }
        }
    }
}

@Composable
private fun SupplierDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama supplier") })
                OutlinedTextField(phone, { phone = it }, label = { Text("Telepon") })
                OutlinedTextField(email, { email = it }, label = { Text("Email") })
                OutlinedTextField(address, { address = it }, label = { Text("Alamat") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email, address) },
                enabled = name.isNotBlank()
            ) { Text("Simpan") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun PurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, ProductEntity, Int, Long, Long) -> Unit
) {
    var invoice by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var qty by remember { mutableStateOf("1") }
    var cost by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("") }
    var supplierExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pembelian Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    invoice,
                    { invoice = it },
                    label = { Text("No. invoice") }
                )

                OutlinedButton(
                    onClick = { supplierExpanded = !supplierExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (supplierName.isBlank()) "Pilih supplier"
                        else supplierName
                    )
                }

                if (supplierExpanded) {
                    suppliers.forEach { supplier ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                supplierName = supplier.name
                                supplierExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                supplier.name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { productExpanded = !productExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedProduct?.productName ?: "Pilih produk")
                }

                if (productExpanded) {
                    products.forEach { product ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                selectedProduct = product
                                productExpanded = false
                                if (cost.isBlank() && product.purchasePrice > 0L) {
                                    cost = product.purchasePrice.toString()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                product.productName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                OutlinedTextField(
                    qty,
                    { qty = it.filter(Char::isDigit) },
                    label = { Text("Jumlah") }
                )

                OutlinedTextField(
                    cost,
                    { cost = it.filter(Char::isDigit) },
                    label = { Text("Harga modal / unit") }
                )

                OutlinedTextField(
                    paid,
                    { paid = it.filter(Char::isDigit) },
                    label = { Text("Dibayar sekarang") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = invoice.isNotBlank() &&
                    supplierName.isNotBlank() &&
                    selectedProduct != null &&
                    (qty.toIntOrNull() ?: 0) > 0 &&
                    (cost.toLongOrNull() ?: -1L) >= 0L,
                onClick = {
                    onSave(
                        suppliers.firstOrNull { it.name == supplierName }?.id,
                        supplierName,
                        invoice,
                        selectedProduct!!,
                        qty.toInt(),
                        cost.toLong(),
                        paid.toLongOrNull() ?: 0L
                    )
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun PayDialog(
    payable: PayableEntity,
    onDismiss: () -> Unit,
    onPay: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val remaining = payable.amount - payable.paidAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bayar Hutang") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(payable.supplierName)
                Text("Sisa hutang: ${formatRupiah(remaining)}")
                OutlinedTextField(
                    amount,
                    { amount = it.filter(Char::isDigit) },
                    label = { Text("Jumlah pembayaran") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = (amount.toLongOrNull() ?: 0L) in 1..remaining,
                onClick = { onPay(amount.toLong()) }
            ) { Text("Bayar Tunai") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

private fun formatRupiah(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .format(value)
        .replace(",00", "")
