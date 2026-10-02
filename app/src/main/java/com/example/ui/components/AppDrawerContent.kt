package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessEntity
import com.example.domain.model.BusinessType
import com.example.ui.AppScreen
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald100
import com.example.ui.theme.UsahaEmerald600
import com.example.ui.theme.UsahaEmerald700
import com.example.ui.theme.UsahaNavy800
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500

@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    currentBusiness: BusinessEntity?,
    featureFlags: Map<String, Boolean>,
    onNavigate: (AppScreen) -> Unit,
    onOpenWorkspaceSwitcher: () -> Unit,
    onLogout: () -> Unit
) {
    var expandedProducts by remember { mutableStateOf(false) }
    var expandedSales by remember { mutableStateOf(false) }
    var expandedPurchasing by remember { mutableStateOf(false) }
    var expandedFinance by remember { mutableStateOf(false) }
    var expandedSettings by remember { mutableStateOf(false) }

    val businessType = currentBusiness?.let { BusinessType.fromCode(it.type) } ?: BusinessType.RETAIL

    // Check feature flags for dynamic menu display
    val hasFnbFeatures = featureFlags.entries.any { (k, v) -> k.startsWith("fnb_") && v } || businessType == BusinessType.FNB
    val hasRetailFeatures = featureFlags.entries.any { (k, v) -> k.startsWith("retail_") && v } || businessType == BusinessType.RETAIL
    val hasServiceFeatures = featureFlags.entries.any { (k, v) -> k.startsWith("service_") && v } || businessType == BusinessType.SERVICE

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp)
            .testTag("app_navigation_drawer")
    ) {
        // App Branding Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UsahaBlue600),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "USAHAOS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = UsahaNavy900,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Business Operating System",
                    style = MaterialTheme.typography.labelSmall,
                    color = UsahaSlate500
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Workspace Selector Card in drawer
        if (currentBusiness != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenWorkspaceSwitcher() }
                    .testTag("drawer_workspace_card"),
                shape = RoundedCornerShape(12.dp),
                color = UsahaSlate100,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(UsahaBlue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business",
                            tint = UsahaBlue600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentBusiness.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                        Text(
                            text = businessType.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaSlate500
                        )
                    }
                    Surface(
                        color = UsahaEmerald100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = UsahaEmerald700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            // Dashboard
            DrawerNavItem(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = currentScreen is AppScreen.Dashboard,
                onClick = { onNavigate(AppScreen.Dashboard) },
                testTag = "nav_dashboard"
            )

            // POS / Kasir
            DrawerNavItem(
                icon = Icons.Default.PointOfSale,
                label = "POS / Kasir",
                isSelected = currentScreen is AppScreen.PosCashier,
                onClick = { onNavigate(AppScreen.PosCashier) },
                testTag = "nav_pos"
            )

            // Produk Group
            DrawerNavGroup(
                icon = Icons.Default.Inventory2,
                label = "Produk",
                isExpanded = expandedProducts,
                onToggle = { expandedProducts = !expandedProducts },
                subItems = listOf(
                    "Semua Produk" to { onNavigate(AppScreen.Products("all")) },
                    "Kategori" to { onNavigate(AppScreen.Products("categories")) },
                    "Varian" to { onNavigate(AppScreen.Products("variants")) },
                    "Stok" to { onNavigate(AppScreen.Products("stock")) },
                    "Stock Opname" to { onNavigate(AppScreen.Products("opname")) }
                ),
                isGroupSelected = currentScreen is AppScreen.Products,
                testTag = "nav_group_products"
            )

            // Penjualan Group
            DrawerNavGroup(
                icon = Icons.Default.ShoppingCart,
                label = "Penjualan",
                isExpanded = expandedSales,
                onToggle = { expandedSales = !expandedSales },
                subItems = listOf(
                    "Semua Penjualan" to { onNavigate(AppScreen.Sales("all")) },
                    "Pelanggan" to { onNavigate(AppScreen.Sales("customers")) },
                    "Piutang" to { onNavigate(AppScreen.Sales("receivables")) }
                ),
                isGroupSelected = currentScreen is AppScreen.Sales,
                testTag = "nav_group_sales"
            )

            // Pembelian Group
            DrawerNavGroup(
                icon = Icons.Default.ShoppingBag,
                label = "Pembelian",
                isExpanded = expandedPurchasing,
                onToggle = { expandedPurchasing = !expandedPurchasing },
                subItems = listOf(
                    "Pembelian" to { onNavigate(AppScreen.Purchasing("all")) },
                    "Supplier" to { onNavigate(AppScreen.Purchasing("suppliers")) },
                    "Hutang" to { onNavigate(AppScreen.Purchasing("payables")) }
                ),
                isGroupSelected = currentScreen is AppScreen.Purchasing,
                testTag = "nav_group_purchasing"
            )

            // Keuangan Group
            DrawerNavGroup(
                icon = Icons.Default.AccountBalance,
                label = "Keuangan",
                isExpanded = expandedFinance,
                onToggle = { expandedFinance = !expandedFinance },
                subItems = listOf(
                    "Kas" to { onNavigate(AppScreen.Finance("cash")) },
                    "Pengeluaran" to { onNavigate(AppScreen.Finance("expenses")) },
                    "Cash Flow" to { onNavigate(AppScreen.Finance("cash_flow")) }
                ),
                isGroupSelected = currentScreen is AppScreen.Finance,
                testTag = "nav_group_finance"
            )

            // Laporan
            DrawerNavItem(
                icon = Icons.Default.Assessment,
                label = "Laporan",
                isSelected = currentScreen is AppScreen.Reports,
                onClick = { onNavigate(AppScreen.Reports) },
                testTag = "nav_reports"
            )

            // Dynamic Business Specific Modules (Adaptive based on toggles!)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MODUL BISNIS",
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate400,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )

            if (hasFnbFeatures) {
                DrawerNavItem(
                    icon = Icons.Default.Restaurant,
                    label = "Modul F&B (Meja/KDS)",
                    isSelected = currentScreen is AppScreen.FnbModule,
                    onClick = { onNavigate(AppScreen.FnbModule()) },
                    badge = "F&B",
                    testTag = "nav_fnb"
                )
            }

            if (hasRetailFeatures) {
                DrawerNavItem(
                    icon = Icons.Default.Inventory2,
                    label = "Modul Retail (Varian/Batch)",
                    isSelected = currentScreen is AppScreen.RetailModule,
                    onClick = { onNavigate(AppScreen.RetailModule()) },
                    badge = "Retail",
                    testTag = "nav_retail"
                )
            }

            if (hasServiceFeatures) {
                DrawerNavItem(
                    icon = Icons.Default.Work,
                    label = "Modul Jasa (SPK/Antrean)",
                    isSelected = currentScreen is AppScreen.ServiceModule,
                    onClick = { onNavigate(AppScreen.ServiceModule()) },
                    badge = "Jasa",
                    testTag = "nav_service"
                )
            }

            // Pengaturan
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "PENGATURAN",
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate400,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
            )

            DrawerNavItem(
                icon = Icons.Default.Tune,
                label = "Fitur & Modul Bisnis",
                isSelected = currentScreen is AppScreen.FeatureManagement,
                onClick = { onNavigate(AppScreen.FeatureManagement) },
                testTag = "nav_feature_settings"
            )

            DrawerNavItem(
                icon = Icons.Default.Settings,
                label = "Profil Usaha",
                isSelected = currentScreen is AppScreen.BusinessProfile,
                onClick = { onNavigate(AppScreen.BusinessProfile) },
                testTag = "nav_profile"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(8.dp))

        // Logout action
        DrawerNavItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Keluar / Logout",
            isSelected = false,
            onClick = onLogout,
            tint = MaterialTheme.colorScheme.error,
            testTag = "nav_logout"
        )
    }
}

@Composable
fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    tint: Color? = null,
    testTag: String
) {
    val bgColor = if (isSelected) UsahaBlue50 else Color.Transparent
    val contentColor = tint ?: if (isSelected) UsahaBlue600 else UsahaSlate500
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = fontWeight,
                modifier = Modifier.weight(1f)
            )
            if (badge != null) {
                Surface(
                    color = UsahaBlue50,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = UsahaBlue600,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerNavGroup(
    icon: ImageVector,
    label: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    subItems: List<Pair<String, () -> Unit>>,
    isGroupSelected: Boolean,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth().testTag(testTag)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggle() },
            color = if (isGroupSelected && !isExpanded) UsahaBlue50 else Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isGroupSelected) UsahaBlue600 else UsahaSlate500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGroupSelected) UsahaBlue600 else UsahaSlate500,
                    fontWeight = if (isGroupSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Tutup" else "Buka",
                    tint = UsahaSlate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 2.dp, bottom = 6.dp)
            ) {
                subItems.forEach { (subLabel, onSubClick) ->
                    Text(
                        text = "•  $subLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = UsahaSlate500,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSubClick() }
                            .padding(vertical = 7.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
