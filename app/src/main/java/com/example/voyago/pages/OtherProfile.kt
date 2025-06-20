package com.example.voyago.pages

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voyago.DataProfile
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.components.AboutSection
import com.example.voyago.components.CardProfile
import com.example.voyago.components.ReviewCard
import com.example.voyago.components.TripCard
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels


@Composable
fun OtherProfileScreen(navController: NavHostController, profileId: Int) {


    var profile by remember {mutableStateOf<DataProfile?>(null)  }
    val travelVM = TravelViewModels()
    var tripsList  by remember { mutableStateOf<List<Travel>>(emptyList()) }

    LaunchedEffect(profileId) {
        tripsList = travelVM.getTravelsByUser(profileId.toString())
        profile = ProfileRepository.getProfileByInternalId(profileId)
    }
    val tripListId = tripsList.map { it.id }


    val reviewVM = ReviewViewModel()
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    LaunchedEffect(tripListId) {
        reviews = reviewVM.getAllReviewsOfUser(tripListId)
    }
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average().toFloat()
    } else 0f

    if (profile != null) {
        LazyColumn(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
        ) {
            item {
                ProfileSection(
                    profile = profile!!,
                    averageRating = averageRating,
                    reviewCount = reviews.size
                )
            }
            item {
                Box(Modifier.padding(10.dp)){
                    Divider()
                }
            }
            item {
                ReviewsSection(reviews = reviews, navController = navController, profileId = profileId)
            }
            item {
                Box(Modifier.padding(10.dp)){
                    Divider()
                }
            }
            item {
                AboutSection(profile = profile!!)
            }
            item {
                Box(Modifier.padding(10.dp)){
                    Divider()
                }
            }
            item {
                TripsSection(userTrips = tripsList, navController = navController)
            }
        }
    } else {
        // Optional: show loading spinner or message
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

}

@Composable
fun ProfileSection(profile: DataProfile, averageRating: Float, reviewCount: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        CardProfile(
            profile = profile,
            reviewScore = averageRating,
            reviewCount = reviewCount,
        )
    }
}

@Composable
fun ReviewsSection(reviews: List<Review>, navController: NavHostController, profileId :Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet.",
                fontSize = 18.sp,
                color = Color.Gray
            )
        } else {
            ReviewCard(
                reviews = reviews,
                onClick = {
                    navController.navigate("review_page/${profileId}/${true}")
                }
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray)
                        .border(1.dp, Color.Black)
                        .clickable(onClick = {
                            navController.navigate("review_page/${profileId}/${true}")
                        })
                ) {
                    Text(
                        text = "Show all ${reviews.size} reviews",
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center).padding(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun TripsSection(userTrips: List<Travel?>, navController: NavHostController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Organized trips (${userTrips.size})", // Display trip count
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(userTrips) { trip ->
                Box(
                    modifier = Modifier
                        .width(340.dp)
                        .padding(vertical = 8.dp)
                ) {
                    if (trip != null) {
                        TripCard(
                            userId = 2,
                            trip = trip,
                            viewTrip = {
                                navController.navigate("trip_page/${trip.id}/${false}")
                            }
                        )
                    }
                }
            }
        }
    }
}
