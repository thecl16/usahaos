package com.example

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.migration.Migrations
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseMigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun exportedSchemaV3_opensWithProductionConfig_andPreservesData() {
        helper.createDatabase(testDb, 3).apply {
            execSQL(
                "INSERT INTO businesses (id, name, type, address, phone, email, currency, createdAt, updatedAt) " +
                    "VALUES ('biz-1', 'Toko Uji', 'RETAIL', 'Jl. Uji 1', '0800', 'uji@example.test', 'IDR', 1, 1)"
            )
            close()
        }

        helper.runMigrationsAndValidate(testDb, 3, true, *Migrations.ALL)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.build(context, testDb)
        try {
            val business = runBlocking { db.businessDao().getBusinessById("biz-1") }
            assertEquals("Toko Uji", business?.name)
        } finally {
            db.close()
        }
    }
}
