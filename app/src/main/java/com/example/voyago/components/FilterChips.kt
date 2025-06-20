package com.example.voyago.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    locations: List<String>,
    selectedLocation: String?,
    onLocationSelected: (String?) -> Unit,
    activities: List<String>,
    selectedActivities: List<String>,
    onActivityToggle: (String) -> Unit,
    clearActivities: () -> Unit,
    selectedPriceRange: Pair<Float, Float>,
    onPriceRangeChange: (Pair<Float, Float>) -> Unit,
    selectedDuration: Int,  // Single value for duration
    onDurationChange: (Int) -> Unit, // Single value for duration
    selectedGroupSize: Int,  // Single value for group size
    onGroupSizeChange: (Int) -> Unit // Single value for group size
) {
    var locationExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var priceExpanded by remember { mutableStateOf(false) }
    var durationExpanded by remember { mutableStateOf(false) }
    var groupSizeExpanded by remember { mutableStateOf(false) }
    val columnModifier = Modifier
        .wrapContentWidth()
        .padding(vertical = 4.dp)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        FilterChip(
            modifier = columnModifier,
            selected = selectedFilter == "All",
            onClick = {
                onFilterSelected("All")
                onLocationSelected(null)
                clearActivities()
            },
            label = { Text("All") }
        )
        Column (modifier = columnModifier){
            FilterChip(
                selected = selectedFilter == "Location" || selectedLocation != null,
                onClick = {
                    onFilterSelected("Location")
                    locationExpanded = !locationExpanded
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedLocation ?: "Location")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(if (locationExpanded) 180f else 0f)
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            onLocationSelected(location)
                            locationExpanded = false
                        }
                    )
                }
            }
        }
        Column (modifier = columnModifier){
            FilterChip(
                selected = selectedFilter == "Activity" || selectedActivities.isNotEmpty(),
                onClick = {
                    onFilterSelected("Activity")
                    activityExpanded = !activityExpanded
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (selectedActivities.isEmpty()) "Activities"
                            else selectedActivities.joinToString(limit = 1, truncated = " +${selectedActivities.size - 1}")
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(if (activityExpanded) 180f else 0f)
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = activityExpanded,
                onDismissRequest = { activityExpanded = false }
            ) {
                activities.forEach { activity ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedActivities.contains(activity),
                                    onCheckedChange = null
                                )
                                Text(activity)
                            }
                        },
                        onClick = { onActivityToggle(activity) }
                    )
                }
            }
        }
        /*

        Price range -> non funziona il Range Slider (follia)

        Column(modifier = columnModifier) {
            FilterChip(
                selected = selectedPriceRange.first != 0f || selectedPriceRange.second != 500f,
                onClick = {
                    priceExpanded = !priceExpanded
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Price Range")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(if (priceExpanded) 180f else 0f)
                        )
                    }
                }
            )

            if (priceExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Price Range: ${selectedPriceRange.first.toInt()} - ${selectedPriceRange.second.toInt()}")

                    RangeSlider(
                        value = selectedPriceRange.first..selectedPriceRange.second,
                        onValueChange = { newRange ->
                            onPriceRangeChange(newRange.start to newRange.endInclusive)
                        },
                        valueRange = 0f..500f,
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(64.dp)
                    )
                }
            }
        }

        */

        Column(modifier = columnModifier) {
            FilterChip(
                selected = selectedFilter == "Duration",
                onClick = {
                    onFilterSelected("Duration")
                    durationExpanded = !durationExpanded
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Duration (Days)")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(if (durationExpanded) 180f else 0f)
                        )
                    }
                }
            )
            if (durationExpanded) {
                Row(
                    modifier = Modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (selectedDuration > 1) onDurationChange(selectedDuration - 1) }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Decrease")
                    }
                    Text(text = "$selectedDuration")
                    IconButton(onClick = { onDurationChange(selectedDuration + 1) }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Increase")
                    }
                }
            }
        }
        Column(modifier = columnModifier) {
            FilterChip(
                selected = selectedFilter == "Group Size",
                onClick = {
                    onFilterSelected("Group Size")
                    groupSizeExpanded = !groupSizeExpanded
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Group Size")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(if (groupSizeExpanded) 180f else 0f)
                        )
                    }
                }
            )
            if (groupSizeExpanded) {
                Row(
                    modifier = Modifier
                    .fillMaxWidth() ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (selectedGroupSize > 1) onGroupSizeChange(selectedGroupSize - 1) }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Decrease")
                    }
                    Text(text = "$selectedGroupSize")
                    IconButton(onClick = { onGroupSizeChange(selectedGroupSize + 1) }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Increase")
                    }
                }
            }
        }
    }
}

