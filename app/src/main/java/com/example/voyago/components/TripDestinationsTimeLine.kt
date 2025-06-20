package com.example.voyago.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.Destination
import androidx.compose.material3.Icon as Icon1

@Composable
fun TripDestinationsTimeline(destinations: List<Destination>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        destinations.forEachIndexed { index, destination ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // LEFT COLUMN: Timeline indicator (circle + line)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (destination.isMandatory) MaterialTheme.colorScheme.primary else Color.Gray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon1(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Destination icon",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    if (index < destinations.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(Color.LightGray)
                        )
                    }
                }

                // RIGHT COLUMN: Destination info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp)
                ) {
                    Text(
                        text = destination.address.fullAddress,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    if (destination.isMandatory) {
                        Text(
                            text = "Mandatory stop",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
