package com.example.voyago.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voyago.Travel
import com.example.voyago.components.FilterChipsRow
import com.example.voyago.components.TripCard
import com.example.voyago.components.TripSearchbar
import com.example.voyago.profileViewModel
import com.example.voyago.viewModels.TravelViewModels
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


fun extractCityAndCountry(fullAddress: String): String {
    val parts = fullAddress.split(",").map { it.trim() }
    return if (parts.size >= 2) {
        "${parts[parts.size - 2]} ${parts.last()}"
    } else {
        fullAddress
    }
}

@Composable
fun TravelProposalScreen2(
    viewModel: TravelViewModels = viewModel(),
    navController: NavHostController,
    userId: Int
) {
    val tripsList by viewModel.travels.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var selectedLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedActivities by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selectedAdults by rememberSaveable { mutableIntStateOf(0) }
    var selectedChildren by rememberSaveable { mutableIntStateOf(0) }
    var selectedExperience by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selminPrice by rememberSaveable { mutableFloatStateOf(0f) }
    var selmaxPrice by rememberSaveable { mutableFloatStateOf(9999f) }
    val today = LocalDate.now()
    var selectedStartDate by rememberSaveable { mutableStateOf(today) }
    var selectedEndDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }

    val tripDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

    val filteredTrips by remember {
        derivedStateOf {
            val filtered = tripsList.filter { trip ->

                val isNotOwner = trip.userId != profileViewModel.id

                val matchesQuery = query.isBlank() ||
                        trip.title.contains(query, ignoreCase = true) ||
                        extractCityAndCountry(trip.destinations.first().address.fullAddress)
                            .contains(query, ignoreCase = true)

                val matchesLocation = selectedLocation.isNullOrEmpty() ||
                        extractCityAndCountry(trip.destinations.first().address.fullAddress) == selectedLocation

                val matchesActivities = selectedActivities.isEmpty() ||
                        trip.activities.any { selectedActivities.contains(it.name) }

                val matchesTypes = selectedExperience.isEmpty() ||
                        selectedExperience.any { type ->
                            trip.experiences.any { it.name.equals(type, ignoreCase = true) }
                        }

                val matchesPrice = trip.pricePerPerson in selminPrice..selmaxPrice

                val tripStart = LocalDate.parse(trip.startDate, tripDateFormatter)
                val tripEnd = LocalDate.parse(trip.endDate, tripDateFormatter)

                val matchesDate = tripStart >= selectedStartDate &&
                        (selectedEndDate == null || tripEnd <= selectedEndDate)

                val groupSize = trip.maxParticipants.adults + trip.maxParticipants.children
                val matchesGroupSize = groupSize >= (selectedAdults + selectedChildren)

                val result = isNotOwner && matchesQuery && matchesLocation && matchesActivities &&
                        matchesTypes && matchesPrice && matchesGroupSize && matchesDate

                if (result) {
                    Log.d("FILTER", "✅ Trip '${trip.title}' passed filters")
                }

                result
            }

            // Log all applied filters
            Log.d("FILTER", """
            🌐 Query: '$query'
            📍 Location: $selectedLocation
            🏃‍♂️ Activities: $selectedActivities
            🧭 Experience: $selectedExperience
            👨‍👩‍👧‍👦 Adults: $selectedAdults, Children: $selectedChildren
            💶 Price: $selminPrice - $selmaxPrice
            📆 Dates: $selectedStartDate to ${selectedEndDate ?: "∞"}
            🔍 Filtered trips: ${filtered.size} / ${tripsList.size}
        """.trimIndent())

            filtered
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TripSearchbar(
                        query = query,
                        onQueryChange = { query = it },
                        onResultClick = { selected -> query = selected },
                        filteredTrips = filteredTrips,
                        modifier = Modifier.weight(1f),
                        isSearchActive = isSearchActive,
                        onActiveChange = { isSearchActive = it },
                        userId = userId
                    )
                    if (!isSearchActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .offset(x = 0.dp, y = 4.dp),
                            shape = CircleShape,
                            color = Color.White,
                            tonalElevation = 2.dp,
                            shadowElevation = 4.dp,
                            onClick = { showFilter = true }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Filtri",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }



            if (filteredTrips.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trip found",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(filteredTrips) { trip ->
                    TripCard(
                        userId = userId,
                        trip = trip,
                        viewTrip = {
                            navController.navigate("trip_page/${trip.id}/false")
                        }
                    )
                }
            }
        }

        if (showFilter) {
            FilterScreen(
                initialAdults = selectedAdults,
                initialChildren = selectedChildren,
                initialMinPrice = selminPrice,
                initialMaxPrice = selmaxPrice,
                initialStartDate = selectedStartDate,
                initialEndDate = selectedEndDate,
                initialExperience = selectedExperience,
                initialActivity = selectedActivities,
                onClose = { showFilter = false },
                onReset = {
                    selectedAdults = 0
                    selectedChildren = 0
                    selectedExperience = emptyList()
                    selminPrice = 0f
                    selmaxPrice = 9999f
                    selectedStartDate = today
                    selectedEndDate = null
                },
                onApply = { adults, children, types, activities, minPrice, maxPrice, startDate, endDate ->
                    selectedAdults = adults
                    selectedChildren = children
                    selectedExperience = types
                    selminPrice = minPrice
                    selmaxPrice = maxPrice
                    selectedStartDate = startDate
                    selectedEndDate = endDate
                    selectedActivities = activities
                }
            )
        }
    }
}