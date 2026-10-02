package com.example.ui.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessEntity
import com.example.domain.model.BusinessType
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate500

@Composable
fun BusinessProfileScreen(
    business: BusinessEntity?,
    onSave: (BusinessEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    if (business == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tidak ada usaha aktif.")
        }
        return
    }

    var name by remember { mutableStateOf(business.name) }
    var address by remember { mutableStateOf(business.address) }
    var phone by remember { mutableStateOf(business.phone) }
    var email by remember { mutableStateOf(business.email) }
    val bizType = remember { BusinessType.fromCode(business.type) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("business_profile_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("profile_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = UsahaNavy900
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Profil Usaha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "Informasi detail outlet dan identitas bisnis",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Usaha") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = "Nama", tint = UsahaSlate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = bizType.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipe Usaha") },
                    supportingText = { Text("Tipe usaha mengatur modul kerja bawaan.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Alamat", tint = UsahaSlate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_address_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. Telepon") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = "Telepon", tint = UsahaSlate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Usaha") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = UsahaSlate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_email_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = business.currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mata Uang") },
                    leadingIcon = {
                        Icon(Icons.Default.Paid, contentDescription = "Mata Uang", tint = UsahaSlate500)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSave(
                            business.copy(
                                name = name,
                                address = address,
                                phone = phone,
                                email = email
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_save_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = UsahaBlue600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
