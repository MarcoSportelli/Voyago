package com.example.voyago.pages

import TripCardMini
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.viewModels.FavoriteRepository
import com.example.voyago.viewModels.FavoriteViewModel
import com.example.voyago.viewModels.ReviewRepository
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels

@Composable
fun FavoriteScreen(userId: Int, navController: NavHostController) {
    val favoriteVM: FavoriteViewModel = viewModel()
    val favoriteTrips by favoriteVM.favoriteList.collectAsState()

    LaunchedEffect(userId) {
        favoriteVM.getUserFavoriteTravelIds(userId)
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
        Text(
            "Favorite",
            fontSize = 36.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )
        if (favoriteTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorite trips",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(favoriteTrips.chunked(2)) { tripPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (tripId in tripPair) {
                            TripCardMini(
                                tripId = tripId.travelId,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    navController.navigate("trip_page/${tripId.travelId}/false")
                                },
                                removeTravel = { travelId ->
                                    favoriteVM.deleteFavorite(userId,travelId)
                                }
                            )
                        }
                        if (tripPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun TripCardMini(
    tripId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    removeTravel: (Int) -> Unit
) {
    val reviewVM = ReviewViewModel()
    val travelVM = TravelViewModels()

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var trip by remember { mutableStateOf<Travel?>(null) }

    LaunchedEffect(tripId) {
        reviews = reviewVM.getReviewsByTripId(tripId)
        trip = travelVM.getTravelById(tripId)
    }

    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average().toFloat()
    } else 0f

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (trip != null) {
                AsyncImage(
                    model = trip!!.images[0],
                    contentDescription = "Travel Image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Grigio placeholder
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
            }

            ElevatedButton(
                onClick = {
                    trip?.let { removeTravel(it.id) }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .padding(horizontal = 8.dp)
                    .size(24.dp),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remove from favorites",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            if (trip != null) {
                Text(
                    text = trip!!.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "%.1f".format(averageRating),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = " • ${reviews.size} reviews",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                // Placeholder per il testo
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            }
        }
    }
}
