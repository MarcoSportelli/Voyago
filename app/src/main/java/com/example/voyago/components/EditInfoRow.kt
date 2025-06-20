package com.example.voyago.components

import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.Activity
import com.example.voyago.PreferredDestination
import com.example.voyago.Experience
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInfoRow(
    fieldName: String,
    value: String,
    error: String? = null,
    modifier: Modifier = Modifier,
    onEdit: (String) -> Unit = {},
    onSave: () -> Unit,
    experiences: List<Experience> = emptyList(),
    onExperiencesUpdate: (List<Experience>) -> Unit = {},
    destinations: List<PreferredDestination> = emptyList(),
    onDestinationsUpdate: (List<PreferredDestination>) -> Unit = {},
    activities: List<Activity> = emptyList(),
    onActivitiesUpdate: (List<Activity>) -> Unit = {},
    isComplexField: Boolean = false,
    isPrefDest: Boolean = false,

    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (editValue, setEditValue) = remember { mutableStateOf(value) }
    val (localError, setLocalError) = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    suspend fun reverseGeocode(latLng: LatLng): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()

                    if (address.thoroughfare != null) addressParts.add(address.thoroughfare)
                    if (address.subThoroughfare != null) addressParts.add(address.subThoroughfare)
                    if (address.locality != null) addressParts.add(address.locality)
                    if (address.adminArea != null) addressParts.add(address.adminArea)
                    if (address.postalCode != null) addressParts.add(address.postalCode)
                    if (address.countryName != null) addressParts.add(address.countryName)

                    addressParts.joinToString(", ")
                } else {
                    "${latLng.latitude}, ${latLng.longitude}"
                }
            } catch (e: IOException) {
                "${latLng.latitude}, ${latLng.longitude}"
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = fieldName,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                    )
                    error?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Column {
                    Button(
                        onClick = {
                            setExpanded(!expanded)
                            setLocalError(null)
                        },
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = if (expanded) "Cancel" else "Edit",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            color = Color.White
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    when {
                        !isComplexField -> {
                            OutlinedTextField(
                                value = editValue,
                                onValueChange = {
                                    setEditValue(it)
                                    setLocalError(null)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = keyboardType,
                                    imeAction = imeAction
                                ),
                                isError = localError != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, top = 16.dp)
                            )
                            localError?.let {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        isPrefDest -> {
                            DestinationsSeek(
                                selectedDestinations = destinations,
                                isEdit = true,
                                onSelectionChanged = onDestinationsUpdate
                            )
                        }

                        fieldName == "Destination Address" -> {
                            var searchQuery by remember { mutableStateOf(editValue) }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    setLocalError(null)
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = keyboardType,
                                    imeAction = imeAction
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            setEditValue(searchQuery)
                                            if (searchQuery.isEmpty()) {
                                                currentLocation = null
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = "Cerca indirizzo")
                                    }
                                },
                                isError = localError != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Or select a point on the map:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            DraggableLocationMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                address = editValue,
                                onLocationChanged = { latLng ->
                                    currentLocation = latLng
                                    coroutineScope.launch {
                                        val address = reverseGeocode(latLng)
                                        setEditValue(address)
                                        searchQuery = address
                                    }
                                },
                                isEditMode = true,
                            )
                        }

                        fieldName == "Preferred Experiences" -> {
                            ExperiencesSeek(
                                selectedActivities = experiences,
                                isEdit = true,
                                onSelectionChanged = onExperiencesUpdate
                            )
                        }

                        fieldName == "Experiences" -> {
                            ExperiencesSeek(
                                selectedActivities = experiences,
                                isEdit = true,
                                onSelectionChanged = onExperiencesUpdate
                            )
                        }

                        fieldName == "Start Date" || fieldName == "End Date" -> {
                            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
                            val parsedDate = try {
                                LocalDate.parse(editValue, formatter)
                            } catch (e: Exception) {
                                LocalDate.now()
                            }

                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = parsedDate.toEpochDay() * 24 * 60 * 60 * 1000
                            )

                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )

                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                val selectedDate = LocalDate.ofEpochDay(selectedDateMillis / (24 * 60 * 60 * 1000))
                                setEditValue(selectedDate.format(formatter))
                            }

                            Text(
                                text = "Selected: $editValue",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        fieldName == "Activities" -> {
                            Activities(
                                selectedActivities = activities,
                                isEdit = true,
                                onSelectionChanged = onActivitiesUpdate
                            )
                        }

                        else -> {
                            Text("Component not implemented yet for: $fieldName")
                        }
                    }
                    Button(
                        onClick = {
                            if (!isComplexField && editValue.isBlank()) {
                                setLocalError("This field is mandatory")
                                return@Button
                            }
                            setExpanded(false)
                            onEdit(editValue)
                            onSave()
                        },
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}