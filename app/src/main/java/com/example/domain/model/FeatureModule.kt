package com.example.domain.model

enum class FeatureCategory(val label: String) {
    FNB("Modul F&B"),
    RETAIL("Modul Retail"),
    SERVICE("Modul Jasa"),
    OPERATIONS("Operasional & Keuangan")
}

data class FeatureDefinition(
    val key: String,
    val name: String,
    val description: String,
    val category: FeatureCategory,
    val defaultEnabledFor: Set<BusinessType>
)

object FeatureRegistry {
    // F&B Specific Features
    val FNB_TABLES = FeatureDefinition(
        key = "fnb_tables",
        name = "Manajemen Meja & Ruangan",
        description = "Pengaturan nomor meja, kapasitas, dan alur pemesanan meja.",
        category = FeatureCategory.FNB,
        defaultEnabledFor = setOf(BusinessType.FNB)
    )
    val FNB_KDS = FeatureDefinition(
        key = "fnb_kds",
        name = "Kitchen Display System (KDS)",
        description = "Layar pesanan dapur dengan status: Baru, Diproses, Siap, Disajikan.",
        category = FeatureCategory.FNB,
        defaultEnabledFor = setOf(BusinessType.FNB)
    )
    val FNB_MODIFIERS = FeatureDefinition(
        key = "fnb_modifiers",
        name = "Modifier & Topping",
        description = "Kustomisasi pesanan (Ukuran, Topping, Level Gula, Level Es, Ekstra).",
        category = FeatureCategory.FNB,
        defaultEnabledFor = setOf(BusinessType.FNB)
    )
    val FNB_RECIPES = FeatureDefinition(
        key = "fnb_recipes",
        name = "Resep & Bahan Baku",
        description = "Pengurangan otomatis stok bahan saat produk terjual.",
        category = FeatureCategory.FNB,
        defaultEnabledFor = setOf(BusinessType.FNB)
    )
    val FNB_FOOD_COST = FeatureDefinition(
        key = "fnb_food_cost",
        name = "Kalkulasi Food Cost / HPP",
        description = "Estimasi HPP otomatis berdasarkan porsi resep bahan baku.",
        category = FeatureCategory.FNB,
        defaultEnabledFor = setOf(BusinessType.FNB)
    )

    // Retail Specific Features
    val RETAIL_VARIANTS = FeatureDefinition(
        key = "retail_variants",
        name = "Varian Produk",
        description = "Pengelolaan varian produk dengan SKU, barcode, dan harga berbeda.",
        category = FeatureCategory.RETAIL,
        defaultEnabledFor = setOf(BusinessType.RETAIL, BusinessType.FNB)
    )
    val RETAIL_SIZE_COLOR = FeatureDefinition(
        key = "retail_size_color",
        name = "Atribut Ukuran & Warna",
        description = "Katalog dimensi pakaian, alas kaki, atau barang mode.",
        category = FeatureCategory.RETAIL,
        defaultEnabledFor = setOf(BusinessType.RETAIL)
    )
    val RETAIL_SERIAL_NUMBER = FeatureDefinition(
        key = "retail_serial_number",
        name = "Pelacakan Serial Number",
        description = "Pencatatan nomor seri unik untuk garansi dan elektronik.",
        category = FeatureCategory.RETAIL,
        defaultEnabledFor = setOf(BusinessType.RETAIL)
    )
    val RETAIL_BATCH_EXPIRY = FeatureDefinition(
        key = "retail_batch_expiry",
        name = "Batch & Tanggal Kedaluwarsa",
        description = "Pencatatan nomor batch produksi dan tanggal expired (metode FEFO).",
        category = FeatureCategory.RETAIL,
        defaultEnabledFor = setOf(BusinessType.RETAIL, BusinessType.FNB)
    )
    val RETAIL_MULTI_PRICE = FeatureDefinition(
        key = "retail_multi_price",
        name = "Multi Harga / Harga Grosir",
        description = "Tingkatan harga berdasarkan kuantitas beli atau kategori pelanggan.",
        category = FeatureCategory.RETAIL,
        defaultEnabledFor = setOf(BusinessType.RETAIL)
    )

