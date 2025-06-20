package com.example.voyago.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.ui.theme.LighGreen20
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.ReviewRepository
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPage(id: Int, isUser: Boolean) {
    var averageRating by remember { mutableStateOf(0f) }
    var reviews by remember { mutableStateOf(listOf<Review>()) }
    var tripsList  by remember { mutableStateOf<List<Travel>>(emptyList()) }
    val reviewVM = ReviewViewModel()
    val travelVM = TravelViewModels()
    var profile by remember {mutableStateOf<DataProfile?>(null)  }

    if (isUser) {
        LaunchedEffect(id) {
            profile = ProfileRepository.getProfileByInternalId(id)
        }
        if (profile != null) {
            LaunchedEffect(id) {
                tripsList = travelVM.getTravelsByUser(id.toString())

            }
            val tripListId = tripsList.map { it?.id ?: -1 }


            LaunchedEffect(tripListId) {
                reviews = reviewVM.getAllReviewsOfUser(tripListId)
            }

            if (reviews.isNotEmpty()) {
                averageRating = reviews.map { it.rating }.average().toFloat()
            }
        }
    } else {

        LaunchedEffect(id) {
            reviews = reviewVM.getReviewsByTripId(id)
        }

        if (reviews.isNotEmpty()) {
            averageRating = reviews.map { it.rating }.average().toFloat()
        }
    }


    var expanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Most Recent") }
    var query by remember { mutableStateOf("") }

    val filteredReviews = reviews.filter {
        it.reviewText.contains(query, ignoreCase = true)
    }

    val sortedReviews = when (selectedSort) {
        "Top Rated" -> filteredReviews.sortedByDescending { it.rating }
        "Lowest Rated" -> filteredReviews.sortedBy { it.rating }
        else -> filteredReviews.sortedByDescending { it.date }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.2f", averageRating), style = MaterialTheme.typography.headlineSmall)
                }

                val starCounts = (1..5).associateWith { star -> reviews.count { it.rating == star } }

                for (i in 5 downTo 1) {
                    val count = starCounts[i] ?: 0
                    val percent = if (reviews.size > 0) count / reviews.size.toFloat() else 0f

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("$i", modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = percent,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${reviews.size} reviews", style = MaterialTheme.typography.titleLarge)
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { expanded = true },
                        label = {
                            Text(
                                text = selectedSort,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF3F3F3),
                            labelColor = Color.Black,
                            selectedContainerColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .padding(4.dp)
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                    ) {
                        listOf("Most Recent", "Top Rated", "Lowest Rated").forEach { sortOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sortOption,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedSort = sortOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search in reviews") },
                shape = RoundedCornerShape(36.dp),
                colors = SearchBarDefaults.colors(containerColor = Color.White),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                windowInsets = WindowInsets(0),
                interactionSource = remember { MutableInteractionSource() },
                content = {}
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(sortedReviews) { review ->
            var profile by remember {mutableStateOf<DataProfile?>(null)  }

            LaunchedEffect(review.userId) {
                profile = ProfileRepository.getProfileByInternalId(review.userId)
            }

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!profile?.img.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile?.img,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LighGreen20),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profile?.username?.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("${profile?.name ?: ""} ${profile?.surname ?: ""}", style = MaterialTheme.typography.bodyMedium)
                        Text("Organizer", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        repeat(5) { i ->
                            Icon(
                                imageVector = if (i < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (i < review.rating) Color.Black else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(review.date, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val highlightedText = buildAnnotatedString {
                    val lowerText = review.reviewText.lowercase()
                    val lowerQuery = query.lowercase()
                    var currentIndex = 0

                    if (query.isNotEmpty() && lowerText.contains(lowerQuery)) {
                        while (currentIndex < review.reviewText.length) {
                            val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
                            if (matchIndex == -1) {
                                append(review.reviewText.substring(currentIndex))
                                break
                            }
                            append(review.reviewText.substring(currentIndex, matchIndex))
                            withStyle(SpanStyle(background = Color.Yellow)) {
                                append(review.reviewText.substring(matchIndex, matchIndex + query.length))
                            }
                            currentIndex = matchIndex + query.length
                        }
                    } else {
                        append(review.reviewText)
                    }
                }

                Text(text = highlightedText, style = MaterialTheme.typography.bodyMedium)

                // Images
                if (review.images.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(review.images) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
