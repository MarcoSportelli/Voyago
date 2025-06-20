package com.example.voyago.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import com.example.voyago.viewModels.RequestsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.voyago.DataProfile
import com.example.voyago.PastTravel
import com.example.voyago.RequestStatus
import com.example.voyago.Travel
import com.example.voyago.TripRequest
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.viewModels.BookedRepository
import com.example.voyago.viewModels.BookedViewModel
import com.example.voyago.viewModels.PastTravelViewModel
import com.example.voyago.viewModels.ProfileRepository
import com.example.voyago.viewModels.ProfileViewModel
import com.example.voyago.viewModels.TravelViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RequestCard(
    request: TripRequest,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    val travelVM = TravelViewModels()
    val bookedTravel = BookedViewModel()
    val requestsVM = RequestsViewModel()
    val pastTravelVM = PastTravelViewModel()

    var trip by remember { mutableStateOf<Travel?>(null) }
    var sender by remember { mutableStateOf<DataProfile?>(null) }

    var localStatus by remember { mutableStateOf(request.status) }
    var isVisible by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(request.tripId, request.senderId) {
        trip = travelVM.getTravelById(request.tripId)
        sender = ProfileRepository.getProfileByInternalId(request.senderId)
    }

    if (showAcceptDialog) {
        ConfirmationDialog(
            title = "Confirm Acceptance",
            message = "Are you sure you want to accept this request?",
            onConfirm = {
                localStatus = RequestStatus.ACCEPTED
                showAcceptDialog = false
                bookedTravel.addBooked(request)
                requestsVM.updateRequestStatus(request.id, RequestStatus.ACCEPTED)
                pastTravelVM.addPastTravel(
                    request.senderId,
                    request.tripId
                )

            },
            onDismiss = { showAcceptDialog = false }
        )
    }

    if (showRejectDialog) {
        ConfirmationDialog(
            title = "Confirm Rejection",
            message = "Are you sure you want to reject this request?",
            onConfirm = {
                localStatus = RequestStatus.REJECTED
                showRejectDialog = false
                requestsVM.updateRequestStatus(request.id, RequestStatus.REJECTED)
            },
            onDismiss = { showRejectDialog = false }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                if (sender != null) {
                                    append("${sender!!.name} ${sender!!.surname}")
                                }
                            }
                            append(" asks to book:")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable(onClick = onClick)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (trip != null) {
                        Text(
                            text = trip!!.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Adults: ${request.adults}, Children: ${request.children}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(status = localStatus)

                    Spacer(modifier = Modifier.height(8.dp))

                    if (localStatus == RequestStatus.PENDING) {
                        Row {
                            IconButton(onClick = { showRejectDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Reject",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showAcceptDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accept",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StatusBadge(status: RequestStatus) {
    val (text, color) = when (status) {
        RequestStatus.PENDING -> "Pending" to Color.Gray
        RequestStatus.ACCEPTED -> "Accepted" to Color(0xFF4CAF50)
        RequestStatus.REJECTED -> "Rejected" to Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}



@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}