package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.voyago.Activity
import com.example.voyago.viewModels.ActivityViewModel
import com.example.voyago.viewModels.TravelViewModel
import kotlin.collections.chunked
import kotlin.collections.forEach

@Composable
fun Step9Screen(
    activityViewModel: ActivityViewModel,
    travelViewModel: TravelViewModel
) {
    val activityList by activityViewModel.activities.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()
    val errorMessage by activityViewModel.errorMessage.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Let users know about all the activities that are included in the trip.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(screenHeight * 0.02f))

        Text(
            "You can add more after publishing.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W300,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(screenHeight * 0.02f))

        when {
            isLoading -> Text("Loading activities...")
            errorMessage != null -> Text("Error: $errorMessage")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(activityList.chunked(2)) { row ->
                        Row {
                            row.forEach { act ->
                                ActivityButton(activity = act, viewModel = travelViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ActivityButton(
    activity: Activity,
    viewModel: TravelViewModel
) {
    val currentTravel by viewModel.travel.collectAsState()
    val isSelected = currentTravel.activities.contains(activity)

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val buttonWidth = screenWidth * 0.42f
    val buttonHeight = screenHeight * 0.18f
    val padding = screenWidth * 0.04f

    Box(
        modifier = Modifier
            .size(buttonWidth, buttonHeight)
            .padding(padding)
            .background(Color.White)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.Black else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = if (!isSelected) 4.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
    ) {
        Button(
            onClick = {
                if (isSelected) viewModel.removeActivity(activity)
                else viewModel.addActivity(activity)
            },
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = activity.icon, fontSize = screenHeight.value.times(0.03).sp)
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                Text(text = activity.name, fontSize = screenHeight.value.times(0.018f).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
