package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.AuthScreen
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.components.ModulePlaceholderScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.features.FeatureManagementScreen
import com.example.ui.finance.FinanceScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.pos.POSScreen
import com.example.ui.products.ProductManagementScreen
import com.example.ui.purchasing.PurchasingScreen
import com.example.ui.sales.SalesHistoryScreen
import com.example.ui.setup.BusinessSetupScreen
import com.example.ui.workspace.BusinessProfileScreen
import com.example.ui.workspace.WorkspaceSwitcherDialog
import kotlinx.coroutines.launch

@Composable
fun MainApp(viewModel: MainAppViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentBusiness by viewModel.currentBusiness.collectAsStateWithLifecycle()
    val userBusinesses by viewModel.userBusinesses.collectAsStateWithLifecycle()
    val featureFlags by viewModel.featureFlags.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val allVariants by viewModel.variants.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val isWorkspaceDialogOpen by viewModel.isWorkspaceDialogOpen.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle Snackbars
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg.message)
            viewModel.clearMessage()
        }
    }

    // 1. Not logged in -> Show Auth Screen
    if (currentUser == null) {
        AuthScreen(
            isLoading = isLoading,
            onLogin = { email, pass -> viewModel.login(email, pass) },
            onRegister = { name, email, phone, pass -> viewModel.register(name, email, phone, pass) },
            onResetPassword = { email, pass -> viewModel.resetPassword(email, pass) }
        )
        return
    }

    // 2. User has no business yet or explicitly on BusinessSetup screen -> Show Setup Screen
    if (userBusinesses.isEmpty() || currentScreen is AppScreen.BusinessSetup) {
        BusinessSetupScreen(
            isLoading = isLoading,
            onCreateBusiness = { name, type, address, phone, email, currency ->
                viewModel.createBusiness(name, type, address, phone, email, currency)
            }
        )
        return
    }

    // 3. Main Business Shell with Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(
                    currentScreen = currentScreen,
                    currentBusiness = currentBusiness,
                    featureFlags = featureFlags,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenWorkspaceSwitcher = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openWorkspaceDialog()
                    },
                    onLogout = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.logout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    business = currentBusiness,
                    user = currentUser,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onOpenWorkspaceDialog = { viewModel.openWorkspaceDialog() },
                    onOpenProfile = { viewModel.navigateTo(AppScreen.BusinessProfile) },
                    onOpenFeatureSettings = { viewModel.navigateTo(AppScreen.FeatureManagement) },
                    onLogout = { viewModel.logout() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val screen = currentScreen) {
                    is AppScreen.Dashboard -> {
                        DashboardScreen(
                            business = currentBusiness,
                            productCount = products.size,
                            categoryCount = categories.size,
                            onOpenPos = { viewModel.navigateTo(AppScreen.PosCashier) },
                            onOpenProducts = { viewModel.navigateTo(AppScreen.Products()) },
                            onOpenFinance = { viewModel.navigateTo(AppScreen.Finance()) },
                            onOpenFeatureSettings = { viewModel.navigateTo(AppScreen.FeatureManagement) }
                        )
                    }

                    is AppScreen.PosCashier -> {
            POSScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.navigateBack() }
            )
        }

        is AppScreen.Products -> {
                        when (screen.subTab) {
                            "stock" -> {
                                ModulePlaceholderScreen(
                                    title = "Manajemen Stok & Kartu Stok",
                                    subtitle = "Pelacakan pergerakan stok keluar/masuk, stok minimum, dan mutasi barang",
                                    emptyTitle = "Belum Ada Pergerakan Stok",
                                    emptyDescription = "Modul inventaris dan kartu stok akan aktif pada Fase 3 (Inventory & Stock Movement).",
                                    icon = Icons.Default.Inventory2,
                                    phaseNumber = "Fase 3",
                                    businessName = currentBusiness?.name ?: "",
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                            "opname" -> {
        InventoryScreen(
            viewModel = viewModel,
            initialTab = 1,
            onNavigateBack = { viewModel.navigateBack() }
        )
    }
    else -> {
                                ProductManagementScreen(
                                    initialSubTab = screen.subTab,
                                    business = currentBusiness,
                                    categories = categories,
                                    products = products,
                                    allVariants = allVariants,
                                    onRequestNextCode = { prefix -> viewModel.previewNextProductCode(prefix) },
                                    onGenerateInternalBarcode = { viewModel.generateInternalBarcode() },
                                    onGetVariantsForProduct = { pId -> viewModel.getVariantsForProduct(pId) },
                                    onCreateCategory = { name, prefix, bType, desc ->
    viewModel.createCategory(
        name,
        prefix,
        com.example.domain.model.BusinessType.fromCode(bType),
        desc
    )
},
onUpdateCategory = { id, name, prefix, bType, desc, isActive ->
    categories.find { it.id == id }?.let { category ->
        viewModel.updateCategory(
            category,
            name,
            prefix,
            desc,
            isActive
        )
    }
},
                                    onToggleCategoryStatus = { catId, isActive ->
                                        viewModel.toggleCategoryStatus(catId, isActive)
                                    },
                                    onCreateProduct = { catId, name, sku, barcode, unit, buyPrice, sellPrice, minStk, track, desc, vList ->
    viewModel.createProduct(
        categoryId = catId,
        productName = name,
        sku = sku,
        barcode = barcode,
        unit = unit,
        purchasePrice = buyPrice,
        sellingPrice = sellPrice,
        minStock = minStk,
        trackStock = track,
        allowNegativeStock = false,
        description = desc,
        initialStock = 0,
        variants = vList
    )
},
                                    onUpdateProduct = { prod, vList ->
                                        viewModel.updateProduct(prod, vList)
                                    },
                                    onToggleProductStatus = { prodId, isActive ->
                                        viewModel.toggleProductStatus(prodId, isActive)
                                    },
                                    onToggleVariantStatus = { vId, isActive ->
                                        viewModel.toggleVariantStatus(vId, isActive)
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                        }
                    }

                    is AppScreen.Sales -> {
            SalesHistoryScreen(
                viewModel = viewModel,
                onOpenPOS = { viewModel.navigateTo(AppScreen.PosCashier) },
                onNavigateBack = { viewModel.navigateBack() }
            )
        }

        is AppScreen.Purchasing -> {
    PurchasingScreen(
        viewModel = viewModel,
        initialTab = 0,
        onNavigateBack = { viewModel.navigateBack() }
    )
}

                    is AppScreen.Finance -> {
    FinanceScreen(
        viewModel = viewModel,
        initialTab = 0,
        onNavigateBack = { viewModel.navigateBack() }
    )
}

                    is AppScreen.Reports -> {
                        ModulePlaceholderScreen(
                            title = "Laporan Operasional Bisnis",
                            subtitle = "Laporan laba rugi, penjualan, inventaris, arus kas, dan margin produk",
                            emptyTitle = "Belum Cukup Data Laporan",
                            emptyDescription = "Laporan komprehensif akan dihitung otomatis dari data transaksi riil pada Fase 6.",
                            icon = Icons.Default.Assessment,
                            phaseNumber = "Fase 6",
                            businessName = currentBusiness?.name ?: "",
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    is AppScreen.FnbModule -> {
                        ModulePlaceholderScreen(
                            title = "Modul Operasional F&B",
                            subtitle = "Manajemen nomor meja, Kitchen Display System (KDS), resep & food cost",
                            emptyTitle = "Belum Ada Pesanan Dapur",
                            emptyDescription = "Modul F&B menghubungkan alur meja, dapur koki, dan kasir pelunasan secara real-time.",
                            icon = Icons.Default.Restaurant,
                            phaseNumber = "Fase 7",
                            businessName = currentBusiness?.name ?: "",
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    is AppScreen.RetailModule -> {
                        ModulePlaceholderScreen(
                            title = "Modul Operasional Retail",
                            subtitle = "Varian produk, atribut ukuran/warna, serial number, batch & tanggal kedaluwarsa",
                            emptyTitle = "Belum Ada Batch / Serial Number",
                            emptyDescription = "Kelola ribuan stok barang dagangan dengan nomor seri dan pelacakan tanggal kadaluwarsa FEFO.",
                            icon = Icons.Default.Inventory2,
                            phaseNumber = "Fase 7",
                            businessName = currentBusiness?.name ?: "",
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    is AppScreen.ServiceModule -> {
                        ModulePlaceholderScreen(
                            title = "Modul Operasional Jasa & Servis",
                            subtitle = "Booking jadwal, nomor antrean, penugasan teknisi, dan SPK / Job Order",
                            emptyTitle = "Belum Ada Surat Perintah Kerja (SPK)",
                            emptyDescription = "Kelola antrean pelanggan, penugasan mekanik/petugas, dan kalkulasi komisi bagi hasil.",
                            icon = Icons.Default.Work,
                            phaseNumber = "Fase 7",
                            businessName = currentBusiness?.name ?: "",
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    is AppScreen.FeatureManagement -> {
                        FeatureManagementScreen(
                            business = currentBusiness,
                            featureFlags = featureFlags,
                            onToggleFeature = { key, enabled -> viewModel.toggleFeature(key, enabled) },
                            onResetDefaults = { viewModel.resetFeaturesToDefault() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    is AppScreen.BusinessProfile -> {
                        BusinessProfileScreen(
                            business = currentBusiness,
                            onSave = { updated -> viewModel.updateBusinessProfile(updated) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    else -> {
                        DashboardScreen(
                            business = currentBusiness,
                            onOpenPos = { viewModel.navigateTo(AppScreen.PosCashier) },
                            onOpenProducts = { viewModel.navigateTo(AppScreen.Products()) },
                            onOpenFinance = { viewModel.navigateTo(AppScreen.Finance()) },
                            onOpenFeatureSettings = { viewModel.navigateTo(AppScreen.FeatureManagement) }
                        )
                    }
                }
            }
        }
    }

    // Multi-tenant Workspace Switcher Dialog
    if (isWorkspaceDialogOpen) {
        WorkspaceSwitcherDialog(
            businesses = userBusinesses,
            currentBusinessId = currentBusiness?.id,
            onSelectBusiness = { bizId -> viewModel.switchBusiness(bizId) },
            onAddNewBusiness = { viewModel.navigateTo(AppScreen.BusinessSetup) },
            onDismiss = { viewModel.closeWorkspaceDialog() }
        )
    }
}
