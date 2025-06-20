package com.example.voyago.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Review
import com.example.voyago.utils.ProfileImage
import com.example.voyago.viewModels.ProfileRepository

@Composable
fun ReviewCard(
    reviews: List<Review>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {


        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reviews) { review ->
                ReviewItem(review, onClick)
            }
        }
    }
}
@Composable
fun ReviewItem(review: Review, onClick:()->Unit) {
    var author by remember { mutableStateOf<DataProfile?>(null) }

    LaunchedEffect(review.userId) {
        author = ProfileRepository.getProfileByInternalId(review.userId)
    }


    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .padding(8.dp)
            .width(280.dp)
            .height(250.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StarRating(rating = review.rating)
                        Text(
                            text = review.date,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = review.reviewText,
                        fontSize = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult -> textLayoutResult = layoutResult },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (textLayoutResult?.hasVisualOverflow == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { onClick() }
                        ) {
                            Text(
                                text = "Show More",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black, // Colore del testo nero
                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Show More",
                                tint = Color.Black,
                                modifier = Modifier.rotate(270f) // Ruota l'icona di 180 gradi per farla puntare verso l'alto
                            )
                        }


                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (author != null) {
                        ProfileImage(
                            profile = author!!,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = when {
                                !author?.username.isNullOrBlank() -> author!!.username
                                else -> {
                                    val name = author?.name ?: ""
                                    val surname = author?.surname ?: ""
                                    // Unisci nome e cognome, gestendo se sono entrambi vuoti
                                    if (name.isNotBlank() || surname.isNotBlank()) "$name $surname".trim() else "Unknown"
                                }
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Organizer",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarRating(rating: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Rating",
                tint = if (i <= rating) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}