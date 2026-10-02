package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.VariantInput
import com.example.domain.model.BusinessType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryRepo: CategoryRepository
    private lateinit var productRepo: ProductRepository

    private val testBusinessId = "biz-test-123"
    private val testBusinessId2 = "biz-test-456"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        categoryRepo = CategoryRepository(db.categoryDao())
        productRepo = ProductRepository(db, db.productDao(), db.productVariantDao(), db.categoryDao())

        runBlocking {
            // Seed a test business
            db.businessDao().insertBusiness(
                BusinessEntity(
                    id = testBusinessId,
                    name = "Toko Berkah Test",
                    type = BusinessType.RETAIL.code,
                    address = "Jl. Sudirman 10",
                    phone = "08123456789",
                    email = "berkah@test.com",
                    currency = "IDR"
                )
            )
            db.businessDao().insertBusiness(
                BusinessEntity(
                    id = testBusinessId2,
                    name = "Kafe Senja Test",
                    type = BusinessType.FNB.code,
                    address = "Jl. Thamrin 20",
                    phone = "08129876543",
                    email = "senja@test.com",
                    currency = "IDR"
                )
            )
        }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UsahaOS", appName)
    }

    @Test
    fun `test category creation and prefix validation`() = runBlocking {
        // Valid category creation
        val result = categoryRepo.createCategory(
            businessId = testBusinessId,
            name = "Minuman",
            prefix = "BEV",
            businessType = "FNB",
            description = "Kategori Aneka Minuman"
        )
        assertTrue(result.isSuccess)
        val category = result.getOrNull()
        assertNotNull(category)
        assertEquals("BEV", category?.prefix)

        // Duplicate prefix in same business must fail
        val duplicateResult = categoryRepo.createCategory(
            businessId = testBusinessId,
            name = "Beverages 2",
            prefix = "BEV",
            businessType = "FNB"
        )
        assertTrue(duplicateResult.isFailure)

        // Same prefix in DIFFERENT business is allowed (Multi-tenant scoped)
        val diffBizResult = categoryRepo.createCategory(
            businessId = testBusinessId2,
            name = "Minuman Kafe",
            prefix = "BEV",
            businessType = "FNB"
        )
        assertTrue(diffBizResult.isSuccess)
    }

    @Test
    fun `test automatic product code generation sequence`() = runBlocking {
        val cat = categoryRepo.createCategory(
            businessId = testBusinessId,
            name = "Makanan",
            prefix = "FOD",
            businessType = "FNB"
        ).getOrThrow()

        // Create first product
        val prod1 = productRepo.createProduct(
            businessId = testBusinessId,
            categoryId = cat.id,
            productName = "Nasi Goreng Spesial",
            sku = "FOD-NASGOR",
            barcode = "8991234567890",
            unit = "porsi",
            purchasePrice = 12000L,
            sellingPrice = 25000L,
            minStock = 5,
            trackStock = true,
            description = "Nasi goreng ayam suwir"
        ).getOrThrow()

        assertEquals("FOD00000001", prod1.productCode)

        // Create second product in same category prefix
        val prod2 = productRepo.createProduct(
            businessId = testBusinessId,
            categoryId = cat.id,
            productName = "Mie Goreng",
            sku = "FOD-MIEGOR",
            barcode = null,
            unit = "porsi",
            purchasePrice = 10000L,
            sellingPrice = 20000L,
            minStock = 5,
            trackStock = true,
            description = ""
        ).getOrThrow()

        assertEquals("FOD00000002", prod2.productCode)
    }

    @Test
    fun `test duplicate sku validation within same business`() = runBlocking {
        val cat = categoryRepo.createCategory(
            businessId = testBusinessId,
            name = "Fashion",
            prefix = "FAS",
            businessType = "RETAIL"
        ).getOrThrow()

        productRepo.createProduct(
            businessId = testBusinessId,
            categoryId = cat.id,
            productName = "T-Shirt Black",
            sku = "FAS-TSH-BLK",
            barcode = null,
            unit = "pcs",
            purchasePrice = 40000L,
            sellingPrice = 85000L,
            minStock = 10,
            trackStock = true,
            description = ""
        ).getOrThrow()

        // Duplicate SKU must fail
        val duplicateSkuResult = productRepo.createProduct(
            businessId = testBusinessId,
            categoryId = cat.id,
            productName = "T-Shirt Black V2",
            sku = "FAS-TSH-BLK",
            barcode = null,
            unit = "pcs",
            purchasePrice = 45000L,
            sellingPrice = 90000L,
            minStock = 5,
            trackStock = true,
            description = ""
        )
        assertTrue(duplicateSkuResult.isFailure)
    }

    @Test
    fun `test product variants creation and persistence`() = runBlocking {
        val cat = categoryRepo.createCategory(
            businessId = testBusinessId,
            name = "Kopi",
            prefix = "KOP",
            businessType = "FNB"
        ).getOrThrow()

        val variants = listOf(
            VariantInput(variantName = "Regular (12oz)", sku = "KOP-SUSU-REG", purchasePrice = 6000L, sellingPrice = 15000L),
            VariantInput(variantName = "Large (16oz)", sku = "KOP-SUSU-LRG", purchasePrice = 8000L, sellingPrice = 20000L)
        )

        val product = productRepo.createProduct(
            businessId = testBusinessId,
            categoryId = cat.id,
            productName = "Kopi Susu Gula Aren",
            sku = null,
            barcode = null,
            unit = "cup",
            purchasePrice = 6000L,
            sellingPrice = 15000L,
            minStock = 0,
            trackStock = true,
            description = "Signature drink",
            variants = variants
        ).getOrThrow()

        val savedVariants = productRepo.getVariantsForProductList(product.id)
        assertEquals(2, savedVariants.size)
        assertEquals("Regular (12oz)", savedVariants[0].variantName)
        assertEquals("Large (16oz)", savedVariants[1].variantName)
        assertEquals(20000L, savedVariants[1].sellingPrice)
    }

    @Test
    fun `test internal barcode generation`() = runBlocking {
        val internalBarcode = productRepo.generateInternalBarcode(testBusinessId)
        assertTrue(internalBarcode.startsWith("200"))
        assertEquals(12, internalBarcode.length)
    }
}
