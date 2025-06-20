package com.example.voyago.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AddDestinationButton(
    onClick: () -> Unit = {},
    icon: ImageVector = Icons.Default.Add,
    iconSize: Dp = 12.dp,
    circleSize: Dp = 60.dp,
    circleColor: Color = Color.White,
    iconColor: Color = Color.Black,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = circleColor,
            onClick = onClick,
            modifier = Modifier.size(circleSize)
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
    }
}