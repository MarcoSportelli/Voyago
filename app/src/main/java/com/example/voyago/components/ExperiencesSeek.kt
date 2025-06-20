package com.example.voyago.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voyago.Experience
import com.example.voyago.viewModels.DestinationRepository
import com.example.voyago.viewModels.ExperienceViewModel

@Composable
fun ExperiencesSeek(
    selectedActivities: List<Experience>,
    isEdit: Boolean = false,
    onSelectionChanged: (List<Experience>) -> Unit = {}
) {
    val currentSelection = remember { mutableStateListOf<Experience>() }

    LaunchedEffect(selectedActivities) {
        currentSelection.clear()
        currentSelection.addAll(selectedActivities)
    }

    var allPreferredExperiences by remember { mutableStateOf<List<Experience>>(emptyList()) }
    var activitiesToShow by remember { mutableStateOf<List<Experience>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        allPreferredExperiences = ExperienceViewModel().getAllExperiences()
        activitiesToShow = if (isEdit) allPreferredExperiences else selectedActivities
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp, end = 10.dp)
    ) {
        if (!isEdit) {
            Text(
                "Experiences Seek:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activitiesToShow) { activity ->
                    val isSelected = currentSelection.any { it.name == activity.name }

                    ExperienceCard(
                        experience = activity,
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
}
