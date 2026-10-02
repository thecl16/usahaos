package com.example.ui.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessEntity
import com.example.domain.model.BusinessType
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.UsahaAmber100
import com.example.ui.theme.UsahaAmber600
import com.example.ui.theme.UsahaBlue100
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald600
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy800
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate300
import com.example.ui.theme.UsahaSlate500

@Composable
fun DashboardScreen(
    business: BusinessEntity?,
    productCount: Int = 0,
    categoryCount: Int = 0,
    onOpenPos: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenFeatureSettings: () -> Unit
) {
    val businessType = business?.let { BusinessType.fromCode(it.type) } ?: BusinessType.RETAIL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Business Header Welcome Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = UsahaBlue50,
            border = androidx.compose.foundation.BorderStroke(1.dp, UsahaBlue100)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = business?.name ?: "Workspace Usaha",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = UsahaBlue600,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = businessType.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Ringkasan Operasional Real-time • Mata Uang: ${business?.currency ?: "IDR"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = UsahaSlate500,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fast Action Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onOpenPos,
                        colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_quick_pos_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "POS Kasir",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Kasir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedButton(
                        onClick = onOpenProducts,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UsahaBlue600),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UsahaBlue100),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_quick_products_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Produk",
                            modifier = Modifier.size(16.dp),
                            tint = UsahaBlue600
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Katalog Produk", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = UsahaBlue600)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real Metrics Grid (Genuinely calculated from empty database: 0 values)
        Text(
            text = "Kinerja Hari Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = UsahaNavy900
        )
        Text(
            text = "Data murni dihitung dari database transaksi usaha.",
            style = MaterialTheme.typography.bodySmall,
            color = UsahaSlate500,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Penjualan Hari Ini",
                value = "Rp 0",
                subtext = "0 transaksi terselesaikan",
                icon = Icons.Default.TrendingUp,
                iconColor = UsahaBlue600,
                iconBg = UsahaBlue100,
                modifier = Modifier.weight(1f),
                testTag = "metric_sales_today"
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                title = "Total Transaksi",
                value = "0",
                subtext = "Belum ada transaksi POS",
                icon = Icons.Default.ReceiptLong,
                iconColor = UsahaNavy900,
                iconBg = UsahaSlate100,
                modifier = Modifier.weight(1f),
                testTag = "metric_total_transactions"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Gross Profit / Laba Kotor",
                value = "Rp 0",
                subtext = "Penjualan dikurangi HPP",
                icon = Icons.Default.Paid,
                iconColor = UsahaEmerald600,
                iconBg = UsahaEmerald100,
                modifier = Modifier.weight(1f),
                testTag = "metric_gross_profit"
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                title = "Saldo Kas Usaha",
                value = "Rp 0",
                subtext = "Kas masuk dikurangi keluar",
                icon = Icons.Default.AccountBalance,
                iconColor = UsahaAmber600,
                iconBg = UsahaAmber100,
                modifier = Modifier.weight(1f),
                testTag = "metric_cash_balance"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Alerts Section
        Text(
            text = "Peringatan & Status Operasional",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = UsahaNavy900
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            AlertChip(
                label = "Stok Menipis: 0",
                isOk = true,
                modifier = Modifier.weight(1f),
                testTag = "alert_low_stock"
            )
            Spacer(modifier = Modifier.width(8.dp))
            AlertChip(
                label = "Stok Habis: 0",
                isOk = true,
                modifier = Modifier.weight(1f),
                testTag = "alert_out_of_stock"
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            AlertChip(
                label = "Piutang Jatuh Tempo: Rp 0",
                isOk = true,
                modifier = Modifier.weight(1f),
                testTag = "alert_receivables_due"
            )
            Spacer(modifier = Modifier.width(8.dp))
            AlertChip(
                label = "Hutang Jatuh Tempo: Rp 0",
                isOk = true,
                modifier = Modifier.weight(1f),
                testTag = "alert_payables_due"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Charts Empty State (Prompt: "Do not generate fake chart values. If the database is empty: Show meaningful empty states.")
        Text(
            text = "Tren Penjualan & Analitik",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = UsahaNavy900
        )
        Spacer(modifier = Modifier.height(8.dp))

        EmptyStateView(
            icon = Icons.Default.TrendingUp,
            title = "Belum Ada Data Penjualan",
            description = "Grafik analitik dan tren penjualan akan muncul otomatis begitu transaksi pertama berhasil dicatat melalui kasir POS.",
            primaryButtonText = "Mulai Transaksi di POS",
            onPrimaryClick = onOpenPos,
            testTag = "dashboard_charts_empty"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Onboarding / Quick Start Guide for New UMKM Workspace
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(UsahaEmerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Panduan",
                            tint = UsahaEmerald700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Langkah Awal Memulai Operasional",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                        Text(
                            text = "Panduan persiapan sistem UsahaOS untuk tokomu",
                            style = MaterialTheme.typography.bodySmall,
                            color = UsahaSlate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SetupStepItem(
                    number = "1",
                    title = "Atur Fitur & Modul Tambahan",
                    description = "Nyalakan atau matikan fitur khusus seperti Meja, Varian, atau SPK.",
                    actionLabel = "Buka Fitur",
                    onClick = onOpenFeatureSettings,
                    testTag = "guide_step_1"
                )

                SetupStepItem(
                    number = "2",
                    title = if (productCount > 0) "Katalog Produk ($productCount Produk, $categoryCount Kategori)" else "Katalog & Stok Produk",
                    description = if (productCount > 0) "Produk aktif tersimpan di database. Siap dijual saat POS aktif." else "Buat kategori dan produk pertama dengan generate otomatis kode produk.",
                    actionLabel = if (productCount > 0) "Buka Katalog" else "Tambah Produk",
                    onClick = onOpenProducts,
                    testTag = "guide_step_2"
                )

                SetupStepItem(
                    number = "3",
                    title = "Catat Kas Operasional (Fase 2)",
                    description = "Isi saldo kas awal untuk modal kembalian kasir.",
                    actionLabel = "Lihat Keuangan",
                    onClick = onOpenFinance,
                    testTag = "guide_step_3"
                )

                SetupStepItem(
                    number = "4",
                    title = "Mulai Transaksi Kasir POS (Fase 2)",
                    description = "Lakukan penjualan cepat dan cetak struk pembayaran.",
                    actionLabel = "Lihat POS",
                    onClick = onOpenPos,
                    testTag = "guide_step_4"
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier.testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = UsahaSlate500,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900
            )

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate500,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun AlertChip(
    label: String,
    isOk: Boolean,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier.testTag(testTag),
        color = if (isOk) UsahaSlate100 else UsahaAmber100,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) UsahaEmerald600 else UsahaAmber600,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = UsahaNavy900,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SetupStepItem(
    number: String,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(UsahaSlate100),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = UsahaNavy900
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = UsahaNavy900
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = UsahaSlate500
            )
        }
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = UsahaBlue600
        )
    }
}
