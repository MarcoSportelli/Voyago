package com.example.voyago.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.PreferredDestination
import com.example.voyago.viewModels.ActivityViewModel
val allDestinations = listOf(
    PreferredDestination("tokyo", "https://.../tokyo.jpg", "Tokyo", "🗼"),
    PreferredDestination("parigi", "https://.../parigi.jpg", "Parigi", "🧑🏻‍🍳"),
    PreferredDestination("rome", "https://.../rome.jpg", "Rome", "🏛️"),
    PreferredDestination("london", "https://.../london.jpg", "London", "🇬🇧")
)

@Composable
fun DestinationsSeek(
    selectedDestinations: List<PreferredDestination>,
    isEdit: Boolean = false,
    onSelectionChanged: (List<PreferredDestination>) -> Unit = {}
) {
    val currentSelection = remember { mutableStateListOf<PreferredDestination>() }

    LaunchedEffect(selectedDestinations) {
        currentSelection.clear()
        currentSelection.addAll(selectedDestinations)
    }


    val activitiesToShow = if (isEdit) allDestinations else selectedDestinations
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 16.dp, start = 10.dp, end = 10.dp)
    ) {
        if (!isEdit){
            Text(
                "Experiences Seek:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            items(activitiesToShow) { destination ->
                val isSelected = currentSelection.any { it.name == destination.name }

                DestinationCard (
                    destination = destination,
                    isSelected = isSelected,
                    isEdit = isEdit,
                    onClick = {
                        if (isEdit) {
                            if (isSelected) {
                                currentSelection.removeAll { it.name == destination.name }
                            } else {
                                currentSelection.add(destination)
                            }
                            onSelectionChanged(currentSelection.toList())
                        }
                    }
                )
            }
        }
    }
}


