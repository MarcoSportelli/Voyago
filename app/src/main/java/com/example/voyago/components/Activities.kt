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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.Activity
import com.example.voyago.Experience
import com.example.voyago.viewModels.ActivityViewModel
import com.example.voyago.viewModels.ExperienceViewModel

@Composable
fun Activities(
    selectedActivities: List<Activity>,
    isEdit: Boolean = false,
    onSelectionChanged: (List<Activity>) -> Unit = {}
) {
    val currentSelection = remember { mutableStateListOf<Activity>() }

    LaunchedEffect(selectedActivities) {
        currentSelection.clear()
        currentSelection.addAll(selectedActivities)
    }

    val acvVM = ActivityViewModel()
    var allActivities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var activitiesToShow by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isLoading = true
        allActivities = acvVM.getAllActivities()
        activitiesToShow = if (isEdit) allActivities else selectedActivities
        isLoading = false
    }

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
            items(activitiesToShow) { activity ->
                val isSelected = currentSelection.any { it.name == activity.name }

                ActivityCard(
                    activity = activity,
                    isSelected = isSelected,
                    isEdit = isEdit,
                    onClick = {
                        if (isEdit) {
                            if (isSelected) {
                                currentSelection.removeAll { it.name == activity.name }
                            } else {
                                currentSelection.add(activity)
                            }
                            onSelectionChanged(currentSelection.toList())
                        }
                    }
                )
            }
        }
    }
}