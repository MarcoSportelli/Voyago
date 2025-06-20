package com.example.voyago.viewModels
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.ChatItem
import com.example.voyago.Message
import com.example.voyago.MessageStatus
import com.example.voyago.NotificationType
import com.example.voyago.profileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



// ViewModel per la chat
class ChatViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems: StateFlow<List<ChatItem>> = _chatItems.asStateFlow()

    private val _currentChat = MutableStateFlow<ChatItem?>(null)
    val currentChat: StateFlow<ChatItem?> = _currentChat.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var messagesListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null

    // Carica tutte le chat dell'utente
    fun loadUserChats() {
        val userId = profileViewModel.id

        _loading.value = true
        chatsListener = db.collection("chats")
            .whereArrayContains("participants", userId.toString())
            .addSnapshotListener { snapshot, error ->
                _loading.value = false

                if (error != null) {
                    Log.e("ChatViewModel", "Error loading chats", error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val participants = doc.get("participants") as? List<String> ?: return@mapNotNull null
                        val tripId = doc.getString("tripId") ?: ""
                        val tripTitle = doc.getString("tripTitle") ?: ""
                        val tripImage = doc.getString("tripImage")
                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0
                        val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                        val id = doc.id
                        val type = doc.getString("type") ?: "trip"
                        when (type) {
                            "organizer" -> ChatItem.OrganizerChat(
                                id = id,
                                tripId = tripId,
                                name = tripTitle,
                                image = tripImage,
                                lastMessage = lastMessage,
                                timestamp = timestamp,
                                unreadCount = unreadCount,
                                participants = participants,
                                type = type
                            )
                            "trip" -> ChatItem.TripChat(
                                id = id,
                                tripId = tripId,
                                name = tripTitle,
                                image = tripImage,
                                lastMessage = lastMessage,
                                timestamp = timestamp,
                                unreadCount = unreadCount,
                                participants = participants,
                                type = type
                            )
                            else -> null
                        }

                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing chat", e)
                        null
                    }
                } ?: emptyList()


                _chatItems.value = chats.sortedByDescending { it.timestamp }
            }
    }

    fun selectChat(chat: ChatItem?) {
        _currentChat.value = chat

        chat?.let {
            loadMessages(it.id)
        }
    }


    private fun loadMessages(chatId: String) {
        messagesListener?.remove()

        messagesListener = db.collection("chats/$chatId/messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error loading messages", error)
                    return@addSnapshotListener
                }

                val currentUserId = profileViewModel.id.toString()

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Message(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            isFromCurrentUser = doc.getString("senderId") == currentUserId,
                            timestamp = doc.getLong("timestamp") ?: 0,
                            status = MessageStatus.valueOf(doc.getString("status") ?: "SENT")
                        )
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing message", e)
                        null
                    }
                } ?: emptyList()

                _messages.value = messages

                val batch = db.batch()
                val undeliveredMessages = messages.filter { message ->
                    !message.isFromCurrentUser && message.status == MessageStatus.SENT
                }
                undeliveredMessages.forEach { message ->
                    val messageRef = db.collection("chats/$chatId/messages").document(message.id)
                    batch.update(messageRef, "status", MessageStatus.DELIVERED.name)
                }
                if (undeliveredMessages.isNotEmpty()) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("ChatViewModel", "Messaggi aggiornati a DELIVERED")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatViewModel", "Errore aggiornando messaggi a DELIVERED", e)
                        }
                }

            }
    }
    fun markMessagesAsRead() {
        val chat = _currentChat.value ?: return
        val userId = profileViewModel.id.toString()
        val messagesToRead = _messages.value.filter {
            it.senderId != userId && it.status != MessageStatus.READ
        }

        if (messagesToRead.isNotEmpty()) {
            val batch = db.batch()

            messagesToRead.forEach { message ->
                val messageRef = db.collection("chats/${chat.id}/messages").document(message.id)
                batch.update(messageRef, "status", MessageStatus.READ.name)
            }

            val chatRef = db.collection("chats").document(chat.id)
            batch.update(chatRef, "unreadCount", 0)

            batch.commit()
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Messaggi marcati come READ")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Errore aggiornando a READ", e)
                }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            try {
                val currentUserId = profileViewModel.id.toString()
                val currentUserName = auth.currentUser?.displayName ?: "Anonymous"
                val chatId = _currentChat.value?.id ?: return@launch

                if (text.isBlank()) return@launch

                val message = hashMapOf(
                    "text" to text,
                    "senderId" to currentUserId,
                    "senderName" to currentUserName,
                    "timestamp" to System.currentTimeMillis(),
                    "status" to MessageStatus.SENT.name
                )

                // Aggiungi il messaggio
                val messageRef = db.collection("chats/$chatId/messages").add(message).await()

                messageRef.update("status", MessageStatus.DELIVERED.name).await()

                // Aggiorna la chat
                val updates = hashMapOf<String, Any>(
                    "lastMessage" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("chats").document(chatId).update(updates).await()

                // Recupera i partecipanti della chat
                val chatSnapshot = db.collection("chats").document(chatId).get().await()
                val participants = chatSnapshot.get("participants") as? List<String> ?: emptyList()

                // Ottieni il tripId se presente e convertibile
                val travelId = chatSnapshot.getString("tripId")?.toIntOrNull() ?: return@launch

                // Invia la notifica a tutti gli utenti tranne il mittente
                participants
                    .filter { it != currentUserId }
                    .forEach { targetUserId ->
                        NotificationSender.sendNotification(
                            type = NotificationType.Message,
                            targetUserId = targetUserId,
                            title = "New message from $currentUserName",
                            body = text,
                            travelId = travelId
                        )
                    }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    fun removeUserFromTripChats(userId: String, tripId: String ) {
        Log.d("ChatViewModel", "Inizio rimozione utente $userId dalle chat per il viaggio $tripId")

        viewModelScope.launch {
            try {
                val snapshot = db.collection("chats")
                    .whereEqualTo("tripId", tripId)
                    .get()
                    .await()
                Log.d("ChatViewModel", "Trovate ${snapshot.size()} chat associate al viaggio $tripId")

                for (doc in snapshot.documents) {
                    val chatId = doc.id
                    val type = doc.getString("type") ?: continue
                    val participants = doc.get("participants") as? List<String> ?: continue
                    Log.d("ChatViewModel", "Analisi chat $chatId di tipo $type con partecipanti: $participants")

                    if (type == "organizer") {
                        // Elimina la chat organizer dove compare il partecipante
                        if (participants.contains(userId)) {
                            deleteChat(chatId)
                            Log.d("ChatViewModel", "Chat organizer $chatId eliminata per $userId")
                        }

                    } else if (type == "trip") {
                        // Rimuove l'utente dalla lista dei partecipanti
                        if (participants.contains(userId)) {
                            val updatedParticipants = participants.filterNot { it == userId }
                            db.collection("chats").document(chatId)
                                .update("participants", updatedParticipants)
                                .await()
                            Log.d("ChatViewModel", "Utente $userId rimosso da chat trip $chatId")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Errore nella rimozione utente da chat trip/organizer", e)
            }
        }
    }

    fun deleteChat(chatId: String) {
        Log.d("ChatViewModel", "Avvio eliminazione chat $chatId")

        viewModelScope.launch {
            try {
                val chatRef = db.collection("chats").document(chatId)

                // Elimina tutti i messaggi della chat
                val messagesSnapshot = db.collection("chats/$chatId/messages").get().await()
                val batch = db.batch()
                Log.d("ChatViewModel", "Trovati ${messagesSnapshot.size()} messaggi da eliminare per chat $chatId")

                for (doc in messagesSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Elimina il documento della chat
                batch.delete(chatRef)

                batch.commit().await()
                Log.d("ChatViewModel", "Chat $chatId eliminata con successo")

                // Rimuovi dal flusso attuale
                _chatItems.update { list -> list.filterNot { it.id == chatId } }

                if (_currentChat.value?.id == chatId) {
                    _currentChat.value = null
                    _messages.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Errore nella cancellazione della chat", e)
            }
        }
    }



    // Aggiorna i messaggi non letti
    private fun updateUnreadMessages(chatId: String, messages: List<Message>) {
        val userId = profileViewModel.id

        // Trova i messaggi non letti da altri utenti
        val unreadMessages = messages.filter {
            it.senderId != userId.toString() && it.status != MessageStatus.READ
        }

        if (unreadMessages.isNotEmpty()) {
            // Aggiorna lo stato dei messaggi
            val batch = db.batch()

            unreadMessages.forEach { message ->
                val messageRef = db.collection("chats/$chatId/messages").document(message.id)
                batch.update(messageRef, "status", MessageStatus.READ.name)
            }

            val chatRef = db.collection("chats").document(chatId)
            batch.update(chatRef, "unreadCount", 0)

            batch.commit()
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error updating message status", e)
                }
        }
    }

    fun createOrganizerChat(tripId: String, tripTitle: String, tripImage: String?, otherUserId: String) {
        val currentUserId = profileViewModel.id
        val chatData = hashMapOf(
            "tripId" to tripId,
            "tripTitle" to tripTitle,
            "tripImage" to tripImage,
            "lastMessage" to "",
            "timestamp" to System.currentTimeMillis(),
            "unreadCount" to 0,
            "participants" to listOf(currentUserId.toString(), otherUserId),
            "type" to "organizer"
        )

        db.collection("chats")
            .add(chatData)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Organizer chat created: ${it.id}")
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Error creating organizer chat", it)
            }
    }


    fun createTripChat(tripId: String, tripTitle: String, tripImage: String?, newParticipantId: String) {
        val currentUserId =  profileViewModel.id.toString()

        db.collection("chats")
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("type", "trip")
            .get()
            .addOnSuccessListener { snapshot ->
                val existingChat = snapshot.documents.firstOrNull()

                if (existingChat != null) {
                    val currentParticipants = existingChat.get("participants") as? List<String> ?: emptyList()

                    if (!currentParticipants.contains(newParticipantId)) {
                        val updatedParticipants = (currentParticipants + newParticipantId).distinct()

                        db.collection("chats").document(existingChat.id)
                            .update("participants", updatedParticipants)
                            .addOnSuccessListener {
                                Log.d("ChatViewModel", "Partecipante aggiunto alla chat esistente: ${existingChat.id}")
                            }
                            .addOnFailureListener {
                                Log.e("ChatViewModel", "Errore nell'aggiornare i partecipanti", it)
                            }
                    }

                } else {

                    val chatData = hashMapOf(
                        "tripId" to tripId,
                        "tripTitle" to tripTitle,
                        "tripImage" to tripImage,
                        "lastMessage" to "",
                        "timestamp" to System.currentTimeMillis(),
                        "unreadCount" to 0,
                        "participants" to listOf(currentUserId, newParticipantId),
                        "type" to "trip"
                    )

                    db.collection("chats")
                        .add(chatData)
                        .addOnSuccessListener {
                            Log.d("ChatViewModel", "Nuova chat di gruppo creata: ${it.id}")
                        }
                        .addOnFailureListener {
                            Log.e("ChatViewModel", "Errore nella creazione della chat di gruppo", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Errore nella ricerca della chat esistente", it)
            }
    }


    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        chatsListener?.remove()
    }
}