package com.example.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald600
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    business: BusinessEntity?,
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    payment: PaymentEntity?,
    onDismiss: () -> Unit,
    onNewTransaction: (() -> Unit)? = null
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UsahaEmerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sukses",
                                tint = UsahaEmerald600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Transaksi Berhasil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Text(
                                text = "Struk Pembayaran Digital",
                                style = MaterialTheme.typography.bodySmall,
                                color = UsahaSlate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("receipt_close_btn")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = UsahaSlate500)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Printable Receipt Canvas Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UsahaSlate100)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Store Info
                        Text(
                            text = business?.name ?: "USAHAOS STORE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = UsahaNavy900,
                            textAlign = TextAlign.Center
                        )
                        if (!business?.address.isNullOrBlank()) {
                            Text(
                                text = business?.address ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = UsahaSlate500,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (!business?.phone.isNullOrBlank()) {
                            Text(
                                text = "Telp: ${business?.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = UsahaSlate500,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Invoice & Date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("No. Faktur:", fontSize = 12.sp, color = UsahaSlate500)
                            Text(sale.invoiceNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Waktu:", fontSize = 12.sp, color = UsahaSlate500)
                            Text(dateFormat.format(Date(sale.createdAt)), fontSize = 12.sp, color = UsahaNavy900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pelanggan:", fontSize = 12.sp, color = UsahaSlate500)
                            Text(sale.customerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = UsahaNavy900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Metode Bayar:", fontSize = 12.sp, color = UsahaSlate500)
                            val methodLabel = when (sale.paymentMethod) {
                                "CASH" -> "Tunai (Cash)"
                                "BANK_TRANSFER" -> "Transfer Bank"
                                "QRIS" -> "QRIS"
                                else -> "Lainnya / Kartu"
                            }
                            Text(methodLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = UsahaBlue600)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items Breakdown
                        Column(modifier = Modifier.fillMaxWidth()) {
                            items.forEach { item ->
                                val displayName = if (item.variantName != null) {
                                    "${item.productName} (${item.variantName})"
                                } else item.productName

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = UsahaNavy900
                                        )
                                        Text(
                                            text = "${item.quantity} x ${currencyFormat.format(item.unitPrice)}",
                                            fontSize = 11.sp,
                                            color = UsahaSlate500
                                        )
                                    }
                                    Text(
                                        text = currencyFormat.format(item.subtotal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = UsahaNavy900
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Totals
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp, color = UsahaSlate500)
                            Text(currencyFormat.format(sale.subtotal), fontSize = 13.sp, color = UsahaNavy900)
                        }
                        if (sale.discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diskon", fontSize = 13.sp, color = UsahaEmerald700)
                                Text("-${currencyFormat.format(sale.discountAmount)}", fontSize = 13.sp, color = UsahaEmerald700)
                            }
                        }
                        if (sale.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pajak", fontSize = 13.sp, color = UsahaSlate500)
                                Text(currencyFormat.format(sale.taxAmount), fontSize = 13.sp, color = UsahaNavy900)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UsahaNavy900)
                            Text(
                                currencyFormat.format(sale.totalAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = UsahaBlue600
                            )
                        }

                        if (payment != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bayar", fontSize = 13.sp, color = UsahaSlate500)
                                Text(currencyFormat.format(payment.amountPaid), fontSize = 13.sp, color = UsahaNavy900)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kembalian", fontSize = 13.sp, color = UsahaSlate500)
                                Text(
                                    currencyFormat.format(payment.changeAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UsahaEmerald700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // PAID STAMP
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = UsahaEmerald100,
                            border = androidx.compose.foundation.BorderStroke(1.dp, UsahaEmerald600)
                        ) {
                            Text(
                                text = "LUNAS / PAID",
                                color = UsahaEmerald700,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Terima kasih atas kunjungan Anda!",
                            fontSize = 11.sp,
                            color = UsahaSlate400,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onNewTransaction != null) {
                        Button(
                            onClick = onNewTransaction,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("receipt_new_transaction_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transaksi Baru")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("receipt_done_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}
