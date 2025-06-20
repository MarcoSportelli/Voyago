package com.example.voyago.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.voyago.ChatItem
import com.example.voyago.DataProfile
import com.example.voyago.Message
import com.example.voyago.MessageStatus
import com.example.voyago.profileViewModel
import com.example.voyago.viewModels.ChatViewModel
import com.example.voyago.viewModels.ProfileRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.LighGreen20
import com.example.voyago.utils.ProfileImage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun ChatScreen(
    onChange: () -> Unit
) {
    val currentUser = profileViewModel.profile.collectAsState()
    val viewModel: ChatViewModel = viewModel()
    val chatItems by viewModel.chatItems.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    LaunchedEffect(Unit) {
        viewModel.loadUserChats()
        println(chatItems)
    }
    LaunchedEffect(viewModel.currentChat) {
        delay(200)
        viewModel.markMessagesAsRead()
    }

    if (currentChat != null) {
        when (currentChat) {
            is ChatItem.OrganizerChat -> OrganizerChatDetailScreen(
                chat = currentChat as ChatItem.OrganizerChat,
                currentUser = currentUser.value!!,
                messages = messages,
                onBack = {
                    viewModel.selectChat(null)
                    onChange()
                },
                onSendMessage = { text -> viewModel.sendMessage(text) }
            )
            is ChatItem.TripChat -> TripChatDetailScreen(
                chat = currentChat as ChatItem.TripChat,
                currentUser = currentUser.value!!,
                messages = messages,
                onBack = {
                    viewModel.selectChat(null)
                    onChange()
                },
                onSendMessage = { text -> viewModel.sendMessage(text) }
            )

            null -> {}
        }
    } else {
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                Text(
                    "Chat",
                    fontSize = 36.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterChips(selectedFilter) { selectedFilter = it }

                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val filteredChats = when (selectedFilter) {
                        "Trips" -> chatItems.filterIsInstance<ChatItem.TripChat>()
                        "Organizer" -> chatItems.filterIsInstance<ChatItem.OrganizerChat>()
                        else -> chatItems
                    }

                    if (filteredChats.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No chats available",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn {
                            items(filteredChats) { item ->
                                ChatListItem(
                                    item = item,
                                    onClick = {
                                        viewModel.selectChat(item)
                                        onChange()
                                    }
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}


fun formatMessageTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(now, messageTime) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
        isYesterday(now, messageTime) -> {
            "Ieri"
        }
        else -> {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean =
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

private fun isYesterday(now: Calendar, other: Calendar): Boolean {
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DATE, -1)
    return isSameDay(yesterday, other)
}

@Composable
fun FilterChips(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Trips", "Organizer")

    Row {
        filters.forEach { filter ->
            val selectedStyle = if (filter == selected) {
                ButtonDefaults.buttonColors(containerColor = DarkGreen20, contentColor = Color.White)
            } else {
                ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            }
            val selectedStyleText = if (filter == selected) Color.White else Color.Black

            Button(
                onClick = { onSelected(filter) },
                colors = selectedStyle,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 8.dp).defaultMinSize(minHeight = 1.dp),
            ) {
                Text(filter, color = selectedStyleText)
            }
        }
    }
}
@Composable
fun CircleAvatarPlaceholder() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerChatDetailScreen(
    chat: ChatItem.OrganizerChat,
    currentUser: DataProfile,
    messages: List<Message>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var newMessage by remember { mutableStateOf("") }
    val otherParticipantId = chat.participants.firstOrNull { it != currentUser.id.toString() }

    var otherUser by remember { mutableStateOf<DataProfile?>(null) }
    var isLoadingOtherUser by remember { mutableStateOf(true) }
    var userOnline by remember { mutableStateOf(false) }
    var lastChanged by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(otherParticipantId) {
        isLoadingOtherUser = true
        otherUser = otherParticipantId?.let { ProfileRepository.getProfileByInternalId(it.toInt()) }
        isLoadingOtherUser = false

        if (otherParticipantId != null) {
            val query = Firebase.firestore.collection("profiles")
                .whereEqualTo("id", otherParticipantId.toInt())
                .limit(1)

            query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("StatusCheck", "Error while fetching status: ${error.message}")
                    userOnline = false
                    lastChanged = null
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    Log.d("StatusCheck", "Profile document found: ${doc.id}")

                    val status = doc.get("status") as? Map<*, *>
                    Log.d("StatusCheck", "Raw status data: $status")

                    val state = status?.get("state") as? String
                    val lastChangedTimestamp = status?.get("lastChanged") as? Timestamp

                    userOnline = (state == "online")
                    lastChanged = lastChangedTimestamp?.toDate()?.time

                    Log.d("StatusCheck", "User online: $userOnline")
                    Log.d("StatusCheck", "Last changed: ${lastChangedTimestamp?.toDate()}")
                } else {
                    Log.w("StatusCheck", "No profile document found for ID: $otherParticipantId")
                    userOnline = false
                    lastChanged = null
                }
            }
        }
    }


    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


    Column(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        }) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min)
            ,
            colors = TopAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.Black,
                navigationIconContentColor = Color.Black,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.Black
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    if (isLoadingOtherUser) {
                        CircleAvatarPlaceholder()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading...", fontWeight = FontWeight.Bold)
                    } else {
                        if (!otherUser?.img.isNullOrEmpty()) {
                            AsyncImage(
                                model = otherUser?.img,
                                contentDescription = "User Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LighGreen20),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = otherUser?.username?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = otherUser?.name ?: "Unknown User",
                                fontWeight = FontWeight.Bold,
                            )
                            if (userOnline) {
                                Text(
                                    text = "Online",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Green,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                val formattedLastSeen = lastChanged?.let {
                                    SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(it))
                                } ?: "Last seen unknown"

                                Text(
                                    text = "Last seen: $formattedLastSeen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },

        )


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message = message, isCurrentUser = message.isFromCurrentUser, isTripChat = false)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Divider(modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 24.dp), // da 16.dp → 24.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                placeholder = { },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp) // aumenta l'altezza minima
                    .padding(end = 8.dp), // spazio tra il campo e il pulsante "Send"
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF0F0F0),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp), // bordi più morbidi
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                singleLine = false,
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        onSendMessage(newMessage)
                        newMessage = ""
                    }
                },
                enabled = newMessage.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripChatDetailScreen(
    chat: ChatItem.TripChat,
    currentUser: DataProfile,
    messages: List<Message>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var newMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        }) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min) // Usa l'altezza intrinseca minima
            ,
            colors = TopAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.Black,
                navigationIconContentColor = Color.Black,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.Black
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = chat.image,
                        contentDescription = "Trip Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp)) // Riduci lo spazio se necessario
                    Column(
                        modifier = Modifier.weight(1f) // Prendi tutto lo spazio rimanente
                    ) {
                        Text(
                            chat.name,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${chat.participants.size} participants",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message = message, isCurrentUser = message.isFromCurrentUser, isTripChat = true)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Divider(modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 24.dp), // da 16.dp → 24.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                placeholder = {  },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp) // aumenta l'altezza minima
                    .padding(end = 8.dp), // spazio tra il campo e il pulsante "Send"
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF0F0F0),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp), // bordi più morbidi
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                singleLine = false,
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        onSendMessage(newMessage)
                        newMessage = ""
                    }
                },
                enabled = newMessage.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatListItem(item: ChatItem, onClick: () -> Unit) {
    val currentUserId = profileViewModel.profile.collectAsState().value?.id?.toString()

    var otherUserProfile by remember { mutableStateOf<DataProfile?>(null) }

    LaunchedEffect(item, currentUserId) {
        if (item is ChatItem.OrganizerChat && currentUserId != null) {
            val otherParticipantId = item.participants.firstOrNull { it != currentUserId }
            if (otherParticipantId != null) {
                otherUserProfile = ProfileRepository.getProfileByInternalId(otherParticipantId.toInt())
            }
        }
    }

    val displayName = when {
        item is ChatItem.OrganizerChat -> otherUserProfile?.name ?: "Loading..."
        else -> item.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            item is ChatItem.OrganizerChat && otherUserProfile != null -> {
                ProfileImage(
                    profile = otherUserProfile!!,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
            item is ChatItem.TripChat && !item.image.isNullOrEmpty() -> {
                AsyncImage(
                    model = item.image,
                    contentDescription = "Trip Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                val circleColor = Color(0xFFD1C4E9)
                val initials = displayName.take(2).uppercase()

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(circleColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.lastMessage,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatMessageTimestamp(item.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (item.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

}

@Composable
fun ChatBubble(message: Message, isCurrentUser: Boolean, isTripChat: Boolean = false) {
    val bubbleColor = if (isCurrentUser) Color(0xFF616161) else Color(0xFFF5F5F5)
    val textColor = if (isCurrentUser) Color.White else Color.Black
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    var senderProfile by remember { mutableStateOf<DataProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(message.senderId) {
        if (!isCurrentUser && isTripChat) {
            isLoading = true
            senderProfile = ProfileRepository.getProfileByInternalId(message.senderId.toInt())
            isLoading = false
        }
    }

    if (!isCurrentUser && isTripChat && isLoading) {
        Spacer(modifier = Modifier.height(8.dp))
        return
    }

    fun getInitial(name: String?): String {
        return name?.takeIf { it.isNotBlank() }?.trim()?.firstOrNull()?.uppercase() ?: "?"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        if (!isCurrentUser && isTripChat && senderProfile != null) {
            Text(
                text = "${senderProfile!!.name} ${senderProfile!!.surname}",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 44.dp, bottom = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isCurrentUser && isTripChat) {
                if (!senderProfile?.img.isNullOrBlank()) {
                    AsyncImage(
                        model = senderProfile!!.img,
                        contentDescription = "Sender Image",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LighGreen20),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitial(senderProfile?.username),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = alignment,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .wrapContentWidth()
                        .widthIn(max = 280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = textColor,
                            fontSize = 14.sp,
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        text = formatMessageTimestamp(message.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.READ -> {
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Message read",
                                        tint = Color.Blue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Message read",
                                        tint = Color.Blue,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(start = 1.dp)
                                    )
                                }
                            }
                            MessageStatus.DELIVERED -> {
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Message delivered",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Message delivered",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(start = 1.dp)
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Message sent",
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}