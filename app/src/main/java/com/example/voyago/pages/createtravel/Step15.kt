package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.viewModels.TravelViewModel


@Composable
fun Step15Screen(viewModel: TravelViewModel){
    val travel by viewModel.travel.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item{
            Text(
                text = "You are done! Check the information you have submitted.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        item{
            Text(
                "Review all the information you have entered. Once you are finished you can proceed with publishing your trip.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W300,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        item{
            InfoRow(infoType = "Title:", infoDescription = travel.title)
            InfoRow(infoType = "Description:", infoDescription = travel.description)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color.Black
            )
        }

        item{
            InfoRow(infoType = "Activities:", infoDescription = travel.activities.joinToString(", ") { it.name })
            InfoRow(
                infoType = "Destination:",
                infoDescription = travel.destinations.firstOrNull()?.address?.fullAddress ?: "No destination specified"
            )
            InfoRow(infoType = "Duration:", infoDescription = "Start: ${travel.startDate} \nEnd: ${travel.endDate}")
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color.Black
            )
        }

        item{
            InfoRow(infoType = "Participants:", infoDescription = "${travel.maxParticipants.adults} adults \n${travel.maxParticipants.children} children ")
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color.Black
            )
        }
        item{
            InfoRow(infoType = "Price per person:", infoDescription = "${travel.pricePerPerson}€")
        }
    }

}


@Composable
fun InfoRow(
    infoType: String,
    infoDescription: String,
){
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
        ) {
            Text(
                infoType,
                style = MaterialTheme.typography.titleLarge
                ,
            )
        }
        Column(
            modifier = Modifier
                .width(200.dp)
        ) {
            Text(
                infoDescription,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}