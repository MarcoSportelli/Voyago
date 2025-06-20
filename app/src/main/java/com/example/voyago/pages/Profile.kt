package com.example.voyago.pages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voyago.Experience
import com.example.voyago.MainActivity
import com.example.voyago.PastTravel
import com.example.voyago.PreferredDestination
import com.example.voyago.R
import com.example.voyago.SignInActivity
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.components.CardProfile
import com.example.voyago.viewModels.ProfileViewModel
import com.example.voyago.components.Contact
import com.example.voyago.components.DesiredDestinationSection
import com.example.voyago.components.ExperiencesSeek
import com.example.voyago.viewModels.ReviewRepository
import com.example.voyago.components.PastTravelExperience
import com.example.voyago.profileViewModel
import com.example.voyago.viewModels.DestinationRepository
import com.example.voyago.viewModels.ExperienceViewModel
import com.example.voyago.viewModels.PastTravelRepository
import com.example.voyago.viewModels.PastTravelViewModel
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.PrimaryColor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    addTravel: () -> Unit,
    navController: NavHostController,
    ) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        screenWidth < 840.dp -> 24.dp
        else -> 32.dp
    }

    val titleFontSize = when {
        screenWidth < 360.dp -> 28.sp
        screenWidth < 600.dp -> 32.sp
        else -> 36.sp
    }

    val profile by profileViewModel.profile.collectAsState()

    LaunchedEffect(Unit) {
        profile?.let { profileViewModel.reloadProfileByInternalId(it.id) }
    }


    var isLoading by remember { mutableStateOf(true) }
    var pastTravels by remember { mutableStateOf<List<PastTravel>>(emptyList()) }
    val travelVM = TravelViewModels()
    var tripsList by remember { mutableStateOf<List<Travel>>(emptyList()) }
    val reviewVM = ReviewViewModel()
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var preferredDestinations by remember { mutableStateOf<List<PreferredDestination>>(emptyList()) }
    var preferredExperiences by remember { mutableStateOf<List<Experience>>(emptyList()) }

    LaunchedEffect(profile) {
        profile?.let {
            isLoading = true
            pastTravels = PastTravelRepository.getPastTravelsByUserId(it.id)
            tripsList = travelVM.getTravelsByUser(it.id.toString())
            val tripIds = tripsList.map { trip -> trip.id }
            reviews = reviewVM.getAllReviewsOfUser(tripIds)
            val destList = DestinationRepository.getAllDestinations()
            preferredDestinations = profile!!.prefDest.mapNotNull { destId ->
                destList.find { it.id == destId }?.let {
                    PreferredDestination(
                        id = it.id,
                        name = it.name,
                        icon = it.icon,
                        imageUrl = it.imageUrl
                    )
                }
            }

            val expList = ExperienceViewModel().getAllExperiences()
            preferredExperiences = profile!!.prefExp.mapNotNull { expId ->
                expList.find { it.id == expId }?.let {
                    Experience(
                        id = it.id,
                        name = it.name,
                        icon = it.icon
                    )
                }
            }
            isLoading = false
        }
    }

    if (profile == null || isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val averageRating = if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else 0f
        val listState = rememberLazyListState()

        val showCompactTopBar by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(
                        top = if (showCompactTopBar) 56.dp else 94.dp,
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = 8.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CardProfile(
                        profile = profile!!,
                        reviewCount = reviews.size,
                        reviewScore = averageRating,
                    )
                }

                item { AddTravel(onClick = { addTravel() }) }

                item {
                    Contact(
                        email = profile!!.email,
                        phone = profile!!.phone,
                        instagram = profile!!.instagram,
                        facebook = profile!!.facebook
                    )
                }
                if (pastTravels.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Past Experience:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            LazyRow {
                                items(pastTravels) { pastTravel ->
                                    PastTravelExperience(
                                        travelId = pastTravel.travelId,
                                        userId = profile!!.id,
                                        onClick={navController.navigate("trip_page/${pastTravel.travelId}/${false}")}
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (preferredExperiences.isNotEmpty()){
                    item {
                        ExperiencesSeek(selectedActivities = preferredExperiences)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (preferredDestinations.isNotEmpty()) {
                    item {
                        DesiredDestinationSection(destination = preferredDestinations)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                item {
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(context, SignInActivity::class.java)
                            context.startActivity(intent)
                            (context as AppCompatActivity).finish()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .height(50.dp),

                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonColors(
                            containerColor = DarkGreen20,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.Black
                        ),
                    ) {
                        Text("Logout",
                            color = Color.White
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = showCompactTopBar,
                transitionSpec = {
                    fadeIn(tween(300)) with fadeOut(tween(300))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) { isCompact ->
                ProfileTopBar(
                    isCompact = isCompact,
                    onNotificationClick = { navController.navigate("notification") },
                    modifier = Modifier
                        .background(Color.White)
                )
            }
        }


    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileTopBar(
    isCompact: Boolean,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isCompact,
        transitionSpec = {
            if (targetState) {
                // Quando diventa compatto: slide in dal basso, slide out verso l'alto
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                // Quando diventa esteso: slide in dall'alto, slide out verso il basso
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }
        },
        modifier = modifier.fillMaxWidth()
    ) { compact ->
        if (compact) {
            // Modalità compatta: testo + icona sulla stessa riga, senza elevazione
            Box{
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifiche",
                            tint = Color.Black
                        )
                    }
                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Color.Gray,
                    thickness = 1.dp
                )
            }
        } else {
            // Modalità estesa: icona sopra con elevazione, testo sotto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 6.dp, // elevazione tipo Material 3
                        shadowElevation = 6.dp, // per compatibilità
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifiche",
                                tint = Color.Black
                            )
                        }
                    }
                }
                Text(
                    text = "Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}








@Composable
fun AddTravel(
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = "Organize a trip",
                    fontSize = 5.em,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "It's easy to organize a trip and make money from it",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Image(
                painter = painterResource(id = R.drawable.bg_hiking),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
