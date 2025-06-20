package com.example.voyago.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.voyago.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voyago.Experience
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.LighGreen20
import com.example.voyago.viewModels.ActivityViewModel
import com.example.voyago.viewModels.ExperienceViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    initialAdults: Int,
    initialChildren: Int,
    initialMinPrice: Float,
    initialMaxPrice: Float,
    initialStartDate: LocalDate,
    initialEndDate: LocalDate?,
    initialExperience: List<String>,
    initialActivity : List<String>,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onApply: (
        adults: Int,
        children: Int,
        experiences: List<String>,
        activities: List<String>,
        minPrice: Float,
        maxPrice: Float,
        startDate: LocalDate,
        endDate: LocalDate?,

    ) -> Unit
) {
    var localAdults by remember { mutableIntStateOf(initialAdults) }
    var localChildren by remember { mutableIntStateOf(initialChildren) }
    var localActivities by remember { mutableStateOf(initialActivity) }
    var localExperiences by remember { mutableStateOf(initialExperience) }
    var localMinPrice by remember { mutableFloatStateOf(initialMinPrice) }
    var localMaxPrice by remember { mutableFloatStateOf(initialMaxPrice) }
    var localStartDate by remember { mutableStateOf(initialStartDate) }
    var localEndDate by remember { mutableStateOf(initialEndDate) }

    Log.d("FilterScreen", "Initial Experiences: $initialExperience")
    Log.d("FilterScreen", "Initial Activities: $initialActivity")

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var showDatePicker by remember { mutableStateOf(false) }

    val acvVM: ActivityViewModel = viewModel()
    val allActivities = acvVM.activities.collectAsState()

    val expVM: ExperienceViewModel = viewModel()
    val experienceList = expVM.experiences.collectAsState()

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { start, end ->
                localStartDate = start
                localEndDate = end
                showDatePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .zIndex(.1f)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null)
            }

            Text("Filter", style = MaterialTheme.typography.titleLarge)
            Box(modifier = Modifier.size(48.dp)) {} // empty box for balance
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // Group Size
        Text("Group Size", style = MaterialTheme.typography.titleMedium)
        Column {
            NumberPicker("Adults", localAdults) { localAdults = it }
            NumberPicker("Children", localChildren) { localChildren = it }
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // Activities
        Text("Activities", style = MaterialTheme.typography.titleMedium)
        ActivityChips(
            allActivities = allActivities.value,
            selectedActivities = localActivities,
            onToggle = { name ->
                localActivities = localActivities.toMutableList().apply {
                    if (contains(name)) remove(name) else add(name)
                }
            }
        )

        Divider(Modifier.padding(vertical = 16.dp))

        // Experiences (ex "types")
        Text("Experiences", style = MaterialTheme.typography.titleMedium)
        ExperienceChips(
            allExperiences = experienceList.value,
            selectedExperiences = localExperiences,
            onToggle = { id ->
                localExperiences = localExperiences.toMutableList().apply {
                    if (contains(id)) remove(id) else add(id)
                }
            }
        )

        Divider(Modifier.padding(vertical = 16.dp))

        // Price Range
        Text("Price Range", style = MaterialTheme.typography.titleMedium)
        RangeSlider(
            value = localMinPrice..localMaxPrice,
            onValueChange = { range ->
                localMinPrice = range.start
                localMaxPrice = range.endInclusive
            },
            valueRange = 0f..9999f
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${localMinPrice.toInt()}€")
            Text("${localMaxPrice.toInt()}€")
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // Date Range
        Text("Date Range", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = if (localEndDate != null) {
                "${localStartDate.format(formatter)} - ${localEndDate?.format(formatter)}"
            } else {
                localStartDate.format(formatter)
            },
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            }
        )

        Divider(Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                localAdults = 0
                localChildren = 0
                localActivities = emptyList()
                localExperiences = emptyList()
                localMinPrice = 0f
                localMaxPrice = 9999f
                localStartDate = LocalDate.now()
                localEndDate = null
                onReset()
            }) {
                Text("Reset All")
            }
            Button(onClick = {
                onApply(
                    localAdults,
                    localChildren,
                    localExperiences,
                    localActivities,
                    localMinPrice,
                    localMaxPrice,
                    localStartDate,
                    localEndDate,
                )
                onClose()
            }) {
                Text("Apply", color = Color.White)
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate, LocalDate) -> Unit
) {
    val context = LocalContext.current
    val datePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                val start = datePickerState.selectedStartDateMillis?.let { Instant.ofEpochMilli(it).atZone(
                    ZoneId.systemDefault()).toLocalDate() }
                val end = datePickerState.selectedEndDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                if (start != null && end != null) {
                    onDateSelected(start, end)
                } else {
                    onDismissRequest()
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(state = datePickerState)
    }
}


@Composable
fun ExperienceChips(
    allExperiences: List<Experience>, // Sostituisci con il tuo modello reale
    selectedExperiences: List<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allExperiences, key = { it.id }) { experience ->
            FilterChip(
                selected = selectedExperiences.contains(experience.id),
                onClick = { onToggle(experience.id) },
                label = { Text("${experience.icon} ${experience.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LighGreen20,
                    selectedLabelColor = Color.White
                )

            )
        }
    }
}

@Composable
fun ActivityChips(
    allActivities: List<Activity>,
    selectedActivities: List<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allActivities, key = { it.name }) { activity ->
            FilterChip(
                selected = selectedActivities.contains(activity.name),
                onClick = { onToggle(activity.name) },
                label = { Text("${activity.icon} ${activity.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LighGreen20, // LightGreen20 (puoi sostituirlo con il tuo valore esatto)
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}


@Composable
fun NumberPicker(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                onClick = { if (value > 0) onValueChange(value - 1) }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Decrease",
                    modifier = Modifier.padding(8.dp),
                    tint = Color.Black
                )
            }

            Text(
                text = value.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                onClick = { onValueChange(value + 1) }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Increase",
                    modifier = Modifier.padding(8.dp),
                    tint = Color.Black
                )
            }
        }
    }
}
