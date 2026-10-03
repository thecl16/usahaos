package com.example.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS suppliers (
                    id TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    phone TEXT,
                    email TEXT,
                    address TEXT,
                    notes TEXT,
                    createdAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_suppliers_businessId ON suppliers(businessId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_suppliers_businessId_name ON suppliers(businessId, name)"
            )

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS purchases (
                    id TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    supplierId TEXT,
                    supplierName TEXT NOT NULL,
                    invoiceNumber TEXT NOT NULL,
                    subtotal INTEGER NOT NULL,
                    discountAmount INTEGER NOT NULL,
                    totalAmount INTEGER NOT NULL,
                    paymentStatus TEXT NOT NULL,
                    paidAmount INTEGER NOT NULL,
                    notes TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE CASCADE,
                    FOREIGN KEY(supplierId) REFERENCES suppliers(id) ON DELETE SET NULL
                )
            """.trimIndent())

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchases_businessId ON purchases(businessId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchases_businessId_supplierId ON purchases(businessId, supplierId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchases_businessId_invoiceNumber ON purchases(businessId, invoiceNumber)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchases_createdAt ON purchases(createdAt)"
            )

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS purchase_items (
                    id TEXT NOT NULL,
                    purchaseId TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    productId TEXT NOT NULL,
                    variantId TEXT,
                    productName TEXT NOT NULL,
                    variantName TEXT,
                    quantity INTEGER NOT NULL,
                    unitCost INTEGER NOT NULL,
                    subtotal INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(purchaseId) REFERENCES purchases(id) ON DELETE CASCADE,
                    FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchase_items_businessId ON purchase_items(businessId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchase_items_purchaseId ON purchase_items(purchaseId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_purchase_items_businessId_productId ON purchase_items(businessId, productId)"
            )

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS payables (
                    id TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    purchaseId TEXT,
                    supplierId TEXT,
                    supplierName TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    paidAmount INTEGER NOT NULL,
                    dueDate INTEGER,
                    status TEXT NOT NULL,
                    notes TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE CASCADE,
                    FOREIGN KEY(purchaseId) REFERENCES purchases(id) ON DELETE SET NULL,
                    FOREIGN KEY(supplierId) REFERENCES suppliers(id) ON DELETE SET NULL
                )
            """.trimIndent())

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payables_businessId ON payables(businessId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payables_businessId_supplierId ON payables(businessId, supplierId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payables_businessId_purchaseId ON payables(businessId, purchaseId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payables_businessId_status ON payables(businessId, status)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payables_dueDate ON payables(dueDate)"
            )

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id TEXT NOT NULL,
                    businessId TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    paymentMethod TEXT NOT NULL,
                    referenceNumber TEXT,
                    notes TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(businessId) REFERENCES businesses(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expenses_businessId ON expenses(businessId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expenses_businessId_category ON expenses(businessId, category)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expenses_createdAt ON expenses(createdAt)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_3_4
    )
}
