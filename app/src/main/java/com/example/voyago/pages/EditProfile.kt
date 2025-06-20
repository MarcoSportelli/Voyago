package com.example.voyago.pages

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.voyago.DataProfile
import com.example.voyago.Experience
import com.example.voyago.PreferredDestination
import com.example.voyago.R
import com.example.voyago.components.EditInfoRow
import com.example.voyago.components.TopAppBar
//import com.example.voyago.viewModels.EditProfileViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.reflect.full.memberProperties
import com.example.voyago.databinding.ActivityCameraBinding
import com.example.voyago.repositories.ImageRepository
import com.example.voyago.utils.ImagePicker
import com.example.voyago.viewModels.DestinationRepository
import com.example.voyago.viewModels.ExperienceViewModel
import com.example.voyago.viewModels.PastTravelRepository
import com.example.voyago.viewModels.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService


@Composable
fun EditProfileScreen(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handleSave() {
        if (viewModel.saveProfile()) {
            Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please fix errors before saving", Toast.LENGTH_SHORT).show()
        }
    }
    var preferredDestinations by remember { mutableStateOf<List<PreferredDestination>>(emptyList()) }
    var preferredExperiences by remember { mutableStateOf<List<Experience>>(emptyList()) }


    LaunchedEffect(viewModel) {
        val destList = DestinationRepository.getAllDestinations()
        preferredDestinations = viewModel.prefDest.mapNotNull { destId ->
            destList.find { it.id == destId }?.let {
                PreferredDestination(
                    id = it.id,
                    name = it.name,
                    icon = it.icon,
                    imageUrl = it.imageUrl
                )
            }
        }

        val expList = ExperienceViewModel().getAllExperiences()
        preferredExperiences = viewModel.prefExp.mapNotNull { expId ->
            expList.find { it.id == expId }?.let {
                Experience(
                    id = it.id,
                    name = it.name,
                    icon = it.icon
                )
            }
        }
    }




    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Text(
                "Edit Profile",
                fontSize = 36.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                ImagePicker(
                    name = viewModel.username,
                    imgUri = viewModel.img,
                    setPhoto = { uri ->
                        if (uri != null) {
                            scope.launch {
                                if (uri.startsWith("http")) {
                                    // È un URL remoto, non serve upload
                                    viewModel.updateImg(uri)
                                    viewModel.saveProfile()
                                    Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    val localUri = uri.toUri()
                                    val url = ImageRepository.uploadImage(context, localUri, ImageRepository.Bucket.PROFILE)
                                    if (url != null) {
                                        viewModel.updateImg(url)
                                        viewModel.saveProfile()
                                        Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    uploadImage = { context, uri -> viewModel.uploadImage(context, uri, ) }
                )
            }
        }

        item {
            SectionHeader("Personal Information")
        }
        item {
            EditInfoRow(
                fieldName = "Name",
                value = viewModel.name,
                error = viewModel.nameError,
                onEdit = viewModel::updateName,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        }
        item {
            EditInfoRow(
                fieldName = "Surname",
                value = viewModel.surname,
                error = viewModel.surnameError,
                onEdit = viewModel::updateSurname,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        }
        item {
            EditInfoRow(
                fieldName = "Username",
                value = viewModel.username,
                error = viewModel.usernameError,
                onEdit = viewModel::updateUsername,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        }

        item {
            SectionHeader("Contact Information")
        }
        item {
            EditInfoRow(
                fieldName = "Email",
                value = viewModel.email,
                error = viewModel.emailError,
                onEdit = viewModel::updateEmail,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        }
        item {
            EditInfoRow(
                fieldName = "Phone",
                value = viewModel.phone,
                error = viewModel.phoneError,
                onEdit = viewModel::updatePhone,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
        }

        // Section: Social
        item {
            SectionHeader("Social")
        }
        item {
            EditInfoRow(
                fieldName = "Instagram",
                value = viewModel.instagram,
                onEdit = viewModel::updateInstagram,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        }
        item {
            EditInfoRow(
                fieldName = "Facebook",
                value = viewModel.facebook,
                onEdit = viewModel::updateFacebook,
                onSave = ::handleSave,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            )
        }

        // Section: Preferences
        item {
            SectionHeader("Preferences")
        }
        item {
            EditInfoRow(
                fieldName = "Preferred Destinations",
                value = preferredDestinations.joinToString { it.name },
                onSave = ::handleSave,
                destinations = preferredDestinations,
                onDestinationsUpdate = viewModel::updatePrefDest,
                isComplexField = true,
                isPrefDest = true
            )
        }
        item {
            EditInfoRow(
                fieldName = "Preferred Experiences",
                value = preferredExperiences.joinToString { it.name },
                onSave = ::handleSave,
                experiences = preferredExperiences,
                onExperiencesUpdate = viewModel::updatePrefExp,
                isComplexField = true
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}


