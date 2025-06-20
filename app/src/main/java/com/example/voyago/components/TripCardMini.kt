import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.viewModels.ReviewRepository
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels

@Composable
fun TripCardMini(tripId: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val reviewVM = ReviewViewModel()
    val travelVM = TravelViewModels()

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var trip by remember { mutableStateOf<Travel?>(null) }
    var imageLoaded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

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
        // Placeholder mentre carica
        Column(
            modifier = modifier
                .padding(8.dp)
        ) {
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
        // Contenuto reale dopo il caricamento
        Column(
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(8.dp)
                .width(160.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = trip!!.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 20.sp
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
    }
}
