package com.example.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.BusinessEntity
import com.example.domain.model.BusinessType
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate400
import com.example.ui.theme.UsahaSlate500

@Composable
fun WorkspaceSwitcherDialog(
    businesses: List<BusinessEntity>,
    currentBusinessId: String?,
    onSelectBusiness: (String) -> Unit,
    onAddNewBusiness: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("workspace_switcher_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(UsahaSlate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Workspace",
                                tint = UsahaNavy900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pilih Workspace Usaha",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = UsahaNavy900
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("workspace_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = UsahaSlate500
                        )
                    }
                }

                Text(
                    text = "Setiap workspace memiliki data transaksi, produk, dan laporan yang terisolasi total.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(12.dp))

                // Business list
                LazyColumn(modifier = Modifier.fillMaxWidth().height(if (businesses.size > 3) 240.dp else androidx.compose.ui.unit.Dp.Unspecified)) {
                    items(businesses) { biz ->
                        val isCurrent = biz.id == currentBusinessId
                        val bizType = BusinessType.fromCode(biz.type)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) UsahaBlue600 else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectBusiness(biz.id) }
                                .testTag("workspace_item_${biz.id}"),
                            color = if (isCurrent) UsahaBlue50 else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = biz.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = UsahaNavy900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = UsahaSlate100,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = bizType.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UsahaSlate500,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (biz.address.isNotBlank()) {
                                        Text(
                                            text = biz.address,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UsahaSlate500,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isCurrent) "Aktif" else "Pilih",
                                    tint = if (isCurrent) UsahaBlue600 else UsahaSlate400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add New Business Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onAddNewBusiness()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_add_new_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Usaha",
                        tint = UsahaBlue600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tambah Usaha Baru",
                        fontWeight = FontWeight.SemiBold,
                        color = UsahaBlue600
                    )
                }
            }
        }
    }
}
