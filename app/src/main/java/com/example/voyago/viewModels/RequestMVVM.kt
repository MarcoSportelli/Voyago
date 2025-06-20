package com.example.voyago.viewModels
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.example.voyago.DataProfile
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import kotlinx.coroutines.launch
import com.example.voyago.TripRequest
import com.example.voyago.RequestStatus
import com.example.voyago.Participants
import com.google.firebase.Timestamp
import java.util.UUID
import kotlin.math.absoluteValue


object TripRequestRepository {

    val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME = "requests"

    suspend fun getRequestsByTripId(tripId: Int): List<TripRequest>{
        return try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("tripId", tripId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                    val tripId = doc.getLong("tripId")?.toInt() ?: return@mapNotNull null
                    val senderId = doc.getLong("senderId")?.toInt() ?: return@mapNotNull null
                    val receiverId = doc.getLong("receiverId")?.toInt() ?: return@mapNotNull null
                    val adults = doc.getLong("adults")?.toInt() ?: 0
                    val children = doc.getLong("children")?.toInt() ?: 0
                    val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                    val status = RequestStatus.valueOf(statusStr)

                    TripRequest(
                        id = id,
                        tripId = tripId,
                        senderId = senderId,
                        receiverId = receiverId,
                        adults = adults,
                        children = children,
                        status = status
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRequestsBySenderUserId(userId: Int): List<TripRequest> {
        return try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("senderId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                    val tripId = doc.getLong("tripId")?.toInt() ?: return@mapNotNull null
                    val senderId = doc.getLong("senderId")?.toInt() ?: return@mapNotNull null
                    val receiverId = doc.getLong("receiverId")?.toInt() ?: return@mapNotNull null
                    val adults = doc.getLong("adults")?.toInt() ?: 0
                    val children = doc.getLong("children")?.toInt() ?: 0
                    val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                    val status = RequestStatus.valueOf(statusStr)

                    TripRequest(
                        id = id,
                        tripId = tripId,
                        senderId = senderId,
                        receiverId = receiverId,
                        adults = adults,
                        children = children,
                        status = status
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getRequestById(requestId: Int): TripRequest? {
        return try {
            val doc = db.collection(COLLECTION_NAME)
                .document(requestId.toString())
                .get()
                .await()

            if (doc.exists()) {
                val id = doc.getLong("id")?.toInt() ?: return null
                val tripId = doc.getLong("tripId")?.toInt() ?: return null
                val senderId = doc.getLong("senderId")?.toInt() ?: return null
                val receiverId = doc.getLong("receiverId")?.toInt() ?: return null
                val adults = doc.getLong("adults")?.toInt() ?: 0
                val children = doc.getLong("children")?.toInt() ?: 0
                val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                val status = RequestStatus.valueOf(statusStr)

                TripRequest(
                    id = id,
                    tripId = tripId,
                    senderId = senderId,
                    receiverId = receiverId,
                    adults = adults,
                    children = children,
                    status = status
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getRequestsByReceiverUserId(userId: Int): List<TripRequest> {
        return try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("receiverId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                    val tripId = doc.getLong("tripId")?.toInt() ?: return@mapNotNull null
                    val senderId = doc.getLong("senderId")?.toInt() ?: return@mapNotNull null
                    val receiverId = doc.getLong("receiverId")?.toInt() ?: return@mapNotNull null
                    val adults = doc.getLong("adults")?.toInt() ?: 0
                    val children = doc.getLong("children")?.toInt() ?: 0
                    val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                    val status = RequestStatus.valueOf(statusStr)

                    TripRequest(
                        id = id,
                        tripId = tripId,
                        senderId = senderId,
                        receiverId = receiverId,
                        adults = adults,
                        children = children,
                        status = status
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addRequest(request: TripRequest) {
        try {
            val requestMap = hashMapOf(
                "id" to request.id,
                "tripId" to request.tripId,
                "senderId" to request.senderId,
                "receiverId" to request.receiverId,
                "adults" to request.adults,
                "children" to request.children,
                "status" to request.status.name
            )
            db.collection(COLLECTION_NAME)
                .document(request.id.toString())
                .set(requestMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun generateIdByScanningRequests(): Int {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("requests")
                .get()
                .await()

            val maxId = snapshot.documents.mapNotNull {
                it.getLong("id")?.toInt()
            }.maxOrNull() ?: 0

            maxId + 1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
    suspend fun deleteRequestByTripId(userId:Int, tripId: Int) {
        Log.d("Request", "userId: $userId")
        Log.d("Request", "tripId: $tripId")

        try {
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("senderId", userId)
                .whereEqualTo("tripId", tripId)
                .get()
                .await()

            Log.d("Request", "snapshot: $snapshot")

            for (doc in snapshot.documents) {
                db.collection(COLLECTION_NAME).document(doc.id).delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun updateRequestStatus(requestId: Int, newStatus: RequestStatus) {
        try {
            db.collection(COLLECTION_NAME)
                .document(requestId.toString())
                .update("status", newStatus.name)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class RequestsViewModel : ViewModel() {

    private val _requests = MutableStateFlow<List<TripRequest>>(emptyList())
    val requests: StateFlow<List<TripRequest>> = _requests.asStateFlow()



    suspend fun getRequestsSender(userId: Int): List<TripRequest> {
        return TripRequestRepository.getRequestsBySenderUserId(userId)
    }
    suspend fun getRequestsReceiver(userId: Int): List<TripRequest> {
        return TripRequestRepository.getRequestsByReceiverUserId(userId)
    }





    fun deleteRequest(userId: Int, tripId: Int) {
        viewModelScope.launch {
            TripRequestRepository.deleteRequestByTripId(userId, tripId)
            _requests.update { current -> current.filterNot { it.tripId == tripId && it.senderId == userId} }
        }
    }

    fun hasSentRequest(senderId: Int, tripId: Int): Boolean {
        return _requests.value.any { it.senderId == senderId && it.tripId == tripId }
    }
    fun addNewRequest(tripId: Int, sender: DataProfile, receiver: DataProfile, participants: Participants) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().hashCode().absoluteValue
            if (newId != -1) {
                // 1. Recupera i dettagli del viaggio
                val tripName = try {
                    TravelRepository.getTravelById(tripId)?.title ?: ""
                } catch (e: Exception) {
                    Log.e("addNewRequest", "Error getting trip details", e)
                    ""
                }

                val tripInfo = if (tripName.isNotEmpty()) " for your trip '$tripName'" else ""

                // 2. Crea la nuova richiesta
                val newRequest = TripRequest(
                    id = newId,
                    tripId = tripId,
                    senderId = sender.id,
                    receiverId = receiver.id,
                    adults = participants.adults,
                    children = participants.children
                )

                // 3. Salva la richiesta
                TripRequestRepository.addRequest(newRequest)

                _requests.update { it + newRequest }

                // 4. Prepara i messaggi di notifica
                val notificationTitle = "New Travel Request"
                val notificationBody = "You have a new travel request$tripInfo"
                val pushTitle = "✈️ New Request"
                val pushBody = "You received a new travel request$tripInfo"

                // 5. Crea e salva la notifica
                val notification = Notification(
                    userId = newRequest.receiverId.toString(),
                    travelId = newRequest.tripId,
                    title = notificationTitle,
                    body = notificationBody,
                    type = NotificationType.RequestReceived,
                    timestamp = Timestamp.now()
                )
                NotificationRepository.addNotification(notification)

                // 6. Invia la notifica push
                NotificationSender.sendNotification(
                    type = NotificationType.RequestReceived,
                    targetUserId = receiver.id.toString(),
                    title = pushTitle,
                    body = pushBody,
                    travelId = tripId
                )
            }
        }
    }


    fun updateRequestStatus(requestId: Int, newStatus: RequestStatus) {
        viewModelScope.launch {
            try {
                // Aggiorna lo stato prima
                TripRequestRepository.updateRequestStatus(requestId, newStatus)

                // Recupera la richiesta
                val request = TripRequestRepository.getRequestById(requestId)
                if (request == null) {
                    Log.e("VOYAGO_DEBUG", "❌ Request not found: ID $requestId")
                    return@launch
                }

                // Recupera il profilo del mittente
                val senderProfile = ProfileRepository.getProfileByInternalId(request.senderId)
                if (senderProfile == null) {
                    Log.e("VOYAGO_DEBUG", "❌ Sender profile not found: ID ${request.senderId}")
                    return@launch
                }

                // Recupera il viaggio
                val trip = TravelRepository.getTravelById(request.tripId)
                if (trip == null) {
                    Log.e("VOYAGO_DEBUG", "❌ Trip not found: ID ${request.tripId}")
                    return@launch
                }

                // Crea la notifica
                val statusText = newStatus.name.lowercase()
                val capitalizedStatus = statusText.replaceFirstChar { it.uppercase() }

                val notification = Notification(
                    userId = request.senderId.toString(),
                    travelId = request.tripId,
                    title = "Request $capitalizedStatus",
                    body = "Your request for trip ${trip.title} was $statusText",
                    read = false,
                    timestamp = Timestamp.now(),
                    type = NotificationType.RequestSend // Oppure crea un nuovo tipo: RequestStatusUpdated
                )
                NotificationRepository.addNotification(notification)

                NotificationSender.sendNotification(
                    type = NotificationType.RequestSend,
                    targetUserId = request.senderId.toString(),
                    title = "Request $capitalizedStatus",
                    body = "Your request for trip ${trip.title} was $statusText",
                    travelId = request.tripId
                )

                if (newStatus == RequestStatus.ACCEPTED) {
                    ChatViewModel().createOrganizerChat(
                        tripId = trip.id.toString(),
                        tripTitle = trip.title,
                        tripImage = trip.images[0],
                        otherUserId = senderProfile.id.toString()
                    )

                    ChatViewModel().createTripChat(
                        tripId = trip.id.toString(),
                        tripTitle = trip.title,
                        tripImage = trip.images.getOrNull(0),
                        newParticipantId = senderProfile.id.toString()
                    )

                }


            } catch (e: Exception) {
                Log.e("VOYAGO_DEBUG", "❌ Error while updating request status or creating notification", e)
            }
        }
    }

}
