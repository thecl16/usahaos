package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.UsahaBlue600
import com.example.ui.theme.UsahaNavy900
import com.example.ui.theme.UsahaSlate100
import com.example.ui.theme.UsahaSlate500

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    testTag: String = "empty_state_view"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(UsahaSlate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(34.dp),
                    tint = UsahaSlate500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = UsahaSlate500,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (primaryButtonText != null && onPrimaryClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onPrimaryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UsahaBlue600,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("${testTag}_primary_btn")
                ) {
                    Text(text = primaryButtonText, fontWeight = FontWeight.Medium)
                }
            }

            if (secondaryButtonText != null && onSecondaryClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSecondaryClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("${testTag}_secondary_btn")
                ) {
                    Text(text = secondaryButtonText, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
