package com.example.ui.products

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    category: CategoryEntity?,
    variants: List<ProductVariantEntity>,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDismiss: () -> Unit
) {
    val rupiahFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("product_detail_dialog"),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UsahaSlate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "Produk",
                                tint = UsahaNavy900,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Text(
                                text = category?.name ?: "Tanpa Kategori",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaSlate500
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("product_detail_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = UsahaSlate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(14.dp))

                // Product Code Banner
                Surface(
                    color = UsahaSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Kode Produk",
                            tint = UsahaNavy900,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kode Produk Internal (Read-Only)",
                                style = MaterialTheme.typography.labelSmall,
                                color = UsahaSlate500
                            )
                            Text(
                                text = product.productCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = UsahaNavy900,
                                letterSpacing = 1.sp
                            )
                        }

                        // Status Badge
                        Surface(
                            color = if (product.isActive) UsahaEmerald100 else UsahaRed100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (product.isActive) "Aktif" else "Nonaktif",
                                color = if (product.isActive) UsahaEmerald700 else UsahaRed600,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailCard(
                        title = "Harga Jual",
                        value = rupiahFormatter.format(product.sellingPrice),
                        subtext = "per ${product.unit}",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DetailCard(
                        title = "Harga Beli / HPP",
                        value = rupiahFormatter.format(product.purchasePrice),
                        subtext = "Estimasi modal",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailCard(
                        title = "SKU",
                        value = product.sku ?: "-",
                        subtext = "Internal SKU",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DetailCard(
                        title = "Barcode",
                        value = product.barcode ?: "-",
                        subtext = if (product.barcode?.startsWith("200") == true) "Internal Barcode" else "Standar",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stock settings summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelola Stok: ${if (product.trackStock) "Aktif" else "Tidak dipantau"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = UsahaSlate500,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Min. Stok: ${product.minStock} ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = UsahaSlate500
                    )
                }

                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Deskripsi:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = UsahaNavy900
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = UsahaSlate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Variants list
                if (variants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Daftar Varian Produk (${variants.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UsahaNavy900
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    variants.forEach { v ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            color = UsahaSlate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = v.variantName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = UsahaNavy900
                                    )
                                    if (!v.sku.isNullOrBlank()) {
                                        Text(
                                            text = "SKU: ${v.sku}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UsahaSlate500
                                        )
                                    }
                                }
                                Text(
                                    text = rupiahFormatter.format(v.sellingPrice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UsahaBlue600
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onToggleActive,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (product.isActive) "Nonaktifkan" else "Aktifkan",
                            color = if (product.isActive) UsahaRed600 else UsahaEmerald600
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Produk")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, UsahaSlate300),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate400
            )
        }
    }
}
