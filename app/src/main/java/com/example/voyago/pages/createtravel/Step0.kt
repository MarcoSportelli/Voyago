package com.example.voyago.pages.createtravel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.voyago.R

@Composable
fun Step0Screen() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Getting started on Voyago is easy",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.05f))

        StepCard(
            stepNumber = 1,
            title = "Tell us about your trip",
            description = "Share some basic information, such as your destination and travel time.",
            img = R.drawable.trail,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.02f))

        StepCard(
            stepNumber = 2,
            title = "Make it stand out from the rest",
            description = "Add a minimum of 3 photos, a title and a description: we'll help you.",
            img = R.drawable.taking_photo,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(screenHeight * 0.02f))

        StepCard(
            stepNumber = 3,
            title = "Complete and publish",
            description = "Choose a starting price, check some details, and publish.",
            img = R.drawable.looking_out_airplane,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StepCard(
    stepNumber: Int,
    title: String,
    description: String,
    img: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 5.em,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .aspectRatio(1.4f)
            ) {
                Image(
                    painter = painterResource(id = img),
                    contentDescription = "Step illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            color = Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                )
            }
        }
    }
}
