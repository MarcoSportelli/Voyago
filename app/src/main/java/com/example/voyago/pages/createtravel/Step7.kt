package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.components.ParticipantsRow
import com.example.voyago.viewModels.TravelViewModel

@Composable
fun Step7Screen(
    viewModel: TravelViewModel
) {
    val travel by viewModel.travel.collectAsState()

    val travelParticipants = travel.maxParticipants

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "How many people can participate in the trip?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        ParticipantsRow(
            participantType = "Adults",
            count = travelParticipants.adults,
            onClickPlus = {
                viewModel.updateParticipants(
                    travel.maxParticipants.copy(
                        adults = travel.maxParticipants.adults + 1
                    )
                )
            },
            onClickMinus = {
                if (travel.maxParticipants.adults > 0) {
                    viewModel.updateParticipants(
                        travel.maxParticipants.copy(
                            adults = travel.maxParticipants.adults - 1
                        )
                    )
                }
            },
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.Black
        )

        ParticipantsRow(
            participantType = "Children",
            count = travelParticipants.children,
            onClickPlus = {
                viewModel.updateParticipants(
                    travel.maxParticipants.copy(
                        children = travel.maxParticipants.children + 1
                    )
                )
            },
            onClickMinus = {
                if (travel.maxParticipants.children > 0) {
                    viewModel.updateParticipants(
                        travel.maxParticipants.copy(
                            children = travel.maxParticipants.children - 1
                        )
                    )
                }
            },
        )


    }

}
