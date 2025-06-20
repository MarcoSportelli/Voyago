package com.example.voyago.pages

import BookedScreen
import OrganizedScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun MyTravelProposalScreen(
    userId: Int,
    navController: NavHostController,
    initialTab: String = "Booked"
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Booked",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { selectedTab = "Booked" },
                fontSize = 24.sp,
                fontWeight = if (selectedTab == "Booked") FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (selectedTab == "Booked") TextDecoration.Underline else null,
                color = if (selectedTab == "Booked") Color.Black else Color.Gray
            )

            Text(
                text = "Organized",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { selectedTab = "Organized" },
                fontSize = 24.sp,
                fontWeight = if (selectedTab == "Organized") FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (selectedTab == "Organized") TextDecoration.Underline else null,
                color = if (selectedTab == "Organized") Color.Black else Color.Gray
            )
        }

        when (selectedTab) {
            "Booked" -> BookedScreen(userId = userId, navController = navController)
            "Organized" -> OrganizedScreen(userId = userId, navController = navController)
        }
    }
}
