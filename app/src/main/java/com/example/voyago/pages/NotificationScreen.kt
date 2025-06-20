package com.example.voyago.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.voyago.BottomNavItem
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import com.example.voyago.profileViewModel
import com.example.voyago.viewModels.NotificationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    userId: String
) {
    val viewModel: NotificationViewModel = viewModel()
    val notifications by viewModel.notifications.collectAsState()
    var deletingAll by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadNotifications(profileViewModel.id.toString())
    }

    LaunchedEffect(deletingAll) {
        if (deletingAll) {
            delay(500)
            viewModel.clearAllNotifications(profileViewModel.id.toString())
            deletingAll = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(vertical = 30.dp, horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Black
                )
            }
            IconButton(onClick = { deletingAll = true }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete all", tint = Color.DarkGray)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notifications, key = { it.id }) { notification ->
                NotificationItem(
                    notification = notification,
                    onDelete = {
                        viewModel.deleteNotification(notification, profileViewModel.id.toString())
                    },
                    deletingAll = deletingAll,
                    onNotificationClick = { clicked ->
                        when (clicked.type) {
                            NotificationType.LastMinute -> {
                                clicked.travelId?.let { travelId ->
                                    navController.navigate("trip_page/${travelId}/${false}")
                                }
                            }
                            NotificationType.Recommended -> {
                                clicked.travelId?.let { travelId ->
                                    navController.navigate("trip_page/${travelId}/${false}")
                                }
                            }
                            NotificationType.RequestReceived -> {
                                clicked.travelId?.let {
                                    navController.popBackStack()
                                    navController.navigate("mytrip/Organized") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            NotificationType.RequestSend -> {
                                navController.popBackStack()
                                navController.navigate("mytrip/Booked") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            NotificationType.ReviewReceived -> {
                                clicked.travelId?.let { travelId ->
                                    navController.popBackStack()
                                    navController.navigate("review_page/$travelId/false"){
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }

                                }
                            }
                            else ->{}

                        }
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onDelete: () -> Unit,
    deletingAll: Boolean = false,
    onNotificationClick: (Notification) -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    var triggerDeletion by remember { mutableStateOf(false) }

    // Gestione dell'animazione e della cancellazione
    LaunchedEffect(triggerDeletion) {
        if (triggerDeletion) {
            delay(300) // Aspetta che l'animazione completi
            onDelete()
            triggerDeletion = false
        }
    }

    val icon = when (notification.type) {
        NotificationType.LastMinute -> Icons.Filled.ShoppingCart
        NotificationType.Recommended -> Icons.Filled.Add
        NotificationType.RequestSend -> Icons.Filled.Build
        NotificationType.RequestReceived -> Icons.Filled.Email
        NotificationType.ReviewReceived -> Icons.Filled.Star
        else -> Icons.Default.Lock

    }

    val iconColor = when (notification.type) {
        NotificationType.LastMinute -> Color.Red
        NotificationType.Recommended -> Color(0xFF4CAF50)
        NotificationType.RequestSend -> Color(0xFF4CAF50)
        NotificationType.RequestReceived -> Color(0xFFFF5722)
        NotificationType.ReviewReceived -> Color(0xFFFFD700)
        else -> Color.White
    }

    AnimatedVisibility(
        visible = visible && !deletingAll,
        exit = slideOutHorizontally(
            animationSpec = tween(300),
            targetOffsetX = { -it }
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray.copy(alpha = 0.2f))
                .clickable { onNotificationClick(notification) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            IconButton(
                onClick = {
                    visible = false
                    triggerDeletion = true
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.DarkGray)
            }
        }
    }
}