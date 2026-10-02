package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BusinessEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.BusinessType
import com.example.ui.theme.UsahaBlue100
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    business: BusinessEntity?,
    user: UserEntity?,
    onOpenDrawer: () -> Unit,
    onOpenWorkspaceDialog: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenFeatureSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            if (business != null) {
                // Workspace Selector Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = UsahaSlate100,
                    modifier = Modifier
                        .clickable { onOpenWorkspaceDialog() }
                        .testTag("topbar_workspace_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(UsahaBlue100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Usaha",
                                tint = UsahaBlue600,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = business.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = UsahaNavy900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Business Type Badge
                                val bizType = BusinessType.fromCode(business.type)
                                Surface(
                                    color = UsahaBlue600,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = bizType.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Pilih Usaha",
                            tint = UsahaSlate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "USAHAOS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = UsahaNavy900
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("topbar_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Buka Menu",
                    tint = UsahaNavy900
                )
            }
        },
        actions = {
            Box {
                IconButton(
                    onClick = { showUserMenu = true },
                    modifier = Modifier.testTag("topbar_user_profile_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(UsahaBlue600),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.take(1)?.uppercase() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                DropdownMenu(
                    expanded = showUserMenu,
                    onDismissRequest = { showUserMenu = false },
                    modifier = Modifier.testTag("user_dropdown_menu")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = user?.fullName ?: "Pemilik Usaha",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = UsahaSlate500
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Ganti Workspace Usaha") },
                        onClick = {
                            showUserMenu = false
                            onOpenWorkspaceDialog()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Pengaturan Fitur & Modul") },
                        onClick = {
                            showUserMenu = false
                            onOpenFeatureSettings()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Profil Usaha") },
                        onClick = {
                            showUserMenu = false
                            onOpenProfile()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Keluar / Logout",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            showUserMenu = false
                            onLogout()
                        }
                    )
                }
            }
        }
    )
}
