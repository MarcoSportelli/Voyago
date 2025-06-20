package com.example.voyago.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.voyago.Address
import com.example.voyago.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAddressScreen(
    onAddressSelected: (Address) -> Unit,
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PlaceAutocompleteItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Initializes Places API
    val apiKey = BuildConfig.MAPS_API_KEY
    if (!Places.isInitialized()) {
        Places.initialize(context, apiKey)
    }
    val placesClient = remember { Places.createClient(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp),
            colors = SearchBarDefaults.colors(
                containerColor = Color.White,
            ),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            query = searchQuery,
            onQueryChange = { query ->
                searchQuery = query
                if (query.length > 2) {
                    searchPlaces(query, placesClient) { results ->
                        searchResults = results
                    }
                } else {
                    searchResults = emptyList()
                }
            },
            onSearch = { },
            active = false,
            onActiveChange = { },
            placeholder = { Text("Search address") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            content = {}
        )

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { item ->
                    AddressResultItem(
                        item = item,
                        onClick = {
                            isLoading = true
                            fetchPlaceDetails(
                                placeId = item.placeId,
                                placesClient = placesClient,
                                onComplete = { structuredAddress ->
                                    onAddressSelected(structuredAddress)
                                    isLoading = false
                                },
                                onError = {
                                    isLoading = false
                                    Log.e("SearchAddress", "Error fetching place details", it)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddressResultItem(item: PlaceAutocompleteItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = item.primaryText,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

private fun searchPlaces(
    query: String,
    placesClient: PlacesClient,
    onResults: (List<PlaceAutocompleteItem>) -> Unit
) {
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val results = response.autocompletePredictions.map { prediction ->
                PlaceAutocompleteItem(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString(),
                    address = prediction.getFullText(null).toString(),
                    latLng = LatLng(0.0, 0.0)
                )
            }
            onResults(results)
        }
        .addOnFailureListener { exception ->
            Log.e("SearchAddress", "Error searching places", exception)
            onResults(emptyList())
        }
}

private fun fetchPlaceDetails(
    placeId: String,
    placesClient: PlacesClient,
    onComplete: (Address) -> Unit,
    onError: (Exception) -> Unit
) {
    val request = FetchPlaceRequest.builder(placeId, listOf(
        Place.Field.ADDRESS_COMPONENTS,
        Place.Field.LAT_LNG,
        Place.Field.NAME,
        Place.Field.ADDRESS
    )).build()

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val latLng = place.latLng ?: LatLng(0.0, 0.0)

            val components = place.addressComponents?.asList()

            components?.forEach { component ->
                println("Component: ${component.name} - Types: ${component.types}")
            }

            val street = components?.firstOrNull { it.types.contains("route") }?.name ?: ""
            val city = components?.firstOrNull { it.types.contains("locality") }?.name ?: ""
            val region = components?.firstOrNull { it.types.contains("administrative_area_level_1") }?.name ?: ""
            val country = components?.firstOrNull { it.types.contains("country") }?.name ?: ""


            val parts = listOf(street, city, region, country).filter { it.isNotBlank() }
            val fullAddress = parts.joinToString(", ")

            println(fullAddress)

            onComplete(
                Address(
                    street = street,
                    city = city,
                    region = region,
                    country = country,
                    fullAddress = fullAddress,
                    latLng = latLng
                )
            )
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}




data class PlaceAutocompleteItem(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val address: String,
    val latLng: LatLng
)