    // Service Specific Features
    val SERVICE_BOOKING = FeatureDefinition(
        key = "service_booking",
        name = "Reservasi & Booking",
        description = "Penjadwalan janji temu pelanggan untuk layanan jasa.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )
    val SERVICE_QUEUE = FeatureDefinition(
        key = "service_queue",
        name = "Antrean Pelanggan",
        description = "Manajemen nomor antrean kasir dan pemanggilan giliran servis.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )
    val SERVICE_TECHNICIAN = FeatureDefinition(
        key = "service_technician",
        name = "Teknisi & Penugasan",
        description = "Alokasi petugas/mekanik/kapster untuk setiap order pekerjaan.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )
    val SERVICE_JOB_ORDER = FeatureDefinition(
        key = "service_job_order",
        name = "Job Order (SPK)",
        description = "Surat perintah kerja servis dengan riwayat diagnosa dan suku cadang.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )
    val SERVICE_COMMISSION = FeatureDefinition(
        key = "service_commission",
        name = "Komisi Teknisi / Pegawai",
        description = "Perhitungan persentase komisi bagi hasil per pekerjaan tuntas.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )
    val SERVICE_STATUS = FeatureDefinition(
        key = "service_status",
        name = "Pelacakan Status Servis",
        description = "Alur status kerja: Menunggu, Dikerjakan, Menunggu Pembayaran, Selesai.",
        category = FeatureCategory.SERVICE,
        defaultEnabledFor = setOf(BusinessType.SERVICE)
    )

    // Core / Operational Modules
    val OP_PURCHASING = FeatureDefinition(
        key = "op_purchasing",
        name = "Manajemen Pembelian & Hutang",
        description = "Pencatatan order pembelian ke pemasok dan tempo pembayaran hutang usaha.",
        category = FeatureCategory.OPERATIONS,
        defaultEnabledFor = setOf(BusinessType.FNB, BusinessType.RETAIL, BusinessType.SERVICE)
    )
    val OP_RECEIVABLES = FeatureDefinition(
        key = "op_receivables",
        name = "Manajemen Piutang Pelanggan",
        description = "Pelacakan penjualan tempo, jatuh tempo penagihan, dan riwayat pelunasan.",
        category = FeatureCategory.OPERATIONS,
        defaultEnabledFor = setOf(BusinessType.FNB, BusinessType.RETAIL, BusinessType.SERVICE)
    )
    val OP_EXPENSES = FeatureDefinition(
        key = "op_expenses",
        name = "Pengeluaran Operasional & Kas",
        description = "Pencatatan biaya operasional, kas kecil, dan arus kas masuk-keluar.",
        category = FeatureCategory.OPERATIONS,
        defaultEnabledFor = setOf(BusinessType.FNB, BusinessType.RETAIL, BusinessType.SERVICE)
    )

    val allFeatures: List<FeatureDefinition> = listOf(
        // F&B
        FNB_TABLES,
        FNB_KDS,
        FNB_MODIFIERS,
        FNB_RECIPES,
        FNB_FOOD_COST,
        // Retail
        RETAIL_VARIANTS,
        RETAIL_SIZE_COLOR,
        RETAIL_SERIAL_NUMBER,
        RETAIL_BATCH_EXPIRY,
        RETAIL_MULTI_PRICE,
        // Service
        SERVICE_BOOKING,
        SERVICE_QUEUE,
        SERVICE_TECHNICIAN,
        SERVICE_JOB_ORDER,
        SERVICE_COMMISSION,
        SERVICE_STATUS,
        // Operations
        OP_PURCHASING,
        OP_RECEIVABLES,
        OP_EXPENSES
    )

    fun getDefaultFlagsFor(type: BusinessType): Map<String, Boolean> {
        return allFeatures.associate { feature ->
            feature.key to feature.defaultEnabledFor.contains(type)
        }
    }
}
