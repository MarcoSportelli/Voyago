package com.example.voyago.pages.createtravel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.viewModels.TravelViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun Step14Screen(viewModel: TravelViewModel) {
    val travel by viewModel.travel.collectAsState()
    var price by remember { mutableStateOf("0.00") }
    var pricePerPerson by remember { mutableStateOf("0.00") }

    // Inizializzazione
    LaunchedEffect(travel.pricePerPerson) {
        if (price == "0.00") { // Solo se non è stato modificato
            val participants = travel.maxParticipants
            val totalParticipants = participants.adults + participants.children + participants.newborns
            val calculatedTotalPrice = travel.pricePerPerson * totalParticipants
            price = "%.2f".format(calculatedTotalPrice).replace(',', '.')
            pricePerPerson = "%.2f".format(travel.pricePerPerson).replace(',', '.')
        }
    }

    // Calcolo prezzo per persona
    LaunchedEffect(price) {
        val cleanPrice = price.replace(',', '.').toDoubleOrNull() ?: 0.0
        val participants = travel.maxParticipants
        val totalParticipants = participants.adults + participants.children + participants.newborns

        val calculatedPricePerPerson = if (totalParticipants > 0) cleanPrice / totalParticipants else cleanPrice

        pricePerPerson = "%.2f".format(calculatedPricePerPerson).replace(',', '.')
        viewModel.updatePrice(calculatedPricePerPerson)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Now it's time to set the price",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "You can change it at any time",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W300,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        TextField(
            value = if (price.isEmpty()) "0.00" else price,
            onValueChange = { newValue ->
                if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    price = newValue
                }
                if (newValue.matches(Regex("^\\d+$"))) {
                    price = "$newValue.00"
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 64.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W600,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
        )
        Text(
            text = "Price per person: $pricePerPerson€",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W300,
        )
    }
}