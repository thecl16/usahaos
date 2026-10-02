package com.example.ui.features

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessEntity
import com.example.domain.model.BusinessType
import com.example.domain.model.FeatureCategory
import com.example.domain.model.FeatureRegistry
import com.example.ui.theme.UsahaBlue50
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500

@Composable
fun FeatureManagementScreen(
    business: BusinessEntity?,
    featureFlags: Map<String, Boolean>,
    onToggleFeature: (key: String, isEnabled: Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = listOf("Semua Fitur", "F&B", "Retail", "Jasa", "Operasional")

    val filteredFeatures = remember(selectedTab) {
        when (selectedTab) {
            1 -> FeatureRegistry.allFeatures.filter { it.category == FeatureCategory.FNB }
            2 -> FeatureRegistry.allFeatures.filter { it.category == FeatureCategory.RETAIL }
            3 -> FeatureRegistry.allFeatures.filter { it.category == FeatureCategory.SERVICE }
            4 -> FeatureRegistry.allFeatures.filter { it.category == FeatureCategory.OPERATIONS }
            else -> FeatureRegistry.allFeatures
        }
    }

    val businessType = business?.let { BusinessType.fromCode(it.type) } ?: BusinessType.RETAIL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("feature_management_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("features_back_btn")
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
                    text = "Fitur & Modul Bisnis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UsahaNavy900
                )
                Text(
                    text = "Kustomisasi modul operasional untuk ${business?.name ?: "toko Anda"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaSlate500
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Banner
        Surface(
            color = UsahaBlue50,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Informasi",
                    tint = UsahaBlue600,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Fitur yang dinonaktifkan akan disembunyikan dari menu dan kasir tanpa menghapus data historis yang sudah tersimpan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UsahaNavy900
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset to Default Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tipe Usaha: ${businessType.title}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = UsahaSlate500
            )

            OutlinedButton(
                onClick = onResetDefaults,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("features_reset_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Standar",
                    modifier = Modifier.size(16.dp),
                    tint = UsahaNavy900
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset ke Standar", fontSize = 12.sp, color = UsahaNavy900)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {}
        ) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) UsahaBlue600 else UsahaSlate500
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedTab == index) UsahaSlate100 else Color.Transparent
                        )
                        .testTag("tab_cat_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Feature Cards
        filteredFeatures.forEach { feature ->
            val isEnabled = featureFlags[feature.key] ?: feature.defaultEnabledFor.contains(businessType)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("feature_card_${feature.key}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = feature.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = UsahaNavy900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = UsahaSlate100,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = feature.category.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UsahaSlate500,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = UsahaSlate500
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggleFeature(feature.key, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UsahaBlue600,
                            uncheckedThumbColor = UsahaSlate500,
                            uncheckedTrackColor = UsahaSlate100
                        ),
                        modifier = Modifier.testTag("feature_switch_${feature.key}")
                    )
                }
            }
        }
    }
}
