package com.example.voyago.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AddressRow(
    address: String,
    icon: ImageVector = Icons.Default.LocationOn,
    iconSize: Dp = 16.dp,
    circleSize: Dp = 80.dp,
    circleColor: Color = Color.White,
    iconColor: Color = Color.Black,
    onClick: () -> Unit = {},
    mandatory: Boolean = false,
    canSetMandatory: Boolean = true,
    onSwitchMandatory: (Boolean) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = circleColor,
            onClick = onClick,
            modifier = Modifier
                .size(circleSize)
                .shadow(2.dp, shape = CircleShape),

        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Location icon",
                tint = iconColor,
                modifier = Modifier.padding(iconSize)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = address,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W500,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        if (canSetMandatory) {
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Mandatory",
                    style = MaterialTheme.typography.labelLarge)
                Switch(checked = mandatory, onCheckedChange =  onSwitchMandatory, colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    checkedTrackColor = Color.White,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    uncheckedTrackColor = Color.White,
                    checkedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) )
            }

        }
    }
}