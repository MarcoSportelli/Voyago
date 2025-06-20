package com.example.voyago.pages.createtravel

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.voyago.Activity
import com.example.voyago.DataProfile
import com.example.voyago.Experience
import com.example.voyago.R
import com.example.voyago.Travel
import com.example.voyago.components.EmptyCard
import com.example.voyago.components.FilterChipsRow
import com.example.voyago.components.TripCard
import com.example.voyago.components.TripSearchbar
import com.example.voyago.pages.ActivityChips
import com.example.voyago.pages.DateRangePickerDialog
import com.example.voyago.pages.ExperienceChips
import com.example.voyago.pages.FilterScreen
import com.example.voyago.pages.NumberPicker
import com.example.voyago.pages.extractCityAndCountry
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.viewModels.ActivityViewModel
import com.example.voyago.viewModels.ExperienceViewModel
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.TravelViewModel
import com.example.voyago.viewModels.TravelViewModels
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.ranges.contains


@Composable
fun Step2FromScratchScreen() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ){
        Image(
            painter = painterResource(id = R.drawable.bg_hiking),
            contentDescription = "hiking",
            modifier = Modifier
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "First",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(screenHeight * 0.01f))
            Text(
                "Tell us about your trip",
                fontSize = 10.em,
                lineHeight = 1.em,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(screenHeight * 0.03f))
            Text(
                "At this stage, we will ask you what type of trip you offer and if, by booking, what people will have at their disposal. After that, you will have to communicate the destination, the duration and how many people can participate in the trip.",
                fontWeight = FontWeight.Normal,
                fontSize = 4.3.em
            )
        }
    }
}


