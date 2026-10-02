package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ProductVariantDao
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductVariantEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

data class VariantInput(
    val id: String? = null,
    val variantName: String,
    val sku: String? = null,
    val barcode: String? = null,
    val purchasePrice: Long = 0L,
    val sellingPrice: Long = 0L
)

class ProductRepository(
    private val database: AppDatabase,
    private val productDao: ProductDao,
    private val productVariantDao: ProductVariantDao,
    private val categoryDao: CategoryDao
) {
    fun getProductsFlow(businessId: String): Flow<List<ProductEntity>> {
        return productDao.getProductsForBusiness(businessId)
    }

    fun getVariantsForProductFlow(productId: String): Flow<List<ProductVariantEntity>> {
        return productVariantDao.getVariantsForProduct(productId)
    }

    suspend fun getVariantsForProductList(productId: String): List<ProductVariantEntity> = withContext(Dispatchers.IO) {
        productVariantDao.getVariantsForProductList(productId)
    }

    fun getVariantsForBusinessFlow(businessId: String): Flow<List<ProductVariantEntity>> {
        return productVariantDao.getVariantsForBusiness(businessId)
    }

    suspend fun getProductById(id: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    /**
     * Automatically generates the next unique product code based on category prefix:
     * Format: PREFIX + 8 digit sequence (e.g. BEV00000001, BEV00000002)
     */
    suspend fun generateNextProductCode(businessId: String, prefix: String): String = withContext(Dispatchers.IO) {
        val cleanPrefix = prefix.trim().uppercase()
        val pattern = "$cleanPrefix%"
        val existingCodes = productDao.getProductCodesStartingWith(businessId, pattern)

        var maxSequence = 0
        for (code in existingCodes) {
            if (code.startsWith(cleanPrefix)) {
                val numPart = code.substring(cleanPrefix.length)
                val seq = numPart.toIntOrNull()
                if (seq != null && seq > maxSequence) {
                    maxSequence = seq
                }
            }
        }

        var candidateSeq = maxSequence + 1
        var candidateCode = "$cleanPrefix%08d".format(candidateSeq)

        // Ensure collision safety
        while (productDao.getProductByCode(businessId, candidateCode) != null) {
            candidateSeq++
            candidateCode = "$cleanPrefix%08d".format(candidateSeq)
        }

        candidateCode
    }

    /**
     * Generates a unique 13-digit internal business barcode (prefix 200).
     */
    suspend fun generateInternalBarcode(businessId: String): String = withContext(Dispatchers.IO) {
        while (true) {
            // EAN-13 in-store internal range starts with 200
            val randomPart = Random.nextLong(100000000L, 999999999L)
            val candidate = "200$randomPart"

            val existsInProduct = productDao.getProductByBarcode(businessId, candidate) != null
            val existsInVariant = productVariantDao.getVariantByBarcode(businessId, candidate) != null

            if (!existsInProduct && !existsInVariant) {
                return@withContext candidate
            }
        }
        @Suppress("UNREACHABLE_CODE")
        ""
    }

    suspend fun createProduct(
        businessId: String,
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
        variants: List<VariantInput> = emptyList()
    ): Result<ProductEntity> = withContext(Dispatchers.IO) {
        val cleanName = productName.trim()
        val cleanSku = sku?.trim()?.ifBlank { null }
        val cleanBarcode = barcode?.trim()?.ifBlank { null }

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama produk wajib diisi"))
        }

        val category = categoryDao.getCategoryById(categoryId)
            ?: return@withContext Result.failure(IllegalArgumentException("Kategori tidak valid"))

        // Validate SKU uniqueness within business
        if (cleanSku != null) {
            val existingSkuProduct = productDao.getProductBySku(businessId, cleanSku)
            val existingSkuVariant = productVariantDao.getVariantBySku(businessId, cleanSku)
            if (existingSkuProduct != null || existingSkuVariant != null) {
                return@withContext Result.failure(IllegalStateException("SKU '$cleanSku' sudah terdaftar dalam usaha ini"))
            }
        }

        // Validate Barcode uniqueness within business
        if (cleanBarcode != null) {
            val existingBarcodeProduct = productDao.getProductByBarcode(businessId, cleanBarcode)
            val existingBarcodeVariant = productVariantDao.getVariantByBarcode(businessId, cleanBarcode)
            if (existingBarcodeProduct != null || existingBarcodeVariant != null) {
                return@withContext Result.failure(IllegalStateException("Barcode '$cleanBarcode' sudah terdaftar dalam usaha ini"))
            }
        }

        // Validate variants uniqueness among themselves and against DB
        val variantSkus = variants.mapNotNull { it.sku?.trim()?.ifBlank { null } }
        if (variantSkus.size != variantSkus.distinct().size) {
            return@withContext Result.failure(IllegalArgumentException("Terdapat duplikasi SKU di antara varian yang dimasukkan"))
        }

        for (vSku in variantSkus) {
            if (vSku == cleanSku) {
                return@withContext Result.failure(IllegalArgumentException("SKU varian '$vSku' tidak boleh sama dengan SKU produk induk"))
            }
            if (productDao.getProductBySku(businessId, vSku) != null || productVariantDao.getVariantBySku(businessId, vSku) != null) {
                return@withContext Result.failure(IllegalStateException("SKU varian '$vSku' sudah digunakan di usaha ini"))
            }
        }

        val variantBarcodes = variants.mapNotNull { it.barcode?.trim()?.ifBlank { null } }
        for (vBc in variantBarcodes) {
            if (vBc == cleanBarcode) {
                return@withContext Result.failure(IllegalArgumentException("Barcode varian tidak boleh sama dengan barcode induk"))
            }
            if (productDao.getProductByBarcode(businessId, vBc) != null || productVariantDao.getVariantByBarcode(businessId, vBc) != null) {
                return@withContext Result.failure(IllegalStateException("Barcode varian '$vBc' sudah digunakan di usaha ini"))
            }
        }

        // Generate product code
        val generatedCode = generateNextProductCode(businessId, category.prefix)
        val now = System.currentTimeMillis()
        val productId = UUID.randomUUID().toString()

        val product = ProductEntity(
            id = productId,
            businessId = businessId,
            categoryId = categoryId,
            productCode = generatedCode,
            productName = cleanName,
            sku = cleanSku,
            barcode = cleanBarcode,
            unit = unit.trim().ifBlank { "pcs" },
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            minStock = minStock,
            trackStock = trackStock,
            allowNegativeStock = allowNegativeStock,
            description = description.trim(),
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val variantEntities = variants.map { v ->
            ProductVariantEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                productId = productId,
                variantName = v.variantName.trim(),
                sku = v.sku?.trim()?.ifBlank { null },
                barcode = v.barcode?.trim()?.ifBlank { null },
                purchasePrice = v.purchasePrice,
                sellingPrice = v.sellingPrice,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }

        database.withTransaction {
            productDao.insertProduct(product)
            if (variantEntities.isNotEmpty()) {
                productVariantDao.insertVariants(variantEntities)
            }
            if (trackStock && initialStock > 0) {
                val stockBalance = com.example.data.local.entity.StockBalanceEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    productId = productId,
                    variantId = null,
                    quantity = initialStock,
                    updatedAt = now
                )
                database.inventoryDao().insertOrUpdateBalance(stockBalance)

                val stockMovement = com.example.data.local.entity.StockMovementEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    productId = productId,
                    variantId = null,
                    type = "IN",
                    quantity = initialStock,
                    balanceBefore = 0,
                    balanceAfter = initialStock,
                    referenceType = "INITIAL",
                    referenceId = null,
                    notes = "Stok Awal Produk Baru",
                    createdAt = now
                )
                database.inventoryDao().insertStockMovement(stockMovement)
            }
        }

        Result.success(product)
    }

    suspend fun updateProduct(
        product: ProductEntity,
        variants: List<VariantInput> = emptyList()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanName = product.productName.trim()
        val cleanSku = product.sku?.trim()?.ifBlank { null }
        val cleanBarcode = product.barcode?.trim()?.ifBlank { null }

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nama produk tidak boleh kosong"))
        }

        // Validate SKU uniqueness
        if (cleanSku != null) {
            val existingProduct = productDao.getProductBySku(product.businessId, cleanSku)
            if (existingProduct != null && existingProduct.id != product.id) {
                return@withContext Result.failure(IllegalStateException("SKU '$cleanSku' sudah terdaftar pada produk lain"))
            }
            val existingVariant = productVariantDao.getVariantBySku(product.businessId, cleanSku)
            if (existingVariant != null && existingVariant.productId != product.id) {
                return@withContext Result.failure(IllegalStateException("SKU '$cleanSku' sudah terdaftar pada varian lain"))
            }
        }

        // Validate Barcode uniqueness
        if (cleanBarcode != null) {
            val existingProduct = productDao.getProductByBarcode(product.businessId, cleanBarcode)
            if (existingProduct != null && existingProduct.id != product.id) {
                return@withContext Result.failure(IllegalStateException("Barcode '$cleanBarcode' sudah terdaftar pada produk lain"))
            }
            val existingVariant = productVariantDao.getVariantByBarcode(product.businessId, cleanBarcode)
            if (existingVariant != null && existingVariant.productId != product.id) {
                return@withContext Result.failure(IllegalStateException("Barcode '$cleanBarcode' sudah terdaftar pada varian lain"))
            }
        }

        val now = System.currentTimeMillis()
        val updatedProduct = product.copy(
            productName = cleanName,
            sku = cleanSku,
            barcode = cleanBarcode,
            updatedAt = now
        )

        val variantEntities = variants.map { v ->
            ProductVariantEntity(
                id = v.id ?: UUID.randomUUID().toString(),
                businessId = product.businessId,
                productId = product.id,
                variantName = v.variantName.trim(),
                sku = v.sku?.trim()?.ifBlank { null },
                barcode = v.barcode?.trim()?.ifBlank { null },
                purchasePrice = v.purchasePrice,
                sellingPrice = v.sellingPrice,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }

        database.withTransaction {
            productDao.updateProduct(updatedProduct)
            productVariantDao.deleteVariantsByProduct(product.id)
            if (variantEntities.isNotEmpty()) {
                productVariantDao.insertVariants(variantEntities)
            }
        }

        Result.success(Unit)
    }

    suspend fun toggleProductStatus(productId: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        productDao.updateProductStatus(productId, isActive)
        Result.success(Unit)
    }

    suspend fun toggleVariantStatus(variantId: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        productVariantDao.updateVariantStatus(variantId, isActive)
        Result.success(Unit)
    }
}
