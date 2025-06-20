package com.example.voyago.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.DataProfile

@Composable
fun AboutSection(profile: DataProfile) {
    val verifiedInfo = listOfNotNull(
        profile.email.takeIf { it.isNotBlank() }?.let { "Email: $it" },
        profile.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" },
        profile.instagram.takeIf { it.isNotBlank() }?.let { "Instagram: $it" },
        profile.facebook.takeIf { it.isNotBlank() }?.let { "Facebook: $it" }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "${profile.name}'s Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (profile.about.isNotBlank()) {
            Text(
                text = profile.about,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        if (verifiedInfo.isNotEmpty()) {
            Text(
                text = "Verified Information",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                verifiedInfo.forEach { info ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = info,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        ProfileInfoItem(title = "Spoken Languages", value = profile.languages.joinToString(", "))
        ProfileInfoItem(title = "Member Since", value = profile.memberSince)
        ProfileInfoItem(title = "Response Rate", value = profile.responseRate)
        ProfileInfoItem(title = "Response Time", value = profile.responseTime)
    }
}

@Composable
private fun ProfileInfoItem(title: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
