package com.example.voyago.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.ui.theme.DarkGreen20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(screen: String, finish: () -> Unit = {}, onClean: () -> Unit = {}) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Dialog di conferma per la cancellazione
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm deletion") },
            text = { Text("Are you sure you want to delete this draft?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClean()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    CenterAlignedTopAppBar(
        modifier = Modifier.background(Color.White),
        navigationIcon = {

            if (screen == "CreateNewTravelProposal") {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .border(2.dp, color = DarkGreen20, shape = RoundedCornerShape(50))
                ) {
                    Button(
                        onClick = { finish() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(50), // per avere bordi molto arrotondati
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Save and exit",
                            color = DarkGreen20
                        )
                    }
                }

            } else {
                IconButton(
                    onClick = { finish() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }

            }

        },
        actions = {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                if (screen == "CreateNewTravelProposal") {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp)
                            .border(2.dp, color = Color(0xFF800000), shape = RoundedCornerShape(50))
                    ) {
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(50), // per avere bordi molto arrotondati
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Delete",
                                color = Color(0xFF800000)
                            )
                        }
                    }
                }
            }

        },
        title = { /* Titolo vuoto per mantenere il centramento */ },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            actionIconContentColor = Color.Black,
            navigationIconContentColor = Color.Black
        )
    )
}