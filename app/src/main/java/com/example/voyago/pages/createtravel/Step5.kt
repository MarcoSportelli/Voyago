package com.example.voyago.pages.createtravel

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.voyago.Address
import com.example.voyago.Destination
import com.example.voyago.components.AddDestinationButton
import com.example.voyago.components.AddressRow
import com.example.voyago.components.SearchAddressScreen
import com.example.voyago.viewModels.TravelViewModel
import com.google.android.gms.maps.model.LatLng

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Step5Screen(
    viewModel: TravelViewModel,
    showSearchScreen: Boolean, onClick: ()->Unit,
    onChangeDestinations: () -> Unit
) {
    val travel by viewModel.travel.collectAsState()

    var selectedLocation by remember { mutableStateOf(Destination(
        address = Address(
            street = "",
            city = "",
            region = "",
            country = "",
            fullAddress = "",
            latLng = LatLng(0.0, 0.0)
        ), false)) }


    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    if (showSearchScreen) {
        SearchAddressScreen(
            onAddressSelected = { address ->
                selectedLocation = Destination(address, false)
                viewModel.addDestination(selectedLocation)

                if (travel.destinations.size == 1) {
                    onChangeDestinations()
                }
                onClick()
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Add stages to your itinerary",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = screenHeight * 0.02f)
                )
            }

            item {
                Text(
                    "You can set the stages you enter as mandatory or optional.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W300,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = screenHeight * 0.02f)
                )
            }

            item {
                AddressRow(
                    address = travel.destinations[0].address.fullAddress,
                    iconSize = 12.dp,
                    circleSize = 60.dp,
                    canSetMandatory = false
                )
            }

            items(travel.destinations.size - 1 ) { index ->
                val realIndex = index + 1
                val destination = travel.destinations[realIndex]
                AddressRow(
                    address = destination.address.fullAddress,
                    icon = Icons.Default.Delete,
                    iconColor = Color.Red,
                    iconSize = 12.dp,
                    circleSize = 60.dp,
                    onClick = {
                        viewModel.removeDestination(realIndex)

                        if (travel.destinations.size == 1) {
                            onChangeDestinations()
                        }

                    },
                    mandatory = destination.isMandatory,
                    canSetMandatory = true,
                    onSwitchMandatory = { isChecked ->
                        viewModel.updateDestinationMandatory(realIndex, isChecked)
                    },
                )
            }

            item {
                AddDestinationButton(
                    onClick = {
                        onClick()
                    }
                )
            }
        }
    }
}
