package com.example.voyago.pages

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.voyago.Travel
import com.example.voyago.components.EditInfoRow
import com.example.voyago.repositories.ImageRepository
import com.example.voyago.utils.ComposeFileProvider
import com.example.voyago.viewModels.EditTravelViewModel
import com.example.voyago.viewModels.EditTravelViewModelFactory
import com.example.voyago.viewModels.TravelViewModels
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


fun saveImageFromUri(uri: Uri, context: Context): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "trip_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
@Composable
fun TravelImagesRow(viewModel: EditTravelViewModel , travelVM: TravelViewModels) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val selectedIndex = remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    // Image Pickers
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val imageUrl = ImageRepository.uploadImage(
                    context, it,
                    bucket = ImageRepository.Bucket.TRAVEL
                )
                imageUrl?.let { url ->
                    viewModel.addImage(url)
                    showSaveToast(context, viewModel, travelVM)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri.value?.let {
                scope.launch {
                    val imageUrl = ImageRepository.uploadImage(
                        context, it,
                        bucket = ImageRepository.Bucket.TRAVEL
                    )
                    imageUrl?.let { url ->
                        viewModel.addImage(url)
                        showSaveToast(context, viewModel, travelVM)
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            imagePicker.launch("image/*")
                            showDialog.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val uri = ComposeFileProvider.getImageUri(context)
                            imageUri.value = uri
                            cameraLauncher.launch(uri)
                            showDialog.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo", color = Color.White)
                    }
                }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(viewModel.images.size) { index ->
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = viewModel.images[index],
                    contentDescription = "Travel Image $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                if(viewModel.images.size > 1){
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (8).dp, y = -8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .clickable {
                                scope.launch {
                                    ImageRepository.deleteImage(
                                        viewModel.images[index],
                                        bucket = ImageRepository.Bucket.TRAVEL
                                    )
                                    viewModel.removeImageAt(index)
                                    showSaveToast(context, viewModel, travelVM)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .clickable { showDialog.value = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Image",
                    modifier = Modifier.size(40.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }

}
@Composable
fun EditTravelScreen(tripId: Int) {
    val travelVM = TravelViewModels()
    var trip by remember { mutableStateOf<Travel?>(null) }

    LaunchedEffect(tripId) {
        trip = travelVM.getTravelById(tripId)
    }
    val context = LocalContext.current
    val owner = context as ViewModelStoreOwner


    trip?.let { nonNullTrip ->
        val viewModel: EditTravelViewModel = viewModel(
            key = "edit-travel-${trip!!.id}",
            viewModelStoreOwner = owner,
            factory = EditTravelViewModelFactory(trip!!)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            // Title
            item {
                Text(
                    "Edit Travel Proposal",
                    fontSize = 36.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Images Row
            item {
                TravelImagesRow(viewModel , travelVM)
            }

            // Section Title
            item {
                Text(
                    "Travel Information",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Dynamic Fields
            Travel::class.primaryConstructor?.parameters
                ?.mapNotNull { parameter ->
                    Travel::class.memberProperties.find { it.name == parameter.name }
                }
                ?.forEach { property ->
                    item {
                        when (property.name) {

                            // Skip non-editable or computed fields
                            "id", "createdAt", "creator", "isPublished", "images", "reviewCount", "reviewScore", "types", "destinations" -> {}

                            // Custom fields with specific logic
//                            "destinationAddress" -> {
//                                EditInfoRow(
//                                    fieldName = "Destination Address",
//                                    value = viewModel.getStringValue(property.name),
//                                    onEdit = { viewModel.updateDestinationAddress(it) },
//                                    onSave = { showSaveToast(context, viewModel, travelVM) },
//                                    isComplexField = true
//                                )
//                            }

                            "title", "description" -> {
                                EditInfoRow(
                                    fieldName = property.name.replaceFirstChar { it.uppercase() },
                                    value = viewModel.getStringValue(property.name),
                                    onEdit = { viewModel.updateStringField(property.name, it) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                    keyboardType = KeyboardType.Text
                                )
                            }

                            "pricePerPerson" -> {
                                EditInfoRow(
                                    fieldName = "Price per person",
                                    value = viewModel.getStringValue(property.name),
                                    onEdit = { viewModel.updatePricePerPerson(it.toDouble()) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                )
                            }

                            "startDate" -> {
                                EditInfoRow(
                                    fieldName = "Start Date",
                                    value = viewModel.getStringValue(property.name),
                                    onEdit = { viewModel.updateStartDate(it) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                    isComplexField = true
                                )
                            }

                            "endDate" -> {
                                EditInfoRow(
                                    fieldName = "End Date",
                                    value = viewModel.getStringValue(property.name),
                                    onEdit = { viewModel.updateEndDate(it) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                    isComplexField = true
                                )
                            }

                            "activities" -> {
                                EditInfoRow(
                                    fieldName = "Activities",
                                    value = viewModel.activities.joinToString { it.name },
                                    activities = viewModel.activities,
                                    onActivitiesUpdate = { viewModel.updateActivities(it) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                    isComplexField = true
                                )
                            }
                            "experiences" -> {
                                EditInfoRow(
                                    fieldName = "Experiences",
                                    value = viewModel.experiences.joinToString { it.name },
                                    experiences = viewModel.experiences,
                                    onExperiencesUpdate = { viewModel.updateExperiences(it) },
                                    onSave = { showSaveToast(context, viewModel, travelVM) },
                                    isComplexField = true
                                )
                            }
                        }
                    }
                }
        }
    }?: run {
        CircularProgressIndicator()
    }
}

private fun showSaveToast(context: Context, viewModel: EditTravelViewModel, travelVM: TravelViewModels) {
    val success = viewModel.saveTravel()
    Toast.makeText(
        context,
        if (success) "Travel saved successfully" else "Fix errors before saving",
        Toast.LENGTH_SHORT
    ).show()

    if (success) {
        println(viewModel.travel.value)
        travelVM.updateTravel(viewModel.travel.value)
    }
}

