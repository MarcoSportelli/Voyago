package com.example.voyago.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Review
import com.example.voyago.Travel
import com.example.voyago.repositories.ImageRepository
import com.example.voyago.repositories.ImageRepositoryReview
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.utils.ComposeFileProvider
import com.example.voyago.utils.ProfileImage
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.ReviewViewModel
import com.example.voyago.viewModels.TravelViewModels
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PastTravelExperience(userId: Int, travelId: Int, onClick: ()->Unit) {
    val travelVM = TravelViewModels()
    var trip by remember { mutableStateOf<Travel?>(null) }
    var creator by remember { mutableStateOf<DataProfile?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(travelId) {
        trip = travelVM.getTravelById(travelId)
        trip?.let {
            creator = ProfileRepository.getProfileByInternalId(it.userId)
        }
    }

    // Placeholder grigio mentre carica
    if (trip == null || creator == null) {
        EmptyCard2()
        return
    }

    // Contenuto normale quando tutto è caricato
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(250.dp)
            .padding(8.dp),
        onClick = onClick

    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val firstImage = trip!!.images.firstOrNull()
            if (firstImage != null) {
                AsyncImage(
                    model = firstImage,
                    contentDescription = "Travel Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No Image", color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trip!!.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = trip!!.description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileImage(profile = creator!!, modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${creator!!.name} ${creator!!.surname}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Organizer",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen20,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Review",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showDialog) {
        FullReviewDialog(
            onDismiss = { showDialog = false },
            onSubmit = { reviewText, rating, images ->
                val newReview = Review(
                    id = -1,
                    travelId = travelId,
                    userId = userId,
                    reviewText = reviewText,
                    date = LocalDate.now().toString(),
                    rating = rating,
                    images = images
                )
                val reviewVM = ReviewViewModel()
                reviewVM.addReview(newReview)
                showDialog = false
                Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}



@Composable
fun EmptyCard2(){
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(250.dp)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(12.dp)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(10.dp)
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .width(70.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray)
            )
        }
    }
}

@Composable
fun FullReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Int, List<String>) -> Unit
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    val images = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (reviewText.isNotBlank() && rating > 0) {
                        onSubmit(reviewText, rating, images)
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Cancel")
            }
        },
        title = {
            Text(text = "Leave a Review with Images")
        },
        text = {
            Column {

                ImagesRow(
                    images = images,
                    onAddImage = { url -> images.add(url) },
                    onRemoveImage = { index ->
                        scope.launch {
                            ImageRepositoryReview.deleteImage(images[index])
                            images.removeAt(index)
                        }
                    }
                )


                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write your experience...") },
                    label = { Text("Review") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i stars",
                                tint = if (i <= rating) Color(0xFFF9A825) else Color.Gray
                            )
                        }
                    }
                }

            }
        }
    )
}


@Composable
fun ImagesRow(
    images: List<String>,
    onAddImage: (String) -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val imageUrl = ImageRepositoryReview.uploadImage(context, it)
                imageUrl?.let { url ->
                    onAddImage(url)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri.value?.let {
                scope.launch {
                    val imageUrl = ImageRepositoryReview.uploadImage(context, it)
                    imageUrl?.let { url ->
                        onAddImage(url)
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            imagePicker.launch("image/*")
                            showDialog.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val uri = ComposeFileProvider.getImageUri(context)
                            imageUri.value = uri
                            cameraLauncher.launch(uri)
                            showDialog.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo", color = Color.White)
                    }
                }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(images.size) { index ->
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = images[index],
                    contentDescription = "Travel Image $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (8).dp, y = -8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .clickable { onRemoveImage(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Image",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .clickable { showDialog.value = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Image",
                    modifier = Modifier.size(40.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }
}
