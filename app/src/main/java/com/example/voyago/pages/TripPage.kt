package com.example.voyago.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.core.tween
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Participants
import com.example.voyago.RequestStatus
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.TripRequest
import com.example.voyago.components.CardProfile
import com.example.voyago.components.DeleteConfirmationPopup
import com.example.voyago.components.DraggableLocationMap
import com.example.voyago.components.ReviewCard
import com.example.voyago.components.TripDestinationsTimeline
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.utils.ProfileImage
import com.example.voyago.utils.yearsSince
import com.example.voyago.viewModels.BookedViewModel
import com.example.voyago.viewModels.FavoriteViewModel
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.ProfileViewModel
import com.example.voyago.viewModels.RequestsViewModel
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelRepository
import com.example.voyago.viewModels.TravelViewModels
import com.example.voyago.viewModels.TripRequestRepository
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Request
enum class RequestState {
    NOT_SENT,
    SENDING,
    SENT,
    ERROR
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TripPageController(
    tripId: Int,
    userId: Int = -1,
    isPersonal: Boolean,
    onBack: () -> Unit,
    gotoProfile: (profileId: Int) -> Unit,
    modifier: Modifier = Modifier,
    navController : NavHostController
) {
    var showPopup by remember { mutableStateOf(false) }
    var showBookingPopup by remember { mutableStateOf(false) }
    var adultsCount by remember { mutableIntStateOf(0) }
    var childrenCount by remember { mutableIntStateOf(0) }

    // Aggiungi stato locale per la richiesta
    var localRequestState by remember {
        mutableStateOf<RequestState>(RequestState.NOT_SENT)
    }



    // INIT
    val fireBaseUser = FirebaseAuth.getInstance().currentUser
    val travelVM = TravelViewModels()
    val profileVM = ProfileViewModel()
    val bookedVM = BookedViewModel()
    val requestVM = RequestsViewModel()
    val favoriteVM: FavoriteViewModel = viewModel()
    val reviewVM = ReviewViewModel()
    val reciverReviewVM = ReviewViewModel()

    var trip by remember { mutableStateOf<Travel?>(null) }
    var currentUser by remember { mutableStateOf<DataProfile?>(null) }
    var receiver by remember { mutableStateOf<DataProfile?>(null) }

    var requests by remember { mutableStateOf<List<TripRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasSentRequest by remember { mutableStateOf(false) }
    var isAlreadyBooked by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    var favoriteIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var tripsList by remember { mutableStateOf<List<Travel>>(emptyList()) }
    var reciverReviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    // Average ratings
    val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else 0f
    val receiveraverageRating = if (reciverReviews.isNotEmpty()) reciverReviews.map { it.rating }.average().toFloat() else 0f
    var maxParticipants by remember { mutableIntStateOf(0) }
    var currentParticipants by remember { mutableIntStateOf(0) }
    var isSoldOut by remember { mutableStateOf(false) }
    // EFFECT: Load all needed data
    LaunchedEffect(fireBaseUser, tripId) {
        if (fireBaseUser == null) return@LaunchedEffect

        isLoading = true

        // Load user profile
        currentUser = profileVM.getProfileByUserId(fireBaseUser.uid)

        // Load trip
        trip = travelVM.getTravelById(tripId)
        if (trip != null && currentUser != null) {
            // Load receiver (trip owner)
            receiver = profileVM.getProfileByInternalId(trip!!.userId)
            maxParticipants = trip!!.maxParticipants.adults + trip!!.maxParticipants.children

            // Load accepted requests for this trip
            val acceptedRequests = TripRequestRepository.getRequestsByTripId(tripId)
                .filter { it.status == RequestStatus.ACCEPTED }

            // Calculate current participants
            currentParticipants = acceptedRequests.sumOf {
                it.adults + it.children
            }

            // Check if trip is sold out
            isSoldOut = currentParticipants >= maxParticipants
            // Load user favorites
            val favIds = favoriteVM.getUserFavoriteTravelIds(currentUser!!.id)
            favoriteIds = favIds
            isFavorite = favIds.contains(tripId)

            // Load trip reviews
            reviews = reviewVM.getReviewsByTripId(tripId)

            // Load trip requests
            val userRequests = TripRequestRepository.getRequestsBySenderUserId(currentUser!!.id)
            hasSentRequest = userRequests.any { it.senderId == currentUser!!.id && it.tripId == tripId }

            val bookedTrips = bookedVM.getUserBookedTravelIds(currentUser!!.id)
            isAlreadyBooked = bookedTrips.contains(tripId)

            // Load all trips by receiver
            val receiverTrips = travelVM.getTravelsByUser(trip!!.userId.toString())
            tripsList = receiverTrips

            // Collect reviews for receiver's trips
            val tripIds = receiverTrips.map { it.id }
            reciverReviews = reciverReviewVM.getAllReviewsOfUser(tripIds)

        }

        isLoading = false
    }
// Modifica la derivazione dello stato del bottone
    val isButtonEnabled by remember {
        derivedStateOf {
            !isAlreadyBooked &&
            !hasSentRequest &&
            localRequestState != RequestState.SENDING &&
            !isSoldOut
        }
    }

// Modifica la logica per determinare il testo del bottone
    val bookButtonLabel by remember {
        derivedStateOf {
            when {
                isSoldOut -> "Sold Out"
                isAlreadyBooked -> "Booked"
                localRequestState == RequestState.SENDING -> "Sending..."
                localRequestState == RequestState.SENT || hasSentRequest -> "Request Sent"
                else -> "Book"
            }
        }
    }

// Aggiorna la logica di invio della richiesta
    val onConfirmBooking = {
        val totalRequested = adultsCount + childrenCount
        val remainingCapacity = maxParticipants - currentParticipants


        localRequestState = RequestState.SENDING
        val participant = Participants(adultsCount, childrenCount, 0)
        receiver?.let { rec ->
            currentUser?.let { user ->
                try {
                    requestVM.addNewRequest(
                        tripId = trip!!.id,
                        sender = user,
                        receiver = rec,
                        participants = participant
                    ).also {
                        // Aggiorna solo lo stato locale se non è già booked
                        if (!isAlreadyBooked) {
                            localRequestState = RequestState.SENT
                            hasSentRequest = true
                        }
                    }
                } catch (e: Exception) {
                    if (!isAlreadyBooked) {
                        localRequestState = RequestState.ERROR
                    }
                } finally {
                    showBookingPopup = false
                }
            }
        }
    }

    if (isLoading || trip == null) {
        // Skeleton loader
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dates placeholder
            Row {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating placeholder
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Description placeholder
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Activities placeholder
            repeat(3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
        return
    }
    else
    {
        val listState = rememberLazyListState()

        val showCompactTopBar by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 500
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ){
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = if (!isPersonal) 100.dp else 0.dp)
            ) {
                item {
                    val images = trip!!.images.filterNotNull()
                    val pagerState = rememberPagerState(pageCount = { images.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = "Travel Image $page",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = "${pagerState.currentPage + 1}/${images.size}",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trip!!.title,
                                fontSize = 36.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(3f),
                                lineHeight = 1.em
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = trip!!.destinations.first().address.fullAddress,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            Text(
                                text = "From:",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = trip!!.startDate,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "To:",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = trip!!.endDate,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = "star")
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "%.1f".format(averageRating),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${reviews.size} reviews",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                receiver?.id?.let { gotoProfile(it) }
                            }
                        ) {
                            if (receiver != null) {
                                ProfileImage(
                                    profile = receiver!!,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column{
                                if (receiver != null) {
                                    Text(
                                        text = "Name of the organizer: ${receiver!!.name}",
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium
                                    )

                                    val year = yearsSince(receiver!!.memberSince)
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "organizer for $year years",
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = trip!!.description,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "What's included?",
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column {
                            for (activity in trip!!.activities){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = activity.icon,
                                        fontSize = 40.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = activity.name,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Where to go?",
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = trip!!.destinations.first().address.fullAddress,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                        ) {
                            DraggableLocationMap(
                                modifier = Modifier.matchParentSize(),
                                address = trip!!.destinations.first().address.fullAddress
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Trip Steps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TripDestinationsTimeline(trip!!.destinations)
                    }
                }

                if (reviews.size >= 1){
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .background(Color.LightGray)
                                .border(1.dp, Color.Black)
                        ){
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Filled.Star, contentDescription = "star")
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "%.1f".format(averageRating),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${reviews.size} reviews",
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = TextDecoration.Underline
                                    )

                                }
                                Spacer(Modifier.height(20.dp))
                                if (reviews.isNotEmpty()) {
                                    ReviewCard(
                                        reviews = reviews,
                                        onClick = {
                                            navController.navigate("review_page/${trip!!.id}/${false}")
                                        }
                                    )
                                } else {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No reviews",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }

                                }

                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ElevatedCard(
                                        onClick = {
                                            navController.navigate("review_page/${trip!!.id}/${false}")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE0E0E0)) // light gray
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Show all ${reviews.size} reviews",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp))
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                    ){
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Introducing the organizer",
                            fontSize = 24.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (receiver != null) {
                            CardProfile(
                                profile = receiver!!,
                                reviewCount = reciverReviews.size,
                                reviewScore = receiveraverageRating,
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            if (receiver!!.languages.isNotEmpty()){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Languages",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = receiver!!.languages.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            if(receiver!!.phone.isNotEmpty()){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = receiver!!.phone,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                    }
                }
            }

            DeleteConfirmationPopup(
                showDialog = showPopup,
                onDismiss = { showPopup = false },
                onConfirm = {
                    travelVM.deleteTravel(tripId)
                    showPopup = false
                    onBack()
                }
            )

            when (localRequestState) {
                RequestState.ERROR -> {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { localRequestState = RequestState.NOT_SENT }) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    ) {
                        Text("Failed to send request. Please try again.")
                    }
                }
                RequestState.SENT -> {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text("Request sent successfully!")
                    }
                }
                else -> {}
            }

            if (showBookingPopup) {
                BookingPopup(
                    adultsCount = adultsCount,
                    childrenCount = childrenCount,
                    onAdultsChanged = { adultsCount = it },
                    onChildrenChanged = { childrenCount = it },
                    onDismiss = { showBookingPopup = false },
                    onConfirm = { onConfirmBooking() },
                    remainingCapacity = maxParticipants - currentParticipants // Pass remaining capacity
                )
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

                TopActionBar(
                    isPersonal = isPersonal,
                    tripId = tripId,
                    setShowPopup = { showPopup = it },
                    onBack = { navController.popBackStack() },
                    onEditTrip = { id ->
                        navController.navigate("editTrip/$id")
                     },
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        isFavorite = !isFavorite
                        if (isFavorite){
                            favoriteVM.addFavorite(userId,tripId)
                        }
                        else{
                            favoriteVM.deleteFavorite(userId,tripId)
                        }
                    },
                    isCompact = isCompact,
                    modifier =  if (isCompact) { Modifier.background(Color.White)
                    }
                    else{ Modifier.background(Color.Transparent) }
                )
            }

            if (!isPersonal) {
                BottomBar(
                    trip = trip!!,
                    pressable = isButtonEnabled,  // Passa lo stato corretto
                    isButtonEnabled = isButtonEnabled,
                    buttonLabel = bookButtonLabel,
                    onBookClick = {
                        if (isButtonEnabled) {  // Doppio controllo per sicurezza
                            showBookingPopup = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f)
                )
            } else null
        }
    }


}




@Composable
fun TopActionBar(
    isPersonal: Boolean,
    tripId: Int,
    setShowPopup: (Boolean) -> Unit,
    onBack: () -> Unit,
    onEditTrip: (Int) -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isCompact){
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (isPersonal) {
                Row {
                    ElevatedButton(
                        onClick = { setShowPopup(true) },
                        modifier = Modifier
                            .size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ElevatedButton(
                        onClick = { onEditTrip(tripId) },
                        modifier = Modifier
                            .size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFF9A825),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                ElevatedButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

    }else{
        Box {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isPersonal) {
                    Row {
                        IconButton(
                            onClick = { setShowPopup(true) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { onEditTrip(tripId) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFFF9A825),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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

    }

}


@Composable
fun BottomBar(
    trip: Travel,
    pressable: Boolean = true,
    isButtonEnabled: Boolean,
    buttonLabel: String,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pricePerPerson = trip.pricePerPerson
    val isBookingOrRequested = buttonLabel == "Book"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .drawBehind {
                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )
            }
            .height(100.dp) ,
        horizontalArrangement = if (isBookingOrRequested) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (isBookingOrRequested) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "€$pricePerPerson",
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
        }
        Button(
            onClick = onBookClick,
            enabled = isButtonEnabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen20,
                disabledContainerColor = Color.Gray,
                contentColor = Color.White
            ),
            modifier = Modifier.size(
                width = if (isBookingOrRequested) 120.dp else 200.dp,
                height = if (isBookingOrRequested) 50.dp else 60.dp
            )
        ) {

            Text(
                text = buttonLabel,
                fontSize = 4.em,
                color = Color.White
            )
        }
    }
}


@Composable
fun BookingPopup(
    adultsCount: Int,
    childrenCount: Int,
    onAdultsChanged: (Int) -> Unit,
    onChildrenChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    remainingCapacity: Int = Int.MAX_VALUE // Add this parameter
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Select Number of Participants",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Add remaining capacity info
            Text(
                text = "Remaining spots: $remainingCapacity",
                fontSize = 16.sp,
                color = if (remainingCapacity <= 0) Color.Red else Color.Unspecified,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Adults Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Adults",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (adultsCount > 0) onAdultsChanged(adultsCount - 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = adultsCount > 0 // Disable when at 0
                    ) {
                        Text(
                            "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adultsCount > 0) Color.Black else Color.Gray
                        )
                    }
                    Text(
                        adultsCount.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { onAdultsChanged(adultsCount + 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = (adultsCount + childrenCount) < remainingCapacity // Disable when at capacity
                    ) {
                        Text(
                            "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((adultsCount + childrenCount) < remainingCapacity) Color.Black else Color.Gray
                        )
                    }
                }
            }

            // Children Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Children",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (childrenCount > 0) onChildrenChanged(childrenCount - 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = childrenCount > 0 // Disable when at 0
                    ) {
                        Text(
                            "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (childrenCount > 0) Color.Black else Color.Gray
                        )
                    }
                    Text(
                        childrenCount.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { onChildrenChanged(childrenCount + 1) },
                        modifier = Modifier.size(36.dp),
                        enabled = (adultsCount + childrenCount) < remainingCapacity // Disable when at capacity
                    ) {
                        Text(
                            "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((adultsCount + childrenCount) < remainingCapacity) Color.Black else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { onConfirm() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen20),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    enabled = (adultsCount + childrenCount) > 0 && // At least 1 participant
                            (adultsCount + childrenCount) <= remainingCapacity // Not exceeding capacity
                ) {
                    Text(
                        "Confirm",
                        color = if ((adultsCount + childrenCount) > 0 &&
                            (adultsCount + childrenCount) <= remainingCapacity)
                            Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

