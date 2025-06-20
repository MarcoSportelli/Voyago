package com.example.voyago.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.R

@Composable
fun Contact(phone: String, email: String, facebook: String, instagram: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Contact:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ContactRow(icon = R.drawable.phone, label = "Phone", value = phone){

                }
                ContactRow(icon = R.drawable.gmail, label = "Email", value = email){

                }
                ContactRow(icon = R.drawable.facebook, label = "Facebook", value = facebook){

                }
                ContactRow(icon = R.drawable.instagram, label = "Instagram", value = instagram){

                }
            }
        }
    }
}

@Composable
fun ContactRow(icon: Int, label: String, value: String, onClick:()->Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, fontSize = 14.sp, fontFamily = FontFamily.SansSerif, modifier = Modifier.clickable { onClick() })

    }
}