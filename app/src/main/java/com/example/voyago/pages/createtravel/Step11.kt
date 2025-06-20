package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
fun Step11Screen(viewModel: TravelViewModel){

    var text by remember { mutableStateOf(viewModel.travel.value.title) }

    val combinedNextClick = {
        viewModel.updateTitle(text)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Now let's give the trip a title",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "Short titles are the most effective. Don't worry, you can always change it later.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W300,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        CharacterCounterTextField(
            value = text,
            onValueChange = { text = it ; viewModel.updateTitle(text) },
            label = "Title",
            placeholder = "Insert a title...",
            maxCharacters = 64,
            modifier = Modifier.padding(16.dp),
            modifier2 = Modifier
                .fillMaxWidth()
        )
    }

}
