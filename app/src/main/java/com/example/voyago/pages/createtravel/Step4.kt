package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.voyago.Address
import com.example.voyago.Destination
import com.example.voyago.components.DraggableLocationMap
import com.example.voyago.components.SearchAddressScreen
import com.example.voyago.viewModels.TravelViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun Step4Screen(viewModel: TravelViewModel, showSearchScreen: Boolean, onClick: () -> Unit) {
    val travel by viewModel.travel.collectAsState()

    val defaultLatLng = LatLng(45.4642, 9.1900)

    var selectedLocation by remember {
        mutableStateOf(
            travel.destinations.firstOrNull()?.address?.latLng ?: defaultLatLng
        )
    }
    @Composable
    fun AddressButton(destination: Destination?) {
        val isSelected = destination != null

        Box(
            modifier = Modifier
                .height(100.dp)
                .background(Color.White)
                .padding(vertical = 20.dp)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color.Black else Color.Transparent,
                    shape = RoundedCornerShape(24.dp)
                )
                .shadow(
                    elevation = if (!isSelected) 4.dp else 0.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false
                ),
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = destination?.address?.fullAddress ?: "Insert your starting point",
                        color = Color.Black,
                        fontSize = 3.em,
                    )
                }
            }
        }
    }

    if (showSearchScreen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SearchAddressScreen(
                onAddressSelected = { address ->
                    println(address)

                    // Crea la destinazione di partenza
                    val startingPoint = Destination(address = address, isMandatory = true)

                    // Aggiorna la destinazione principale
                    selectedLocation = address.latLng

                    // Sovrascrive tutta la lista delle destinazioni
                    travel.destinations.clear()
                    travel.destinations.add(startingPoint)

                    onClick()
                }
            )
        }
    }

    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        )
        {
            // Titolo
            Text(
                text = "Where is your destination?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
            Text(
                "The location will be shared with participants only after they have made a reservation.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W300,
                modifier = Modifier
                    .fillMaxWidth()

            )
            AddressButton(travel.destinations.firstOrNull())
            DraggableLocationMap(
                modifier = Modifier
                    .fillMaxSize(),
                initialLocation = selectedLocation,
                onLocationChanged = { newLocation ->
                    selectedLocation = newLocation
                    travel.destinations.add(Destination(
                        address = Address(
                            street = "",
                            city = "",
                            region = "",
                            country = "",
                            fullAddress = "",
                            latLng = newLocation
                        ), true))
                }
            )
        }
    }
}
