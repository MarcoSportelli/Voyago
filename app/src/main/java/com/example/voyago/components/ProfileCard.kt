package com.example.voyago.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.ui.theme.LighGreen20
import com.example.voyago.utils.ProfileImage
import com.example.voyago.utils.yearsSince

@Composable
fun CardProfile(
    profile: DataProfile,
    reviewCount: Int,
    reviewScore: Float,
) {
    val years = yearsSince(profile.memberSince)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(160.dp)
            ) {
                if (!profile.img.isNullOrEmpty()) {
                    AsyncImage(
                        model = profile.img,
                        contentDescription = "Image profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .background(LighGreen20),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.username.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color =Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    text = profile.name + " " + profile.surname,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    text = profile.username,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            CardProfileInfo(reviewCount, reviewScore, years)
        }
    }
}

@Composable
fun CardProfileInfo(reviewCount: Int, reviewScore: Float, years: Int) {
    Column(
        modifier = Modifier.padding(start = 40.dp)
    ) {
        Text(
            text = "$reviewCount",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            "Reviews",
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(
                text = "%.1f".format(reviewScore),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(imageVector = Icons.Filled.Star, contentDescription = "Star")
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            "Score",
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "$years",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            "years of organizer",
            fontSize = 10.sp
        )
    }
}