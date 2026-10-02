package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (fullName: String, email: String, phone: String, pass: String) -> Unit,
    onResetPassword: (email: String, newPass: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register, 2: Forgot Password

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Branding
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(UsahaBlue600, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "USAHAOS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = UsahaNavy900
            )

            Text(
                text = "Satu sistem untuk seluruh operasional usaha.",
                style = MaterialTheme.typography.bodySmall,
                color = UsahaSlate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (selectedTab != 2) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = UsahaSlate100,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                            indicator = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    localError = null
                                },
                                text = {
                                    Text(
                                        "Masuk",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 0) UsahaBlue600 else UsahaSlate500
                                    )
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(
                                        if (selectedTab == 0) Color.White else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("tab_login")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    localError = null
                                },
                                text = {
                                    Text(
                                        "Daftar Baru",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 1) UsahaBlue600 else UsahaSlate500
                                    )
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(
                                        if (selectedTab == 1) Color.White else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("tab_register")
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        // Forgot Password Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Lupa Kata Sandi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                        }
                        Text(
                            text = "Masukkan email dan buat kata sandi baru untuk akun Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UsahaSlate500,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    }

                    // Form Fields
                    if (selectedTab == 1) {
                        // Register extra fields
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nama Lengkap") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Nama", tint = UsahaSlate500)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Nomor Telepon / WhatsApp") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "Telepon", tint = UsahaSlate500)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_phone_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email field (Common)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Alamat Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = UsahaSlate500)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (selectedTab == 2) "Kata Sandi Baru" else "Kata Sandi") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Sandi", tint = UsahaSlate500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Lihat Sandi"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (selectedTab == 1 || selectedTab == 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Kata Sandi") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Konfirmasi Sandi", tint = UsahaSlate500)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    selectedTab = 2
                                    localError = null
                                },
                                modifier = Modifier.testTag("auth_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Lupa kata sandi?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UsahaBlue600
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Local error message display
                    AnimatedVisibility(visible = localError != null) {
                        Text(
                            text = localError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            localError = null
                            when (selectedTab) {
                                0 -> { // Login
                                    if (email.isBlank() || password.isBlank()) {
                                        localError = "Email dan kata sandi wajib diisi"
                                    } else {
                                        onLogin(email, password)
                                    }
                                }
                                1 -> { // Register
                                    if (fullName.isBlank()) {
                                        localError = "Nama lengkap wajib diisi"
                                    } else if (!email.contains("@")) {
                                        localError = "Format email tidak valid"
                                    } else if (password.length < 6) {
                                        localError = "Kata sandi minimal 6 karakter"
                                    } else if (password != confirmPassword) {
                                        localError = "Konfirmasi kata sandi tidak cocok"
                                    } else {
                                        onRegister(fullName, email, phone, password)
                                    }
                                }
                                2 -> { // Reset password
                                    if (!email.contains("@")) {
                                        localError = "Format email tidak valid"
                                    } else if (password.length < 6) {
                                        localError = "Kata sandi baru minimal 6 karakter"
                                    } else if (password != confirmPassword) {
                                        localError = "Konfirmasi kata sandi tidak cocok"
                                    } else {
                                        onResetPassword(email, password)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_btn"),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "Masuk ke UsahaOS"
                                        1 -> "Daftar Akun Baru"
                                        else -> "Simpan Kata Sandi Baru"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Submit",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (selectedTab == 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { selectedTab = 0 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_back_to_login_btn")
                        ) {
                            Text(
                                text = "Kembali ke Halaman Masuk",
                                color = UsahaSlate500,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "UsahaOS • Platform Operasional Terpadu UMKM Indonesia",
                style = MaterialTheme.typography.labelSmall,
                color = UsahaSlate500
            )
        }
    }
}
