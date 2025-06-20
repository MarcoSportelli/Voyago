package com.example.voyago.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.GLOBAL_NOTIFICATION_USER_ID
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object NotificationRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME = "notifications"

    suspend fun getNotifications(userId: String): List<Notification> {
        return try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereIn("userId", listOf(userId, "9999"))  // <-- Magic here!
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null

                    // Estrazione campi con controllo
                    val userId = data["userId"] as? String ?: return@mapNotNull null
                    val travelId = (data["travelId"] as? Long)?.toInt() // Firestore salva i numeri come Long
                    val title = data["title"] as? String ?: return@mapNotNull null
                    val body = data["body"] as? String ?: return@mapNotNull null
                    val read = data["read"] as? Boolean ?: false
                    val timestamp = data["timestamp"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                    val typeStr = data["type"] as? String ?: return@mapNotNull null
                    val type = try {
                        NotificationType.valueOf(typeStr)
                    } catch (e: IllegalArgumentException) {
                        // Tipo non riconosciuto, salta la notifica
                        return@mapNotNull null
                    }
                    val imageUrl = data["imageUrl"] as? String

                    Notification(
                        id = doc.id,
                        userId = userId,
                        travelId = travelId,
                        title = title,
                        body = body,
                        read = read,
                        timestamp = timestamp,
                        type = type,
                        imageUrl = imageUrl
                    )
                } catch (e: Exception) {
                    Log.e("NotificationRepo", "Error parsing notification ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error fetching notifications", e)
            emptyList()
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        try {
            db.collection(COLLECTION_NAME)
                .document(notificationId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error deleting notification", e)
        }
    }

    suspend fun clearNotificationsForUser(userId: String) {
        try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            for (doc in snapshot.documents) {
                db.collection(COLLECTION_NAME)
                    .document(doc.id)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error clearing notifications for user $userId", e)
        }
    }


    suspend fun addNotification(notification: Notification) {
        try {
            db.collection(COLLECTION_NAME)
                .add(notification)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error adding notification", e)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            db.collection(COLLECTION_NAME)
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking notification as read", e)
        }
    }
}

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            val loadedNotifications = NotificationRepository.getNotifications(userId)
            _notifications.value = loadedNotifications
            Log.d("NotificationVM", "Loaded ${loadedNotifications.size} notifications for ${userId} ")
        }
    }

    fun markAsRead(notificationId: String, userId: String) {
        viewModelScope.launch {
            NotificationRepository.markAsRead(notificationId)
            loadNotifications(userId)  // Refresh after marking as read
        }
    }
    fun deleteNotification(notification: Notification, userId: String) {
        viewModelScope.launch {
            NotificationRepository.deleteNotification(notification.id)
            loadNotifications(userId)  // Aggiorna lista
        }
    }

    fun clearAllNotifications(userId: String) {
        viewModelScope.launch {
            NotificationRepository.clearNotificationsForUser(userId)
            loadNotifications(userId)  // Aggiorna lista
        }
    }


}
