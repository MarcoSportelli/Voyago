
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voyago.DataProfile
import com.example.voyago.Travel
import com.example.voyago.TripRequest
import com.example.voyago.components.RequestCard
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.RequestsViewModel
import com.example.voyago.viewModels.TravelViewModels

@Composable
fun OrganizedScreen(userId: Int, navController: NavHostController) {
    val requestsVM: RequestsViewModel = viewModel()
    val travelVM: TravelViewModels = viewModel()

    var profile by remember { mutableStateOf<DataProfile?>(null) }
    var tripsList by remember { mutableStateOf<List<Travel>>(emptyList()) }

    var isTripsLoading by remember { mutableStateOf(true) }
    var isRequestsLoading by remember { mutableStateOf(true) }

    var requestState by remember { mutableStateOf<List<TripRequest>>(emptyList()) }

    LaunchedEffect(userId) {
        profile = ProfileRepository.getProfileByInternalId(userId)

        isTripsLoading = true
        tripsList = travelVM.getTravelsByUser(userId.toString())
        isTripsLoading = false

        isRequestsLoading = true
        requestState = requestsVM.getRequestsReceiver(userId)
        isRequestsLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = "Trip", style = MaterialTheme.typography.titleLarge)

        when {
            isTripsLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            tripsList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "No trip organized", color = Color.Gray)
                }
            }
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tripsList) { trip ->
                        TripCardMini(
                            tripId = trip.id,
                            modifier = Modifier,
                            onClick = {
                                navController.navigate("trip_page/${trip.id}/true")
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Requests", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (isRequestsLoading || profile == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val filteredRequests = requestState.filter { it.receiverId == profile!!.id }

                if (filteredRequests.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(text = "No requests", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredRequests) { request ->
                        RequestCard(
                            request = request,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navController.navigate("profile/${request.senderId}")
                            }
                        )
                    }
                }
            }
        }
    }

}

