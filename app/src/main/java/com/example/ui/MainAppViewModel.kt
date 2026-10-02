package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.FeatureFlagEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.BusinessRepository
import com.example.data.repository.CartItem
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CheckoutReceipt
import com.example.data.repository.CustomerRepository
import com.example.data.repository.FeatureToggleRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.PosRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.VariantInput
import com.example.domain.model.BusinessType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppScreen(val route: String, val title: String) {
    // Auth & Onboarding
    data object Auth : AppScreen("auth", "Autentikasi")
    data object BusinessSetup : AppScreen("business_setup", "Setup Usaha Baru")

    // Core Shell
    data object Dashboard : AppScreen("dashboard", "Dashboard Ringkasan")
    data object PosCashier : AppScreen("pos", "POS / Kasir")

    // Produk & Stok
    data class Products(val subTab: String = "all") : AppScreen("products", "Produk & Inventaris")
    data class Inventory(val subTab: String = "balance") : AppScreen("inventory", "Stok & Opname")

    // Penjualan
    data class Sales(val subTab: String = "sales") : AppScreen("sales", "Riwayat Penjualan")

    // Pembelian
    data class Purchasing(val subTab: String = "purchases") : AppScreen("purchasing", "Pembelian & Hutang")

    // Keuangan
    data class Finance(val subTab: String = "cash") : AppScreen("finance", "Keuangan & Kas")

    // Laporan
    data object Reports : AppScreen("reports", "Laporan Usaha")

    // Business Modules (Modular Toggles)
    data class FnbModule(val tab: String = "tables") : AppScreen("fnb", "Modul F&B")
    data class RetailModule(val tab: String = "variants") : AppScreen("retail", "Modul Retail")
    data class ServiceModule(val tab: String = "booking") : AppScreen("service", "Modul Layanan & Jasa")

    // Settings
    data object FeatureManagement : AppScreen("feature_settings", "Fitur & Modul Usaha")
    data object BusinessProfile : AppScreen("business_profile", "Profil Usaha")
}

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val authRepo = AuthRepository(db.userDao(), db.activeSessionDao())
    private val businessRepo = BusinessRepository(db.businessDao(), db.businessUserDao(), db.featureFlagDao(), db.activeSessionDao())
    private val featureRepo = FeatureToggleRepository(db.featureFlagDao())
    private val categoryRepo = CategoryRepository(db.categoryDao())
    private val productRepo = ProductRepository(db, db.productDao(), db.productVariantDao(), db.categoryDao())
    private val inventoryRepo = InventoryRepository(db, db.inventoryDao(), db.productDao())
    private val posRepo = PosRepository(db, db.posDao(), db.inventoryDao(), db.cashDao(), db.productDao())
    private val customerRepo = CustomerRepository(db.customerDao())

    // Active session observation
    val activeSession = authRepo.activeSession.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Current User
    val currentUser: StateFlow<UserEntity?> = activeSession.flatMapLatest { session ->
        val userId = session?.currentUserId
        if (userId != null) authRepo.getUserByIdFlow(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current User's Businesses
    val userBusinesses: StateFlow<List<BusinessEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) businessRepo.getUserBusinessesFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Business
    val currentBusiness: StateFlow<BusinessEntity?> = activeSession.flatMapLatest { session ->
        val businessId = session?.currentBusinessId
        if (businessId != null) businessRepo.getBusinessByIdFlow(businessId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Feature Flags for current business: Map<Key, isEnabled>
    val featureFlags: StateFlow<Map<String, Boolean>> = currentBusiness.flatMapLatest { business ->
        if (business != null) {
            featureRepo.getFeatureFlagsFlow(business.id)
        } else {
            flowOf(emptyList())
        }
    }.combine(flowOf(Unit)) { flags, _ ->
        flags.associate { it.featureKey to it.isEnabled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Categories for current business
    val categories: StateFlow<List<CategoryEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) categoryRepo.getCategoriesFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCategories: StateFlow<List<CategoryEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) categoryRepo.getActiveCategoriesFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products for current business
    val products: StateFlow<List<ProductEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) productRepo.getProductsFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Variants for current business
    val variants: StateFlow<List<ProductVariantEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) productRepo.getVariantsForBusinessFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // PHASE 3: Inventory Balances & Movements
    val stockBalances: StateFlow<List<StockBalanceEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) inventoryRepo.getAllStockBalancesFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Convenient map of stock: key is either productId (for main) or "${productId}_${variantId}"
    val stockBalancesMap: StateFlow<Map<String, Int>> = stockBalances.map { list ->
        list.associate { b ->
            val key = if (b.variantId != null) "${b.productId}_${b.variantId}" else b.productId
            key to b.quantity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val stockMovements: StateFlow<List<StockMovementEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) inventoryRepo.getMovementsFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // PHASE 4: Sales, Cash Balance & Customers
    val sales: StateFlow<List<SaleEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) posRepo.getSalesFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashBalance: StateFlow<Long> = currentBusiness.flatMapLatest { business ->
        if (business != null) posRepo.getCashBalanceFlow(business.id) else flowOf(0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val customers: StateFlow<List<CustomerEntity>> = currentBusiness.flatMapLatest { business ->
        if (business != null) customerRepo.getCustomersFlow(business.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _cartDiscount = MutableStateFlow<Long>(0L)
    val cartDiscount: StateFlow<Long> = _cartDiscount.asStateFlow()

    private val _lastReceipt = MutableStateFlow<CheckoutReceipt?>(null)
    val lastReceipt: StateFlow<CheckoutReceipt?> = _lastReceipt.asStateFlow()

    // Current screen navigation
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation BackStack
    private val screenBackStack = mutableListOf<AppScreen>()

    // Workspace Dialog state
    private val _isWorkspaceDialogOpen = MutableStateFlow(false)
    val isWorkspaceDialogOpen: StateFlow<Boolean> = _isWorkspaceDialogOpen.asStateFlow()

    // UI Toasts / Messages
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        if (addToBackStack && _currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        if (_currentScreen.value != AppScreen.Dashboard) {
            _currentScreen.value = AppScreen.Dashboard
            return true
        }
        return false
    }

    fun openWorkspaceDialog() {
        _isWorkspaceDialogOpen.value = true
    }

    fun closeWorkspaceDialog() {
        _isWorkspaceDialogOpen.value = false
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(message = msg, isError = isError)
    }

    // ==========================================
    // AUTHENTICATION & BUSINESS SETUP
    // ==========================================

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.login(email, pass)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Selamat datang kembali, ${it.fullName}!")
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal masuk", isError = true)
            }
        }
    }

    fun register(fullName: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.register(fullName, email, phone, pass)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Pendaftaran berhasil! Silakan setup profil usaha Anda.")
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal mendaftar", isError = true)
            }
        }
    }

    fun resetPassword(email: String, newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.resetPassword(email, newPass)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Kata sandi berhasil diatur ulang. Silakan masuk.")
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal reset kata sandi", isError = true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepo.logout()
            _isLoading.value = false
            screenBackStack.clear()
            _currentScreen.value = AppScreen.Auth
            showMessage("Anda telah keluar dari UsahaOS.")
        }
    }

    fun createBusiness(
        name: String,
        type: BusinessType,
        address: String,
        phone: String,
        email: String,
        currency: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = businessRepo.createBusiness(
                userId = user.id,
                name = name,
                type = type,
                address = address,
                phone = phone,
                email = email,
                currency = currency
            )
            _isLoading.value = false
            result.onSuccess {
                showMessage("Workspace usaha '${it.name}' berhasil dibuat!")
                _currentScreen.value = AppScreen.Dashboard
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal membuat usaha", isError = true)
            }
        }
    }

    fun switchBusiness(businessId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            businessRepo.switchBusiness(businessId)
            _isLoading.value = false
            _isWorkspaceDialogOpen.value = false
            clearCart()
            showMessage("Berhasil beralih ke workspace usaha baru.")
        }
    }

    // ==========================================
    // FEATURE TOGGLES
    // ==========================================

    fun toggleFeature(featureKey: String, isEnabled: Boolean) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            featureRepo.toggleFeature(biz.id, featureKey, isEnabled)
            val stateName = if (isEnabled) "diaktifkan" else "dinonaktifkan"
            showMessage("Fitur $featureKey $stateName.")
        }
    }

    fun resetFeaturesToDefault() {
        val biz = currentBusiness.value ?: return
        val bizType = BusinessType.fromCode(biz.type)
        viewModelScope.launch {
            featureRepo.resetToDefaults(biz.id, bizType)
            showMessage("Fitur dikembalikan ke standar ${bizType.title}.")
        }
    }

    // ==========================================
    // CATEGORY MANAGEMENT
    // ==========================================

    fun createCategory(
        name: String,
        prefix: String,
        businessType: BusinessType,
        description: String,
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = categoryRepo.createCategory(
                businessId = biz.id,
                name = name,
                prefix = prefix,
                businessType = businessType,
                description = description
            )
            _isLoading.value = false
            result.onSuccess {
                showMessage("Kategori '${it.name}' (${it.prefix}) berhasil dibuat!")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal membuat kategori", isError = true)
            }
        }
    }

    fun updateCategory(
        category: CategoryEntity,
        name: String,
        prefix: String,
        description: String,
        isActive: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = categoryRepo.updateCategory(category, name, prefix, description, isActive)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Kategori '${name}' berhasil diperbarui.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal memperbarui kategori", isError = true)
            }
        }
    }

    fun toggleCategoryStatus(categoryId: String, isActive: Boolean) {
        viewModelScope.launch {
            categoryRepo.toggleCategoryStatus(categoryId, isActive)
            val status = if (isActive) "diaktifkan" else "dinonaktifkan"
            showMessage("Status kategori $status.")
        }
    }

    // ==========================================
    // PRODUCT MANAGEMENT
    // ==========================================

    fun createProduct(
        categoryId: String,
        productName: String,
        sku: String?,
        barcode: String?,
        unit: String,
        purchasePrice: Long,
        sellingPrice: Long,
        minStock: Int,
        trackStock: Boolean,
        allowNegativeStock: Boolean = false,
        description: String,
        initialStock: Int = 0,
        variants: List<VariantInput>,
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = productRepo.createProduct(
                businessId = biz.id,
                categoryId = categoryId,
                productName = productName,
                sku = sku,
                barcode = barcode,
                unit = unit,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                minStock = minStock,
                trackStock = trackStock,
                allowNegativeStock = allowNegativeStock,
                description = description,
                initialStock = initialStock,
                variants = variants
            )
            _isLoading.value = false
            result.onSuccess { prod ->
                showMessage("Produk '${prod.productName}' (${prod.productCode}) berhasil disimpan!")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal menyimpan produk", isError = true)
            }
        }
    }

    fun updateProduct(
        product: ProductEntity,
        variants: List<VariantInput>,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = productRepo.updateProduct(product, variants)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Produk '${product.productName}' berhasil diperbarui.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal memperbarui produk", isError = true)
            }
        }
    }

    fun toggleProductStatus(productId: String, isActive: Boolean) {
        viewModelScope.launch {
            productRepo.toggleProductStatus(productId, isActive)
            val status = if (isActive) "diaktifkan" else "dinonaktifkan"
            showMessage("Status produk $status.")
        }
    }

    fun toggleVariantStatus(variantId: String, isActive: Boolean) {
        viewModelScope.launch {
            productRepo.toggleVariantStatus(variantId, isActive)
            val status = if (isActive) "diaktifkan" else "dinonaktifkan"
            showMessage("Status varian $status.")
        }
    }

    // ==========================================
    // PHASE 3: INVENTORY ACTIONS
    // ==========================================

    fun recordStockIn(
        productId: String,
        variantId: String? = null,
        quantity: Int,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = inventoryRepo.recordStockIn(biz.id, productId, variantId, quantity, notes)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Stok masuk (+$quantity) berhasil dicatat.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal menambah stok", isError = true)
            }
        }
    }

    fun recordStockOut(
        productId: String,
        variantId: String? = null,
        quantity: Int,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = inventoryRepo.recordStockOut(biz.id, productId, variantId, quantity, notes)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Stok keluar (-$quantity) berhasil dicatat.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal mengurangi stok", isError = true)
            }
        }
    }

    fun recordStockAdjustment(
        productId: String,
        variantId: String? = null,
        newQuantity: Int,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = inventoryRepo.recordStockAdjustment(biz.id, productId, variantId, newQuantity, notes)
            _isLoading.value = false
            result.onSuccess {
                showMessage("Penyesuaian stok berhasil disimpan (Stok baru: $newQuantity).")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal menyesuaikan stok", isError = true)
            }
        }
    }

    fun recordStockOpname(
        productId: String,
        variantId: String? = null,
        physicalStock: Int,
        notes: String = "",
        onComplete: () -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = inventoryRepo.recordStockOpname(biz.id, productId, variantId, physicalStock, notes)
            _isLoading.value = false
            result.onSuccess { m ->
                val diffStr = if (m.quantity >= 0) "+${m.quantity}" else "${m.quantity}"
                showMessage("Stock Opname berhasil dikonfirmasi! Selisih stok: $diffStr.")
                onComplete()
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal memproses stock opname", isError = true)
            }
        }
    }

    // ==========================================
    // PHASE 4: POS & CART ACTIONS
    // ==========================================

    fun addToCart(
        product: ProductEntity,
        variant: ProductVariantEntity? = null,
        qty: Int = 1
    ) {
        val items = _cartItems.value.toMutableList()
        val existingIndex = items.indexOfFirst {
            it.productId == product.id && it.variantId == variant?.id
        }

        if (existingIndex >= 0) {
            val existing = items[existingIndex]
            items[existingIndex] = existing.copy(quantity = existing.quantity + qty)
        } else {
            items.add(
                CartItem(
                    productId = product.id,
                    variantId = variant?.id,
                    productName = product.productName,
                    variantName = variant?.variantName,
                    unitPrice = variant?.sellingPrice ?: product.sellingPrice,
                    quantity = qty,
                    discountAmount = 0L,
                    trackStock = product.trackStock,
                    allowNegativeStock = product.allowNegativeStock,
                    barcode = variant?.barcode ?: product.barcode
                )
            )
        }
        _cartItems.value = items
        showMessage("'${product.productName}' dimasukkan ke keranjang")
    }

    fun updateCartItemQuantity(index: Int, newQty: Int) {
        val items = _cartItems.value.toMutableList()
        if (index in items.indices) {
            if (newQty <= 0) {
                items.removeAt(index)
            } else {
                items[index] = items[index].copy(quantity = newQty)
            }
            _cartItems.value = items
        }
    }

    fun removeCartItem(index: Int) {
        val items = _cartItems.value.toMutableList()
        if (index in items.indices) {
            items.removeAt(index)
            _cartItems.value = items
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0L
        _selectedCustomer.value = null
    }

    fun setSelectedCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun setCartDiscount(discount: Long) {
        _cartDiscount.value = discount.coerceAtLeast(0L)
    }

    fun createCustomer(
        name: String,
        phone: String?,
        email: String?,
        address: String?,
        onComplete: (CustomerEntity) -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        viewModelScope.launch {
            val result = customerRepo.createCustomer(biz.id, name, phone, email, address)
            result.onSuccess { cust ->
                _selectedCustomer.value = cust
                showMessage("Pelanggan '${cust.name}' berhasil ditambahkan!")
                onComplete(cust)
            }.onFailure { error ->
                showMessage(error.message ?: "Gagal menambah pelanggan", isError = true)
            }
        }
    }

    fun checkout(
        paymentMethod: String,
        amountPaid: Long,
        notes: String = "",
        onComplete: (CheckoutReceipt) -> Unit = {}
    ) {
        val biz = currentBusiness.value ?: return
        val items = _cartItems.value
        if (items.isEmpty()) {
            showMessage("Keranjang belanja masih kosong", isError = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = posRepo.processCheckout(
                businessId = biz.id,
                cartItems = items,
                customerId = _selectedCustomer.value?.id,
                customerName = _selectedCustomer.value?.name ?: "Pelanggan Umum",
                orderDiscount = _cartDiscount.value,
                taxAmount = 0L,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                notes = notes
            )
            _isLoading.value = false
            result.onSuccess { receipt ->
                _lastReceipt.value = receipt
                clearCart()
                showMessage("Transaksi ${receipt.sale.invoiceNumber} BERHASIL!")
                onComplete(receipt)
            }.onFailure { error ->
                showMessage(error.message ?: "Transaksi checkout gagal", isError = true)
            }
        }
    }

    fun clearLastReceipt() {
        _lastReceipt.value = null
    }

    fun getSaleDetail(
        saleId: String,
        onResult: (SaleEntity?, List<SaleItemEntity>, PaymentEntity?) -> Unit
    ) {
        viewModelScope.launch {
            val sale = posRepo.getSaleById(saleId)
            val items = posRepo.getSaleItems(saleId)
            val payment = posRepo.getPaymentForSale(saleId)
            onResult(sale, items, payment)
        }
    }
}
