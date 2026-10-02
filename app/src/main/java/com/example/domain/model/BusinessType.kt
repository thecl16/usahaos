package com.example.domain.model

enum class BusinessType(
    val code: String,
    val title: String,
    val subtitle: String,
    val description: String
) {
    FNB(
        code = "FNB",
        title = "F&B / Kuliner",
        subtitle = "Restoran, Kafe, Kedai, Katering",
        description = "Mendukung manajemen meja, kitchen display (KDS), varian rasa/topping, dan resep bahan baku otomatis."
    ),
    RETAIL(
        code = "RETAIL",
        title = "Retail / Toko",
        subtitle = "Minimarket, Fashion, Elektronik, Grosir",
        description = "Mendukung varian ukuran/warna, barcode scanner, serial number, batch, tanggal kedaluwarsa, dan multi-harga."
    ),
    SERVICE(
        code = "SERVICE",
        title = "Jasa / Layanan",
        subtitle = "Bengkel, Barbershop, Salon, Klinik, Servis",
        description = "Mendukung reservasi booking, antrean pelanggan, penugasan teknisi, job order (SPK), dan komisi kerja."
    );

    companion object {
        fun fromCode(code: String): BusinessType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: RETAIL
        }
    }
}
