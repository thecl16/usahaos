package com.example.data.local.migration

import androidx.room.migration.Migration

/**
 * Registry migrasi Room eksplisit.
 * Baseline: schema versi 3 adalah schema pertama yang diekspor.
 * Saat ada perubahan schema: naikkan version di AppDatabase,
 * tambah MIGRATION_3_4 di sini dengan SQL yang menjaga data lama,
 * lalu masukkan ke ALL.
 */
object Migrations {
    val ALL: Array<Migration> = arrayOf()
}
