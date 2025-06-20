package com.example.voyago.pages

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.voyago.Activity
import com.example.voyago.Destination
import com.example.voyago.R
import com.example.voyago.Travel
import com.example.voyago.components.AddDestinationButton
import com.example.voyago.components.AddressRow
import com.example.voyago.components.CharacterCounterTextField
import com.example.voyago.components.DraggableLocationMap
import com.example.voyago.components.FilterChipsRow
import com.example.voyago.components.NavigationBar
import com.example.voyago.components.NavigationBarPublish
import com.example.voyago.viewModels.TravelViewModel
import com.example.voyago.components.TopAppBar
import com.example.voyago.pages.createtravel.*
import com.example.voyago.viewModels.ActivityViewModel
import com.example.voyago.viewModels.ExperienceViewModel
import com.example.voyago.viewModels.TravelViewModels

@Composable
fun CreateNewTravelScreen(
    viewModel: TravelViewModel,
    navController: NavController,
    travelMVVM: TravelViewModels = viewModel(),
) {
    val travel by viewModel.travel.collectAsState()
    var selectedTrip by remember { mutableStateOf<Travel?>(null) }
    val tripsList by travelMVVM.travels.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var showSearchScreen by remember { mutableStateOf(false) }
    var importedFromExisting by remember { mutableStateOf(false) }
    var areStagesEmpty by remember { mutableStateOf(travel.destinations.size >= 2) }

    // Aggiungiamo una variabile per tenere traccia del percorso
    var navigationPath by remember { mutableStateOf<List<Int>>(emptyList()) }

    val onNextClick: () -> Unit = {
        navigationPath = navigationPath + currentStep // Aggiungi lo step corrente al percorso
        currentStep++
    }

    val onBackClick: () -> Unit = {
        when {
            currentStep == 0 -> navController.popBackStack()

            // Se stiamo tornando indietro dallo step 4
            currentStep == 4 -> {
                currentStep = if (importedFromExisting) {
                    // Se veniamo da un viaggio importato, torniamo allo step 2
                    2
                } else {
                    // Altrimenti torniamo allo step 3 (creazione da zero)
                    3
                }
            }

            currentStep == 5 && showSearchScreen -> {
                showSearchScreen = false
            }
            currentStep == 6 && showSearchScreen -> {
                showSearchScreen = false
            }
            currentStep == 16 -> {
                currentStep = 15
            }
            currentStep == 3 -> currentStep -= 2
            else -> currentStep--
        }

        // Rimuovi l'ultimo step dal percorso quando torni indietro
        if (navigationPath.isNotEmpty()) {
            navigationPath = navigationPath.dropLast(1)
        }
    }

    Scaffold(
        modifier = Modifier
            .background(Color.White),
        topBar = {
            TopAppBar(
                screen = "CreateNewTravelProposal",
                finish = { navController.popBackStack("profile", false) },
                onClean = {
                    navController.popBackStack("profile", false)
                    viewModel.restart()
                }

            )
        },
        bottomBar = {
            when {
                currentStep == 0 -> StartButtonBar(
                    onStartClick = {
                        navigationPath = listOf(0) // Inizializza il percorso
                        currentStep = 1
                    }
                )
                currentStep == 2 -> NavigationBar(
                    onNextClick = {
                        selectedTrip?.let { trip ->
                            viewModel.updateTitle(trip.title)
                            viewModel.updateDescription(trip.description)
                            viewModel.updatePrice(trip.pricePerPerson)
                            trip.activities.forEach { viewModel.addActivity(it) }
                            trip.experiences.forEach { viewModel.addType(it) }
                            trip.images.forEach {
                                if (it != null) {
                                    viewModel.addImage(it)
                                }
                            }
                            viewModel.updateParticipants(trip.maxParticipants)
                            viewModel.updateDestinations(trip.destinations)
                            viewModel.updatePrice(trip.pricePerPerson)
                            importedFromExisting = true
                            currentStep = 4
                            navigationPath = navigationPath + 2
                        }
                    },
                    onBackClick = onBackClick,
                    pressable = selectedTrip != null
                )
                currentStep == 3 -> NavigationBar(
                    onNextClick = {
                        importedFromExisting = false // Impostiamo il flag
                        currentStep = 4
                        navigationPath = navigationPath + 3 // Aggiungiamo lo step 3 al percorso
                    },
                    onBackClick = onBackClick,
                    pressable = true
                )
                currentStep in 4..15 -> NavigationBar(
                    onNextClick = onNextClick,
                    onBackClick = onBackClick,
                    pressable = isStepValid(currentStep, travel, showSearchScreen, areStagesEmpty, importedFromExisting)
                )
                currentStep == 16 -> NavigationBarPublish(
                    onNextClick = {
                        viewModel.publishTravel(tripsList.size)
                        travelMVVM.addTravel(viewModel.travel.value)
                        viewModel.restart()
                        navController.popBackStack()
                    },
                    onBackClick = onBackClick
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentStep) {
                0 -> Step0Screen()
                1 -> Step1Screen(
                    onStartFromScratchClick = {
                        currentStep = 3
                        navigationPath = listOf(0, 1) // Aggiorna il percorso
                    },
                    onImportClick = {
                        currentStep = 2
                        navigationPath = listOf(0, 1) // Aggiorna il percorso
                    }
                )
                2 -> Step2ImportExistingScreen(
                    selectedTrip = selectedTrip,
                    onTripSelected = { selectedTrip = it },
                    navController = navController
                )
                3 -> Step2FromScratchScreen()
                4 -> Step3Screen(experienceViewModel= ExperienceViewModel(), viewModel)
                5 -> Step4Screen(
                    viewModel = viewModel,
                    showSearchScreen = showSearchScreen,
                    onClick = { showSearchScreen = !showSearchScreen }
                )
                6 -> Step5Screen(
                    viewModel, showSearchScreen,
                    onClick = { showSearchScreen = !showSearchScreen },
                    onChangeDestinations = { areStagesEmpty = !areStagesEmpty }
                )
                7 -> Step6Screen(viewModel)
                8 -> Step7Screen(viewModel)
                9 -> Step8Screen(viewModel)
                10 -> Step9Screen(activityViewModel = ActivityViewModel(), viewModel)
                11 -> Step10Screen(viewModel)
                12 -> Step11Screen(viewModel)
                13 -> Step12Screen(viewModel)
                14 -> Step13Screen()
                15 -> Step14Screen(viewModel)
                16 -> Step15Screen(viewModel)
            }
        }
    }
}


@Composable
fun StartButtonBar(onStartClick: () -> Unit) {
    Surface(
        tonalElevation = 4.dp,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun isStepValid(
    step: Int,
    travel: Travel,
    showSearchScreen: Boolean,
    areStagesEmpty: Boolean,
    isImported: Boolean
): Boolean {
    return when (step) {
        4 -> travel.experiences.isNotEmpty()
        5 -> travel.destinations.isNotEmpty() && !showSearchScreen
        6 -> {
            if (isImported) true
            else areStagesEmpty && !showSearchScreen
        }
        7 -> travel.startDate.isNotEmpty() && travel.endDate.isNotEmpty()
        8 -> travel.maxParticipants.adults > 0 ||
                travel.maxParticipants.children > 0 ||
                travel.maxParticipants.newborns > 0
        10 -> travel.activities.isNotEmpty()
        11 -> travel.images.isNotEmpty()
        12 -> travel.title.isNotEmpty()
        13 -> travel.description.isNotEmpty()
        15 -> travel.pricePerPerson > 0
        else -> true
    }
}
