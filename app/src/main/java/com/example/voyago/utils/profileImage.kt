package com.example.voyago.utils

import android.provider.ContactsContract.RawContacts.Data
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.voyago.DataProfile
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.LighGreen20

@Composable
fun ProfileImage(profile:DataProfile, modifier: Modifier){
    if (!profile.img.isNullOrEmpty()) {
        AsyncImage(
            model = profile.img,
            contentDescription = "Image profile",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .background(LighGreen20),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.username.firstOrNull()?.uppercase() ?: "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}