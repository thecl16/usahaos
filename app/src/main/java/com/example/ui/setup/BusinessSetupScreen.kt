package com.example.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BusinessType
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaEmerald600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500

@Composable
fun BusinessSetupScreen(
    isLoading: Boolean,
    onCreateBusiness: (
        name: String,
        type: BusinessType,
        address: String,
        phone: String,
        email: String,
        currency: String
    ) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BusinessType.RETAIL) }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("IDR - Rupiah (Rp)") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("business_setup_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Indicator & Branding
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(UsahaBlue600, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Setup Usaha",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Setup Workspace Usaha",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = UsahaNavy900
        )

        Text(
            text = "Langkah awal untuk memulai operasional bisnismu di UsahaOS.",
            style = MaterialTheme.typography.bodyMedium,
            color = UsahaSlate500
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Content
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Business Name Field
                Text(
                    text = "1. Nama Usaha",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Contoh: Kopi Kenangan Senja, Toko Berkah") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_business_name_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Business Type Selection
                Text(
                    text = "2. Jenis Operasional Usaha",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "UsahaOS akan mengaktifkan modul dan alur kerja bawaan sesuai jenis usaha yang kamu pilih.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                BusinessType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val icon = when (type) {
                        BusinessType.FNB -> Icons.Default.Restaurant
                        BusinessType.RETAIL -> Icons.Default.ShoppingBag
                        BusinessType.SERVICE -> Icons.Default.Work
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) UsahaBlue600 else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedType = type }
                            .testTag("setup_type_${type.code.lowercase()}"),
                        color = if (isSelected) UsahaBlue50 else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) UsahaBlue600 else UsahaSlate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = type.title,
                                    tint = if (isSelected) Color.White else UsahaSlate500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UsahaNavy900
                                )
                                Text(
                                    text = type.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UsahaBlue600,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UsahaSlate500
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isSelected) "Dipilih" else "Belum dipilih",
                                tint = if (isSelected) UsahaBlue600 else UsahaSlate400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Contact Details
                Text(
                    text = "3. Kontak & Lokasi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Usaha / Toko") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Alamat", tint = UsahaSlate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_address_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. Telepon") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Telepon", tint = UsahaSlate500)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_phone_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Usaha") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = UsahaSlate500)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setup_email_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Currency Field (Default IDR)
                Text(
                    text = "4. Mata Uang Operasional",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Mata Uang Standar") },
                    leadingIcon = {
                        Icon(Icons.Default.Paid, contentDescription = "Mata Uang", tint = UsahaEmerald600)
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_currency_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (businessName.isBlank()) {
                            errorMessage = "Nama usaha wajib diisi"
                        } else {
                            errorMessage = null
                            onCreateBusiness(
                                businessName,
                                selectedType,
                                address,
                                phone,
                                email,
                                "IDR"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("setup_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Buat Workspace Usaha Sekarang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
