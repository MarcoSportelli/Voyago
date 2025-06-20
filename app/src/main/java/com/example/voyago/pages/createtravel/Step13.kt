package com.example.voyago.pages.createtravel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.voyago.R

@Composable
fun Step13Screen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    )
    {
        item{
            Image(
                painter = painterResource(id = R.drawable.bg_travel2),
                contentDescription = "travel2",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()

            )
        }
        item {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Third",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    "Complete and Publish",
                    fontSize = 9.em,
                    lineHeight = 1.em,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.size(20.dp))
                Text(
                    "In this step, you will choose a starting price, carefully review all the details you’ve entered, and once everything is set, you’ll be ready to publish your new trip.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W300,

                    )
            }
        }
    }

}
