package com.example.voyago.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.voyago.R
import java.io.File
import kotlin.math.min
import androidx.core.net.toUri
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.LighGreen20
import kotlinx.coroutines.launch

/*
    When we take a picture with the camera, we first need to create a file to store the image.
    This function creates a file called "selected_image_.jpg" in the "images" folder
 */
class ComposeFileProvider : FileProvider(
    R.xml.filepaths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory,
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}

/*
    Show the image (or the placeholder, in case there is no image) and the button that opens
    a popup that lets the user select a new image, take a picture or delete the current image (if present)
 */
@Composable
fun ImagePicker(
    name: String,
    imgUri: String?,
    setPhoto: (String?) -> Unit,
    uploadImage: suspend (Context, Uri) -> String?
){
    var imageUri by remember { mutableStateOf(imgUri ?: "") }
    var showPopUp by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val hasImage = imageUri.isNotEmpty()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Sincronizza con l'imgUri dall'esterno
    LaunchedEffect(imgUri) {
        if (imgUri != null && imgUri != imageUri && !isUploading) {
            imageUri = imgUri
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                // Mostra immediatamente l'immagine selezionata
                imageUri = it.toString()
                isUploading = true

                scope.launch {
                    println("DENTRO SCOPE - GALLERY")
                    val url = uploadImage(context, it)
                    isUploading = false
                    url?.let { uploadedUrl ->
                        println("URL from gallery: $uploadedUrl")
                        imageUri = uploadedUrl
                        setPhoto(uploadedUrl)
                    }
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                // Mostra immediatamente la foto scattata
                imageUri = tempCameraUri.toString()
                isUploading = true

                scope.launch {
                    println("DENTRO SCOPE - CAMERA")
                    val url = uploadImage(context, tempCameraUri!!)
                    println("PISELLI DURI AaaaaaaaAAAAAAAAAAA" + url)
                    isUploading = false
                    url?.let { uploadedUrl ->
                        println("URL from camera: $uploadedUrl")
                        imageUri = uploadedUrl
                        setPhoto(uploadedUrl)
                    }
                    tempCameraUri = null
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        // Immagine profilo
        if (hasImage) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Indicatore di caricamento
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏳",
                            fontSize = 30.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(LighGreen20)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 89.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Pulsante Edit
        ElevatedButton(
            onClick = { showPopUp = true },
            colors = ButtonColors(
                containerColor = DarkGreen20,
                contentColor = Color.White,
                disabledContainerColor = Color.LightGray,
                disabledContentColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "edit profile image",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Edit",
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Popup menu
        if (showPopUp) {
            Dialog(onDismissRequest = { showPopUp = false }) {
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
                                showPopUp = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Choose from Gallery", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val uri = ComposeFileProvider.getImageUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                                showPopUp = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Take Photo", color = Color.White)
                        }

                        if (hasImage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    imageUri = ""
                                    setPhoto(null)
                                    isUploading = false
                                    showPopUp = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove Photo")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetImage(imageUri: Uri?, size: Int) {
    AsyncImage(
        model = imageUri,
        modifier = Modifier
            .border(1.dp, color = Color.LightGray, shape = CircleShape)
            .size(size.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .clip(CircleShape),
        contentDescription = "Image",
        contentScale = ContentScale.Fit
    )
}

@Composable
fun NoImage(name: String, size: Int) {
    if (name.isBlank()) {
        Text(
            text = "No image",

            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = (size/5.7).sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
    } else {
        if(name.contains(" ")){
            var displayName = ""
            val words = name.split(" ")
            for (i in 0 until min(words.size, 3)) {
                if (words[i].isNotEmpty()) {
                    displayName += words[i][0].uppercaseChar()
                }
            }
            Text(
                text = displayName,
                /*modifier = Modifier
                    .background(
                        lightBlue,
                        shape = RoundedCornerShape(size.dp)
                    )
                    .border(1.dp, color = Color.LightGray, shape = CircleShape)
                    .size(size.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),*/
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = (size / 3).sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }else {
            var displayName = ""
            for (i in 0 until min(name.length, 3)) {
                displayName += name[i].uppercaseChar()
            }
            Text(
                text = displayName,
                /*modifier = Modifier
                    .background(
                        lightBlue,
                        shape = RoundedCornerShape(size.dp)
                    )
                    .border(1.dp, color = Color.LightGray, shape = CircleShape)
                    .size(size.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),*/
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = (size / 3).sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }
    }
}