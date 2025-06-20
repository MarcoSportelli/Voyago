import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.components.DeleteConfirmationPopup
import com.example.voyago.viewModels.BookedRepository
import com.example.voyago.viewModels.BookedViewModel
import com.example.voyago.viewModels.ChatViewModel
import com.example.voyago.viewModels.PastTravelRepository
import com.example.voyago.viewModels.PastTravelViewModel
import com.example.voyago.viewModels.RequestsViewModel

import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun BookedScreen(userId: Int, navController: NavHostController) {

    val bookedMVVM = BookedViewModel()
    var bookedTravel by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        bookedTravel = bookedMVVM.getUserBookedTravelIds(userId)
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        bookedTravel.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No booked trips",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp
                )
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookedTravel.chunked(2)) { tripPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (tripId in tripPair) {
                                TripCardMiniBooked(
                                    userId = userId,
                                    tripId = tripId,
                                    modifier = Modifier.weight(1f),
                                    bookedMVVM = bookedMVVM,
                                    onClick = {
                                        navController.navigate("trip_page/${tripId}/${false}")
                                    },
                                    onRemove = {
                                        bookedTravel = bookedTravel.filter { it != tripId }
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
}

@Composable
fun TripCardMiniBooked(
    userId: Int,
    tripId: Int,
    bookedMVVM: BookedViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit // ✅ nuova callback per comunicare rimozione
) {
    val reviewVM = ReviewViewModel()
    val travelVM = TravelViewModels()
    val requestsVM = RequestsViewModel()
    val chatVM = ChatViewModel()
    val pastTravelVM = PastTravelViewModel()

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var trip by remember { mutableStateOf<Travel?>(null) }
    var imageLoaded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) } // ✅ visibilità animata


    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        isLoading = true
        trip = travelVM.getTravelById(tripId)
        reviews = reviewVM.getReviewsByTripId(tripId)
        isLoading = false
    }
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average().toFloat()
    } else 0f


    if (isLoading || trip == null) {
        // Loading placeholder
        Column(modifier = modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.6f)
                    .background(Color.LightGray)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.4f)
                    .background(Color.LightGray)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    } else {
        Column(
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = trip!!.images.firstOrNull(),
                    contentDescription = "Travel Image",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    onSuccess = { imageLoaded = true },
                    onLoading = { imageLoaded = false }
                )

                if (!imageLoaded) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                }

                ElevatedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .offset(x = 8.dp, y = (-8).dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove from bookings",
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showDialog) {
                    DeleteConfirmationPopup(
                        showDialog = true,
                        onDismiss = { showDialog = false },
                        onConfirm = {
                            bookedMVVM.deleteBooked(userId, trip!!.id)
                            showDialog = false
                            coroutineScope.launch {
                                isVisible = false
                                delay(300) // ⏳ lascia finire l'animazione
                                onRemove() // ❗ comunica al parent di rimuovere la card
                            }
                            requestsVM.deleteRequest(userId, trip!!.id)
                            chatVM.removeUserFromTripChats(userId.toString(), trip!!.id.toString())
                            pastTravelVM.deletePastTravel(userId,trip!!.id)
                        },
                        title = "Remove Booking",
                        message = "Are you sure you want to remove this booking?",
                        confirmText = "Remove",
                        cancelText = "Cancel"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp)) {
                Text(
                    text = trip!!.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
        }
    }}
