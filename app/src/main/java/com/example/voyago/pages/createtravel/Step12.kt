package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.components.CharacterCounterTextField
import com.example.voyago.viewModels.TravelViewModel


@Composable
fun Step12Screen(viewModel: TravelViewModel){
    var text by remember { mutableStateOf(viewModel.travel.value.description) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Write a description",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "Tell us what makes your trip special",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W300,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        CharacterCounterTextField(
            value = text,
            onValueChange = { text = it ; viewModel.updateDescription(text) },
            label = "Description",
            placeholder = "Insert a description...",
            modifier = Modifier
                .padding(16.dp),
            modifier2 = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }

}
