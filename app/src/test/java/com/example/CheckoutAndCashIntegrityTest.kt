package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.PayableEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import com.example.data.local.entity.StockBalanceEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.repository.CartItem
import com.example.data.repository.PayableRepository
import com.example.data.repository.PosRepository
import com.example.data.repository.PurchaseItemInput
import com.example.data.repository.PurchaseRepository
import com.example.domain.model.BusinessType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CheckoutAndCashIntegrityTest {

    private lateinit var db: AppDatabase
    private lateinit var posRepo: PosRepository
    private lateinit var purchaseRepo: PurchaseRepository
    private lateinit var payableRepo: PayableRepository

    private val businessA = "biz-A"
    private val businessB = "biz-B"
    private val categoryA = "cat-A"
    private val categoryB = "cat-B"

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        posRepo = PosRepository(
            database = db,
            posDao = db.posDao(),
            inventoryDao = db.inventoryDao(),
            cashDao = db.cashDao(),
            productDao = db.productDao(),
            productVariantDao = db.productVariantDao(),
            customerDao = db.customerDao()
        )

        purchaseRepo = PurchaseRepository(
            database = db,
            purchaseDao = db.purchaseDao(),
            payableDao = db.payableDao(),
            inventoryDao = db.inventoryDao(),
            productDao = db.productDao(),
            cashDao = db.cashDao(),
            supplierDao = db.supplierDao(),
            productVariantDao = db.productVariantDao()
        )

        payableRepo = PayableRepository(
            database = db,
            payableDao = db.payableDao(),
            cashDao = db.cashDao(),
            purchaseDao = db.purchaseDao(),
            supplierDao = db.supplierDao()
        )

        // Seed Business A and Business B
        db.businessDao().insertBusiness(
            BusinessEntity(
                id = businessA,
                name = "Usaha Toko A",
                type = BusinessType.RETAIL.name,
                address = "Jl. A",
                phone = "08111111",
                email = "tokoa@test.com",
                currency = "IDR",
                createdAt = System.currentTimeMillis()
            )
        )
        db.businessDao().insertBusiness(
            BusinessEntity(
                id = businessB,
                name = "Usaha Toko B",
                type = BusinessType.RETAIL.name,
                address = "Jl. B",
                phone = "08222222",
                email = "tokob@test.com",
                currency = "IDR",
                createdAt = System.currentTimeMillis()
            )
        )

        // Seed default categories
        db.categoryDao().insertCategory(
            CategoryEntity(
                id = categoryA,
                businessId = businessA,
                name = "Kategori A",
                prefix = "KATA",
                businessType = "RETAIL"
            )
        )
        db.categoryDao().insertCategory(
            CategoryEntity(
                id = categoryB,
                businessId = businessB,
                name = "Kategori B",
                prefix = "KATB",
                businessType = "RETAIL"
            )
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    // 1. Duplicate cart rows consolidation and stock validation
    @Test
    fun `test duplicate cart rows are consolidated and stock validation checks total requested quantity`() = runBlocking {
        // Product in Business A with stock = 5
        val productId = "prod-stock-test"
        db.productDao().insertProduct(
            ProductEntity(
                id = productId,
                businessId = businessA,
                categoryId = categoryA,
                productCode = "KATA00000001",
                productName = "Kopi Robusta",
                sellingPrice = 10000L,
                trackStock = true,
                allowNegativeStock = false
            )
        )
        db.inventoryDao().insertOrUpdateBalance(
            StockBalanceEntity(
                id = "bal-1",
                businessId = businessA,
                productId = productId,
                quantity = 5
            )
        )

        // Cart with two duplicate lines for the same product: 3 + 3 = 6 > 5
        val cartItems = listOf(
            CartItem(productId = productId, productName = "Kopi Robusta", unitPrice = 10000L, quantity = 3),
            CartItem(productId = productId, productName = "Kopi Robusta", unitPrice = 10000L, quantity = 3)
        )

        val result = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cartItems,
            paymentMethod = "CASH",
            amountPaid = 60000L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Stok tidak mencukupi") == true)

        // Stock was not decremented
        val balanceAfterFail = db.inventoryDao().getStockBalance(businessA, productId, null)?.quantity ?: 0
        assertEquals(5, balanceAfterFail)

        // Now test when total stock is sufficient (e.g., 2 + 2 = 4 <= 5)
        val validCart = listOf(
            CartItem(productId = productId, productName = "Kopi Robusta", unitPrice = 10000L, quantity = 2),
            CartItem(productId = productId, productName = "Kopi Robusta", unitPrice = 10000L, quantity = 2)
        )
        val validResult = posRepo.processCheckout(
            businessId = businessA,
            cartItems = validCart,
            paymentMethod = "CASH",
            amountPaid = 40000L
        )

        assertTrue(validResult.isSuccess)
        val receipt = validResult.getOrThrow()
        assertEquals(40000L, receipt.sale.totalAmount)
        assertEquals(2, receipt.items.size)
        // Authoritative stock updated to 5 - 4 = 1
        val finalBalance = db.inventoryDao().getStockBalance(businessA, productId, null)?.quantity ?: 0
        assertEquals(1, finalBalance)
    }

    // 2. Reject products belonging to another business (cross-business ownership validation)
    @Test
    fun `test cross-business product is rejected during checkout`() = runBlocking {
        // Product in Business B
        val prodB = "prod-of-biz-b"
        db.productDao().insertProduct(
            ProductEntity(
                id = prodB,
                businessId = businessB,
                categoryId = categoryB,
                productCode = "KATB00000001",
                productName = "Barang Milik B",
                sellingPrice = 15000L,
                trackStock = false
            )
        )

        // Attempt to checkout in Business A
        val cart = listOf(
            CartItem(productId = prodB, productName = "Barang Milik B", unitPrice = 15000L, quantity = 1)
        )

        val result = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "CASH",
            amountPaid = 15000L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("bukan milik usaha aktif") == true)
    }

    // 3. Reject variant belonging to another business or different product
    @Test
    fun `test invalid variant is rejected during checkout`() = runBlocking {
        val prodA = "prod-a-var"
        db.productDao().insertProduct(
            ProductEntity(
                id = prodA,
                businessId = businessA,
                categoryId = categoryA,
                productCode = "KATA00000002",
                productName = "Baju Kaos",
                sellingPrice = 50000L,
                trackStock = false
            )
        )
        // Variant created under Business B
        val variantB = "var-b"
        db.productVariantDao().insertVariant(
            ProductVariantEntity(
                id = variantB,
                productId = prodA,
                businessId = businessB,
                variantName = "Merah XL",
                sellingPrice = 50000L
            )
        )

        val cart = listOf(
            CartItem(productId = prodA, variantId = variantB, productName = "Baju Kaos", unitPrice = 50000L, quantity = 1)
        )

        val result = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "CASH",
            amountPaid = 50000L
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Varian produk tidak valid") == true)
    }

    // 4. CASH checkout creates Cash IN transaction
    @Test
    fun `test CASH checkout creates Cash IN transaction and updates cash balance`() = runBlocking {
        val prod = "prod-cash-in"
        db.productDao().insertProduct(
            ProductEntity(
                id = prod,
                businessId = businessA,
                categoryId = categoryA,
                productCode = "KATA00000003",
                productName = "Teh Manis",
                sellingPrice = 5000L,
                trackStock = false
            )
        )

        val cart = listOf(
            CartItem(productId = prod, productName = "Teh Manis", unitPrice = 5000L, quantity = 4)
        )

        val initialCash = posRepo.getCashBalance(businessA)
        assertEquals(0L, initialCash)

        val result = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "CASH",
            amountPaid = 25000L // 20k total, 5k change
        )

        assertTrue(result.isSuccess)
        val receipt = result.getOrThrow()
        assertEquals(20000L, receipt.sale.totalAmount)
        assertEquals(5000L, receipt.payment.changeAmount)
        assertNotNull(receipt.cashTransaction)
        assertEquals("IN", receipt.cashTransaction?.type)
        assertEquals(20000L, receipt.cashTransaction?.amount)

        val finalCash = posRepo.getCashBalance(businessA)
        assertEquals(20000L, finalCash)
    }

    // 5. Non-CASH checkout does NOT create Cash IN and does NOT modify cash balance
    @Test
    fun `test non-CASH checkout does not create Cash IN transaction`() = runBlocking {
        val prod = "prod-qris"
        db.productDao().insertProduct(
            ProductEntity(
                id = prod,
                businessId = businessA,
                categoryId = categoryA,
                productCode = "KATA00000004",
                productName = "Kopi Susu",
                sellingPrice = 18000L,
                trackStock = false
            )
        )

        val cart = listOf(
            CartItem(productId = prod, productName = "Kopi Susu", unitPrice = 18000L, quantity = 2)
        )

        // Seed 10,000 cash balance first
        db.cashDao().insertCashTransaction(
            CashTransactionEntity(
                id = "seed-cash",
                businessId = businessA,
                type = "IN",
                category = "MODAL AWAL",
                amount = 10000L,
                referenceType = "MANUAL",
                referenceId = "1"
            )
        )
        assertEquals(10000L, posRepo.getCashBalance(businessA))

        val result = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "QRIS",
            amountPaid = 36000L
        )

        assertTrue(result.isSuccess)
        val receipt = result.getOrThrow()
        assertNull(receipt.cashTransaction)
        assertEquals("QRIS", receipt.payment.paymentMethod)

        // Cash balance remains 10,000 (NOT changed by QRIS)
        val cashAfter = posRepo.getCashBalance(businessA)
        assertEquals(10000L, cashAfter)
    }

    // 6. Payment reference is preserved when provided, null when absent, never fabricated
    @Test
    fun `test payment reference integrity without fake references`() = runBlocking {
        val prod = "prod-ref-test"
        db.productDao().insertProduct(
            ProductEntity(
                id = prod,
                businessId = businessA,
                categoryId = categoryA,
                productCode = "KATA00000005",
                productName = "Snack",
                sellingPrice = 10000L,
                trackStock = false
            )
        )
        val cart = listOf(CartItem(productId = prod, productName = "Snack", unitPrice = 10000L, quantity = 1))

        // Case A: Null reference passed
        val resA = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "BANK_TRANSFER",
            amountPaid = 10000L,
            paymentReference = null
        )
        assertTrue(resA.isSuccess)
        assertNull(resA.getOrThrow().payment.referenceNumber)

        // Case B: Blank string passed -> becomes null
        val resB = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "QRIS",
            amountPaid = 10000L,
            paymentReference = "   "
        )
        assertTrue(resB.isSuccess)
        assertNull(resB.getOrThrow().payment.referenceNumber)

        // Case C: User-provided reference is accurately recorded
        val resC = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "BANK_TRANSFER",
            amountPaid = 10000L,
            paymentReference = "BCA-987654321"
        )
        assertTrue(resC.isSuccess)
        assertEquals("BCA-987654321", resC.getOrThrow().payment.referenceNumber)
    }

    // 7. CASH payable payment reduces cash (Cash OUT)
    @Test
    fun `test CASH payable payment reduces cash balance`() = runBlocking {
        // Initial cash = 100,000
        db.cashDao().insertCashTransaction(
            CashTransactionEntity(
                id = "init-cash",
                businessId = businessA,
                type = "IN",
                category = "MODAL",
                amount = 100000L,
                referenceType = "MANUAL",
                referenceId = "1"
            )
        )

        val payable = payableRepo.create(
            businessId = businessA,
            purchaseId = null,
            supplierId = null,
            supplierName = "Supplier Jaya",
            amount = 50000L,
            paidAmount = 0L
        ).getOrThrow()

        val payResult = payableRepo.pay(
            businessId = businessA,
            id = payable.id,
            amount = 30000L,
            paymentMethod = "CASH"
        )

        assertTrue(payResult.isSuccess)
        val updated = payResult.getOrThrow()
        assertEquals(30000L, updated.paidAmount)
        assertEquals("PARTIAL", updated.status)

        // Cash was reduced from 100,000 by 30,000 to 70,000
        val finalCash = posRepo.getCashBalance(businessA)
        assertEquals(70000L, finalCash)
    }

    // 8. Non-CASH payable payment does NOT reduce cash balance
    @Test
    fun `test non-CASH payable payment does not reduce cash balance`() = runBlocking {
        // Initial cash = 100,000
        db.cashDao().insertCashTransaction(
            CashTransactionEntity(
                id = "init-cash-2",
                businessId = businessA,
                type = "IN",
                category = "MODAL",
                amount = 100000L,
                referenceType = "MANUAL",
                referenceId = "1"
            )
        )

        val payable = payableRepo.create(
            businessId = businessA,
            purchaseId = null,
            supplierId = null,
            supplierName = "Supplier Sentosa",
            amount = 50000L,
            paidAmount = 0L
        ).getOrThrow()

        val payResult = payableRepo.pay(
            businessId = businessA,
            id = payable.id,
            amount = 30000L,
            paymentMethod = "BANK_TRANSFER"
        )

        assertTrue(payResult.isSuccess)
        assertEquals(30000L, payResult.getOrThrow().paidAmount)

        // Cash balance remains 100,000
        val finalCash = posRepo.getCashBalance(businessA)
        assertEquals(100000L, finalCash)
    }

    // 9. Synchronize purchase payment status when paying payable
    @Test
    fun `test purchase paidAmount and paymentStatus sync on payable payment`() = runBlocking {
        // Supplier and Product in Business A
        val supplier = SupplierEntity(id = "supp-sync", businessId = businessA, name = "PT Sumber Makmur")
        db.supplierDao().insert(supplier)

        val product = ProductEntity(
            id = "prod-sync",
            businessId = businessA,
            categoryId = categoryA,
            productCode = "KATA00000006",
            productName = "Beras 5kg",
            sellingPrice = 60000L
        )
        db.productDao().insertProduct(product)

        // Create purchase: 10 items @ 40,000 = 400,000 total. Paid: 100,000 (status: PARTIAL)
        val purchaseResult = purchaseRepo.createPurchase(
            businessId = businessA,
            supplierId = supplier.id,
            supplierName = supplier.name,
            invoiceNumber = "PO-SYNC-001",
            items = listOf(PurchaseItemInput(productId = product.id, productName = product.productName, quantity = 10, unitCost = 40000L)),
            paidAmount = 100000L,
            paymentMethod = "CASH"
        )
        assertTrue(purchaseResult.isSuccess)
        val purchase = purchaseResult.getOrThrow()
        assertEquals("PARTIAL", purchase.paymentStatus)
        assertEquals(100000L, purchase.paidAmount)
        assertEquals(400000L, purchase.totalAmount)

        // Find the generated payable for remaining 300,000
        val unpaidPayables = payableRepo.getUnpaidFlow(businessA).first()
        val relatedPayable = unpaidPayables.first { it.purchaseId == purchase.id }
        assertEquals(300000L, relatedPayable.amount)
        assertEquals(0L, relatedPayable.paidAmount)

        // Pay 150,000 on the payable
        val pay1 = payableRepo.pay(
            businessId = businessA,
            id = relatedPayable.id,
            amount = 150000L,
            paymentMethod = "BANK_TRANSFER"
        )
        assertTrue(pay1.isSuccess)

        // Verify purchase is synchronized to 100k + 150k = 250k, still PARTIAL
        val purchaseAfterPay1 = purchaseRepo.getById(businessA, purchase.id)
        assertNotNull(purchaseAfterPay1)
        assertEquals(250000L, purchaseAfterPay1!!.paidAmount)
        assertEquals("PARTIAL", purchaseAfterPay1.paymentStatus)

        // Pay remaining 150,000 on the payable
        val pay2 = payableRepo.pay(
            businessId = businessA,
            id = relatedPayable.id,
            amount = 150000L,
            paymentMethod = "CASH"
        )
        assertTrue(pay2.isSuccess)
        val fullyPaidPayable = pay2.getOrThrow()
        assertEquals("PAID", fullyPaidPayable.status)
        assertEquals(300000L, fullyPaidPayable.paidAmount)

        // Verify purchase is now fully PAID (400k)
        val purchaseAfterPay2 = purchaseRepo.getById(businessA, purchase.id)
        assertNotNull(purchaseAfterPay2)
        assertEquals(400000L, purchaseAfterPay2!!.paidAmount)
        assertEquals("PAID", purchaseAfterPay2.paymentStatus)

        // Purchase no longer appears in unpaid purchases list
        val unpaidList = purchaseRepo.getUnpaidFlow(businessA).first()
        assertFalse(unpaidList.any { it.id == purchase.id })
    }

    // 10. Overpayment prevention on payable
    @Test
    fun `test overpayment prevention on payable`() = runBlocking {
        val payable = payableRepo.create(
            businessId = businessA,
            purchaseId = null,
            supplierId = null,
            supplierName = "Supplier ABC",
            amount = 50000L,
            paidAmount = 40000L
        ).getOrThrow()

        // Remaining is 10,000. Paying 15,000 must fail.
        val overpayResult = payableRepo.pay(
            businessId = businessA,
            id = payable.id,
            amount = 15000L,
            paymentMethod = "CASH"
        )
        assertTrue(overpayResult.isFailure)
        assertTrue(overpayResult.exceptionOrNull()?.message?.contains("Pembayaran melebihi sisa utang") == true)

        // Pay exact 10,000 succeeds
        val payExact = payableRepo.pay(
            businessId = businessA,
            id = payable.id,
            amount = 10000L,
            paymentMethod = "CASH"
        )
        assertTrue(payExact.isSuccess)
        assertEquals("PAID", payExact.getOrThrow().status)

        // Paying an already paid payable must fail
        val payAlreadyPaid = payableRepo.pay(
            businessId = businessA,
            id = payable.id,
            amount = 5000L,
            paymentMethod = "CASH"
        )
        assertTrue(payAlreadyPaid.isFailure)
        assertTrue(payAlreadyPaid.exceptionOrNull()?.message?.contains("Utang sudah lunas") == true)
    }

    // 11. Supplier and product tenant validation in purchase creation
    @Test
    fun `test supplier and product tenant validation in purchase creation`() = runBlocking {
        // Supplier and Product belong to Business B
        val suppB = SupplierEntity(id = "supp-biz-b", businessId = businessB, name = "Supplier B")
        db.supplierDao().insert(suppB)

        val prodB = ProductEntity(
            id = "prod-biz-b",
            businessId = businessB,
            categoryId = categoryB,
            productCode = "KATB00000002",
            productName = "Produk B",
            sellingPrice = 20000L
        )
        db.productDao().insertProduct(prodB)

        // Product in Business A
        val prodA = ProductEntity(
            id = "prod-biz-a",
            businessId = businessA,
            categoryId = categoryA,
            productCode = "KATA00000007",
            productName = "Produk A",
            sellingPrice = 10000L
        )
        db.productDao().insertProduct(prodA)

        // Attempt purchase in Business A with Supplier B -> Must fail
        val resSuppFail = purchaseRepo.createPurchase(
            businessId = businessA,
            supplierId = suppB.id,
            supplierName = suppB.name,
            invoiceNumber = "INV-CROSS-1",
            items = listOf(PurchaseItemInput(productId = prodA.id, productName = prodA.productName, quantity = 1, unitCost = 5000L))
        )
        assertTrue(resSuppFail.isFailure)
        assertTrue(resSuppFail.exceptionOrNull()?.message?.contains("Supplier tidak ditemukan atau bukan milik usaha aktif") == true)

        // Attempt purchase in Business A with Product B -> Must fail
        val resProdFail = purchaseRepo.createPurchase(
            businessId = businessA,
            supplierId = null,
            supplierName = "Supplier Umum",
            invoiceNumber = "INV-CROSS-2",
            items = listOf(PurchaseItemInput(productId = prodB.id, productName = prodB.productName, quantity = 1, unitCost = 15000L))
        )
        assertTrue(resProdFail.isFailure)
        assertTrue(resProdFail.exceptionOrNull()?.message?.contains("tidak ditemukan atau bukan milik usaha aktif") == true)
    }

    // 12. Non-CASH purchase creation does not create Cash OUT transaction
    @Test
    fun `test non-CASH purchase creation does not reduce cash balance`() = runBlocking {
        val prod = ProductEntity(
            id = "prod-po-cash",
            businessId = businessA,
            categoryId = categoryA,
            productCode = "KATA00000008",
            productName = "Gula Pasir",
            sellingPrice = 15000L
        )
        db.productDao().insertProduct(prod)

        // Initial cash = 100,000
        db.cashDao().insertCashTransaction(
            CashTransactionEntity(
                id = "init-cash-po",
                businessId = businessA,
                type = "IN",
                category = "MODAL",
                amount = 100000L,
                referenceType = "MANUAL",
                referenceId = "1"
            )
        )

        // Create purchase paid with BANK_TRANSFER
        val result = purchaseRepo.createPurchase(
            businessId = businessA,
            supplierId = null,
            supplierName = "Supplier Gula",
            invoiceNumber = "PO-TRF-01",
            items = listOf(PurchaseItemInput(productId = prod.id, productName = prod.productName, quantity = 2, unitCost = 12000L)),
            paidAmount = 24000L,
            paymentMethod = "BANK_TRANSFER"
        )

        assertTrue(result.isSuccess)
        // Cash balance remains 100,000 (NOT reduced because paymentMethod was BANK_TRANSFER)
        val cash = posRepo.getCashBalance(businessA)
        assertEquals(100000L, cash)

        // Now create a purchase paid with CASH
        val cashResult = purchaseRepo.createPurchase(
            businessId = businessA,
            supplierId = null,
            supplierName = "Supplier Gula",
            invoiceNumber = "PO-CASH-01",
            items = listOf(PurchaseItemInput(productId = prod.id, productName = prod.productName, quantity = 2, unitCost = 12000L)),
            paidAmount = 24000L,
            paymentMethod = "CASH"
        )
        assertTrue(cashResult.isSuccess)
        // Cash balance is reduced from 100,000 to 76,000
        val cashAfter = posRepo.getCashBalance(businessA)
        assertEquals(76000L, cashAfter)
    }

    // 13. Double checkout protection prevents concurrent checkouts
    @Test
    fun `test double checkout protection prevents concurrent checkouts`() = runBlocking {
        val prod = ProductEntity(
            id = "prod-double-guard",
            businessId = businessA,
            categoryId = categoryA,
            productCode = "KATA00000009",
            productName = "Barang Guard",
            sellingPrice = 10000L,
            trackStock = false
        )
        db.productDao().insertProduct(prod)

        val cart = listOf(CartItem(productId = prod.id, productName = prod.productName, unitPrice = 10000L, quantity = 1))

        val field = PosRepository::class.java.getDeclaredField("isCheckingOut").apply { isAccessible = true }
        val atomicGuard = field.get(posRepo) as java.util.concurrent.atomic.AtomicBoolean

        // Simulate checkout currently in flight
        atomicGuard.set(true)

        val blockedCall = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "CASH",
            amountPaid = 10000L
        )

        assertTrue(blockedCall.isFailure)
        assertTrue(blockedCall.exceptionOrNull()?.message?.contains("sedang diproses") == true)

        // Reset guard
        atomicGuard.set(false)

        val normalCall = posRepo.processCheckout(
            businessId = businessA,
            cartItems = cart,
            paymentMethod = "CASH",
            amountPaid = 10000L
        )
        assertTrue(normalCall.isSuccess)
    }
}