@Composable
fun Step2ImportExistingScreen(
    selectedTrip: Travel?,
    onTripSelected: (Travel) -> Unit,
    navController: NavController,
    viewModel: TravelViewModels = viewModel(),
) {

    val tripsList by viewModel.travels.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var selectedLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedActivities by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var selectedAdults by rememberSaveable { mutableIntStateOf(0) }
    var selectedChildren by rememberSaveable { mutableIntStateOf(0) }
    var selectedExperience by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selminPrice by rememberSaveable { mutableFloatStateOf(0f) }
    var selmaxPrice by rememberSaveable { mutableFloatStateOf(9999f) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }

    val filteredTrips by remember {
        derivedStateOf {
            tripsList.filter { trip ->
                val matchesQuery = query.isBlank() ||
                        trip.title.contains(query, ignoreCase = true) ||
                        extractCityAndCountry(trip.destinations.first().address.fullAddress).contains(query, ignoreCase = true)

                val matchesLocation = selectedLocation.isNullOrEmpty() ||
                        extractCityAndCountry(trip.destinations.first().address.fullAddress) == selectedLocation

                val matchesActivities = selectedActivities.isEmpty() ||
                        trip.activities.any { selectedActivities.contains(it.name) }

                val matchesTypes = selectedExperience.isEmpty() ||
                        selectedExperience.any { type -> trip.experiences.any { it.name.equals(type, ignoreCase = true) } }

                val matchesPrice = trip.pricePerPerson in selminPrice..selmaxPrice

                val groupSize = trip.maxParticipants.adults + trip.maxParticipants.children
                val matchesGroupSize = groupSize >= (selectedAdults + selectedChildren)

                matchesQuery && matchesLocation && matchesActivities && matchesTypes && matchesPrice && matchesGroupSize
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (tripsList.isEmpty()) {
            // mostra loader centrale mentre carica
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        } else {
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

                items(filteredTrips) { trip ->
                    TripCard(
                        trip = trip,
                        isSelected = selectedTrip?.id == trip.id,
                        onClick = { onTripSelected(trip) },
                        viewTrip = { navController.navigate("trip_page/${trip.id}/${false}") }
                    )
                }
            }

            if (showFilter) {
                FilterScreenStep2(
                    initialAdults = selectedAdults,
                    initialChildren = selectedChildren,
                    initialMinPrice = selminPrice,
                    initialMaxPrice = selmaxPrice,

                    initialExperience = selectedExperience,
                    initialActivity = selectedActivities,
                    onClose = { showFilter = false },
                    onReset = {
                        selectedAdults = 0
                        selectedChildren = 0
                        selectedExperience = emptyList()
                        selminPrice = 0f
                        selmaxPrice = 9999f

                    },
                    onApply = { adults, children, types, activities, minPrice, maxPrice ->
                        selectedAdults = adults
                        selectedChildren = children
                        selectedExperience = types
                        selminPrice = minPrice
                        selmaxPrice = maxPrice
                        selectedActivities = activities
                    }
                )
            }
        }
    }

}





@Composable
fun TripCard(
    trip: Travel,
    isSelected: Boolean,
    onClick: () -> Unit,
    viewTrip: () -> Unit
) {
    var creator by remember {mutableStateOf<DataProfile?>(null)  }

    LaunchedEffect(trip.userId) {
        creator = ProfileRepository.getProfileByInternalId(trip.userId)
    }
    val borderColor = if (isSelected) DarkGreen20 else Color.LightGray


    if (creator == null) {
        EmptyCard()
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(

                    ){
                        if (creator != null) {
                            AsyncImage(
                                model = creator!!.img,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        if (creator != null) {
                            Text(
                                text = creator!!.name +" "+ creator!!.surname,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "organizer",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            val pagerState = rememberPagerState(pageCount = { trip.images.size })
            val coroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) { page ->
                    AsyncImage(
                        model = trip.images[page],
                        contentDescription = "Travel Image $page",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()

                    )
                }

                if (trip.images.size > 1){
                    val totalPages = pagerState.pageCount
                    val currentPage = pagerState.currentPage
                    val maxVisibleDots = 5
                    val halfVisible = maxVisibleDots / 2

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in -halfVisible..halfVisible) {
                            val pageIndex = currentPage + i

                            if (pageIndex in 0 until totalPages) {
                                val distance = kotlin.math.abs(i)
                                val scale = when (distance) {
                                    0 -> 0.6f
                                    1 -> 0.6f
                                    2 -> 0.6f
                                    else -> 0.6f
                                }

                                val alpha = when (distance) {
                                    0 -> 1.0f
                                    1 -> 0.8f
                                    2 -> 0.6f
                                    else -> 0.3f
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size((8.dp * scale).coerceAtLeast(4.dp))
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = alpha))
                                )
                            } else {
                                Spacer(modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(6.dp))
                            }
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = trip.title,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = trip.description,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "€${trip.pricePerPerson}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "per person",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
                ElevatedButton(
                    onClick = { viewTrip() },
                    colors = ButtonColors(
                        containerColor = DarkGreen20,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreenStep2(
    initialAdults: Int,
    initialChildren: Int,
    initialMinPrice: Float,
    initialMaxPrice: Float,
    initialExperience: List<String>,
    initialActivity : List<String>,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onApply: (
        adults: Int,
        children: Int,
        experiences: List<String>,
        activities: List<String>,
        minPrice: Float,
        maxPrice: Float,
    ) -> Unit
) {
    var localAdults by remember { mutableIntStateOf(initialAdults) }
    var localChildren by remember { mutableIntStateOf(initialChildren) }
    var localActivities by remember { mutableStateOf(initialActivity) }
    var localExperiences by remember { mutableStateOf(initialExperience) }
    var localMinPrice by remember { mutableFloatStateOf(initialMinPrice) }
    var localMaxPrice by remember { mutableFloatStateOf(initialMaxPrice) }

    val acvVM: ActivityViewModel = viewModel()
    val allActivities = acvVM.activities.collectAsState()

    val expVM: ExperienceViewModel = viewModel()
    val experienceList = expVM.experiences.collectAsState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .zIndex(.1f)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null)
            }

            Text("Filter", style = MaterialTheme.typography.titleLarge)
            Box(modifier = Modifier.size(48.dp)) {} // empty box for balance
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // Group Size
        Text("Group Size", style = MaterialTheme.typography.titleMedium)
        Column {
            NumberPicker("Adults", localAdults) { localAdults = it }
            NumberPicker("Children", localChildren) { localChildren = it }
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // Activities
        Text("Activities", style = MaterialTheme.typography.titleMedium)
        ActivityChips(
            allActivities = allActivities.value,
            selectedActivities = localActivities,
            onToggle = { name ->
                localActivities = localActivities.toMutableList().apply {
                    if (contains(name)) remove(name) else add(name)
                }
            }
        )

        Divider(Modifier.padding(vertical = 16.dp))

        // Experiences (ex "types")
        Text("Experiences", style = MaterialTheme.typography.titleMedium)
        ExperienceChips(
            allExperiences = experienceList.value,
            selectedExperiences = localExperiences,
            onToggle = { id ->
                localExperiences = localExperiences.toMutableList().apply {
                    if (contains(id)) remove(id) else add(id)
                }
            }
        )

        Divider(Modifier.padding(vertical = 16.dp))

        // Price Range
        Text("Price Range", style = MaterialTheme.typography.titleMedium)
        RangeSlider(
            value = localMinPrice..localMaxPrice,
            onValueChange = { range ->
                localMinPrice = range.start
                localMaxPrice = range.endInclusive
            },
            valueRange = 0f..9999f
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${localMinPrice.toInt()}€")
            Text("${localMaxPrice.toInt()}€")
        }

        Divider(Modifier.padding(vertical = 16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                localAdults = 0
                localChildren = 0
                localActivities = emptyList()
                localExperiences = emptyList()
                localMinPrice = 0f
                localMaxPrice = 9999f
                onReset()
            }) {
                Text("Reset All")
            }
            Button(onClick = {
                onApply(
                    localAdults,
                    localChildren,
                    localExperiences,
                    localActivities,
                    localMinPrice,
                    localMaxPrice
                )
                onClose()
            }) {
                Text("Apply", color = Color.White)
            }
        }
    }
}
