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
import com.example.voyago.Experience
import com.example.voyago.viewModels.ExperienceViewModel
import com.example.voyago.viewModels.TravelViewModel

@Composable
fun Step3Screen(
    experienceViewModel: ExperienceViewModel,
    travelViewModel: TravelViewModel
) {
    val experienceList by experienceViewModel.experiences.collectAsState()
    val isLoading by experienceViewModel.isLoading.collectAsState()
    val errorMessage by experienceViewModel.errorMessage.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Which of these options best describes the type of trip you will be taking?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(screenHeight * 0.02f))

        when {
            isLoading -> Text("Loading experiences...")
            errorMessage != null -> Text("Error: $errorMessage")
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(experienceList.chunked(2)) { row ->
                        Row {
                            row.forEach { exp ->
                                TypeButton(experience = exp, viewModel = travelViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TypeButton(
    experience: Experience,
    viewModel: TravelViewModel
) {
    val currentTravel by viewModel.travel.collectAsState()
    val isSelected = currentTravel.experiences.contains(experience)

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
                if (isSelected) viewModel.removeType(experience)
                else viewModel.addType(experience)
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
                Text(text = experience.icon, fontSize = screenHeight.value.times(0.03).sp)
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                Text(text = experience.name, fontSize = screenHeight.value.times(0.018f).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
