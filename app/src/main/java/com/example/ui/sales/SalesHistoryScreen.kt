package com.example.ui.sales

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.ui.MainAppViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.pos.ReceiptDialog
import com.example.ui.theme.UsahaBlue100
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    viewModel: MainAppViewModel,
    onNavigateBack: () -> Unit,
    onOpenPOS: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val cashBalance by viewModel.cashBalance.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var viewingReceiptSale by remember { mutableStateOf<SaleEntity?>(null) }
    var viewingReceiptItems by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
    var viewingReceiptPayment by remember { mutableStateOf<PaymentEntity?>(null) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    val filteredSales = sales.filter { s ->
        searchQuery.isBlank() ||
                s.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                s.customerName.contains(searchQuery, ignoreCase = true)
    }

    val totalSalesAmount = sales.sumOf { it.totalAmount }
    val totalSalesCount = sales.size

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
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("sales_history_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = UsahaNavy900)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Riwayat Penjualan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                        Text(
                            text = currentBusiness?.name ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaSlate500
                        )
                    }

                    Button(
                        onClick = onOpenPOS,
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sales_open_pos_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka POS", fontSize = 12.sp)
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Metric Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = UsahaBlue50)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Penjualan", fontSize = 11.sp, color = UsahaBlue600, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(currencyFormat.format(totalSalesAmount), fontSize = 15.sp, fontWeight = FontWeight.Black, color = UsahaBlue600)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = UsahaEmerald100)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Saldo Kas Usaha", fontSize = 11.sp, color = UsahaEmerald700, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(currencyFormat.format(cashBalance), fontSize = 15.sp, fontWeight = FontWeight.Black, color = UsahaEmerald700)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = UsahaSlate100)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Transaksi", fontSize = 11.sp, color = UsahaSlate500, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$totalSalesCount", fontSize = 15.sp, fontWeight = FontWeight.Black, color = UsahaNavy900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari no. faktur atau nama pelanggan...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = UsahaSlate500) },
                    modifier = Modifier.fillMaxWidth().testTag("sales_history_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredSales.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        EmptyStateView(
                            icon = Icons.Default.ReceiptLong,
                            title = if (sales.isEmpty()) "Belum ada transaksi penjualan" else "Transaksi tidak ditemukan",
                            description = if (sales.isEmpty())
                                "Buka POS / Kasir untuk mulai melayani pelanggan dan mencatat transaksi penjualan."
                            else "Coba gunakan kata kunci pencarian nomor faktur yang lain.",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredSales, key = { it.id }) { sale ->
                            SaleHistoryCard(
                                sale = sale,
                                onShowReceipt = {
                                    viewModel.getSaleDetail(sale.id) { s, items, pay ->
                                        viewingReceiptSale = s
                                        viewingReceiptItems = items
                                        viewingReceiptPayment = pay
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog for viewing past sales
    if (viewingReceiptSale != null) {
        ReceiptDialog(
            business = currentBusiness,
            sale = viewingReceiptSale!!,
            items = viewingReceiptItems,
            payment = viewingReceiptPayment,
            onDismiss = { viewingReceiptSale = null }
        )
    }
}

@Composable
fun SaleHistoryCard(
    sale: SaleEntity,
    onShowReceipt: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Card(
        modifier = Modifier.fillMaxWidth().testTag("sale_card_${sale.id}"),
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
                Column {
                    Text(
                        text = sale.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = UsahaNavy900
                    )
                    Text(
                        text = dateFormat.format(Date(sale.createdAt)),
                        fontSize = 11.sp,
                        color = UsahaSlate500
                    )
                }

                Surface(
                    color = UsahaEmerald100,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LUNAS",
                        color = UsahaEmerald700,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pelanggan: ${sale.customerName}",
                        fontSize = 12.sp,
                        color = UsahaSlate500
                    )
                    val methodLabel = when (sale.paymentMethod) {
                        "CASH" -> "Tunai (Cash)"
                        "BANK_TRANSFER" -> "Transfer Bank"
                        "QRIS" -> "QRIS"
                        else -> "Kartu / EDC"
                    }
                    Text(
                        text = "Metode: $methodLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UsahaBlue600
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(sale.totalAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = UsahaNavy900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onShowReceipt,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("view_receipt_btn_${sale.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Struk", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
