package com.example.voyago.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.Travel
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.PrimaryColor
import com.example.voyago.utils.ProfileImage
import com.example.voyago.viewModels.FavoriteRepository
import com.example.voyago.viewModels.FavoriteViewModel
import com.example.voyago.viewModels.ProfileRepository

@Composable
fun TripCard(userId:Int, trip: Travel, viewTrip: () -> Unit){
    
    var creator by remember { mutableStateOf<DataProfile?>(null) }
    LaunchedEffect(trip.userId) {
        creator = ProfileRepository.getProfileByInternalId(trip.userId)
    }    
    
    val favoriteVM: FavoriteViewModel = viewModel()

    var favoriteIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val ids = favoriteVM.getUserFavoriteTravelIds(userId)
        favoriteIds = ids
        isFavorite = ids.contains(trip.id)
    }

    if (creator == null) {
        EmptyCard()
        return
    }

    Card (
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileImage(
                        profile = creator!!,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                    Column{
                        Text(
                            text = creator!!.name +" "+ creator!!.surname,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Organizer",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                IconButton(onClick = {
                    isFavorite = !isFavorite
                    if (isFavorite){
                        favoriteVM.addFavorite(userId,trip.id)
                    }
                    else{
                        favoriteVM.deleteFavorite(userId,trip.id)
                    }
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                    )
                }

            }
            val pagerState = rememberPagerState(pageCount = { trip.images.size })
            val coroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) { page ->
                    AsyncImage(
                        model = trip.images[page],
                        contentDescription = "Travel Image $page",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()

                    )
                }

                if (trip.images.size > 1){
                    val totalPages = pagerState.pageCount
                    val currentPage = pagerState.currentPage
                    val maxVisibleDots = 5
                    val halfVisible = maxVisibleDots / 2

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in -halfVisible..halfVisible) {
                            val pageIndex = currentPage + i

                            if (pageIndex in 0 until totalPages) {
                                val distance = kotlin.math.abs(i)
                                val scale = when (distance) {
                                    0 -> 0.6f
                                    1 -> 0.6f
                                    2 -> 0.6f
                                    else -> 0.6f
                                }

                                val alpha = when (distance) {
                                    0 -> 1.0f
                                    1 -> 0.8f
                                    2 -> 0.6f
                                    else -> 0.3f
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size((8.dp * scale).coerceAtLeast(4.dp))
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = alpha))
                                )
                            } else {
                                Spacer(modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(6.dp))
                            }
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = trip.title,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${trip.startDate}-${trip.endDate}" ,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "€${trip.pricePerPerson}",
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
                ElevatedButton(
                    onClick = { viewTrip() },
                    colors = ButtonColors(
                        containerColor = DarkGreen20,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCard(){
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Placeholder per header (foto + nome)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(120.dp)
                            .background(Color.LightGray)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(80.dp)
                            .background(Color.LightGray)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Placeholder per immagine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Placeholder per titolo
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.6f)
                    .background(Color.LightGray)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder per date
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.4f)
                    .background(Color.LightGray)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Placeholder per prezzo e bottone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(100.dp)
                        .background(Color.LightGray)
                        .clip(RoundedCornerShape(4.dp))
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(80.dp)
                        .background(Color.LightGray)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}