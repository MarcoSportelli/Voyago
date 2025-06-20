package com.example.voyago.viewModels

import android.util.Log
import com.example.voyago.Review
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import com.example.voyago.viewModels.ReviewRepository._reviews
import com.example.voyago.viewModels.ReviewRepository.reviewsRef
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.app
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.absoluteValue

object ReviewRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsRef = firestore.collection("reviews")
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    suspend fun fetchReviews() {
        try {
            val snapshot = reviewsRef.get().await()
            val reviewsList = snapshot.documents.mapNotNull { doc ->
                Review(
                    id = (doc.getLong("id") ?: return@mapNotNull null).toInt(),
                    travelId = (doc.getLong("travelId") ?: return@mapNotNull null).toInt(),
                    userId = (doc.getLong("userId") ?: return@mapNotNull null).toInt(),
                    reviewText = doc.getString("reviewText") ?: "",
                    date = doc.getString("date") ?: "",
                    rating = (doc.getLong("rating") ?: return@mapNotNull null).toInt(),
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            }
            _reviews.value = reviewsList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getReviewsByTripId(tripId: Int): List<Review> {
        return try {
            val snapshot = reviewsRef
                .whereEqualTo("travelId", tripId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Review(
                    id = (doc.getLong("id") ?: return@mapNotNull null).toInt(),
                    travelId = (doc.getLong("travelId") ?: return@mapNotNull null).toInt(),
                    userId = (doc.getLong("userId") ?: return@mapNotNull null).toInt(),
                    reviewText = doc.getString("reviewText") ?: "",
                    date = doc.getString("date") ?: "",
                    rating = (doc.getLong("rating") ?: return@mapNotNull null).toInt(),
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getReviewsByUserId(userId: Int): List<Review> {
        return try {
            val snapshot = reviewsRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Review(
                    id = (doc.getLong("id") ?: return@mapNotNull null).toInt(),
                    travelId = (doc.getLong("travelId") ?: return@mapNotNull null).toInt(),
                    userId = (doc.getLong("userId") ?: return@mapNotNull null).toInt(),
                    reviewText = doc.getString("reviewText") ?: "",
                    date = doc.getString("date") ?: "",
                    rating = (doc.getLong("rating") ?: return@mapNotNull null).toInt(),
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteReviewsByTravelId(tripId: Int) {
        try {
            // Prima elimina da Firestore
            val snapshot = reviewsRef
                .whereEqualTo("travelId", tripId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Poi aggiorna lo stato locale
            _reviews.update { list -> list.filterNot { it.travelId == tripId } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllReviewOfUserFromDB(tripIdList: List<Int>?): List<Review> {
        if (tripIdList.isNullOrEmpty()) return emptyList()
        return reviewsRef
            .whereIn("travelId", tripIdList)
            .get()
            .await()
            .documents.mapNotNull { doc ->
                Review(
                    id = (doc.getLong("id") ?: return@mapNotNull null).toInt(),
                    travelId = (doc.getLong("travelId") ?: return@mapNotNull null).toInt(),
                    userId = (doc.getLong("userId") ?: return@mapNotNull null).toInt(),
                    reviewText = doc.getString("reviewText") ?: "",
                    date = doc.getString("date") ?: "",
                    rating = (doc.getLong("rating") ?: return@mapNotNull null).toInt(),
                    images = doc.get("images") as? List<String> ?: emptyList()
                )
            }
    }


    suspend fun addReview(review: Review) {
        try {
            // Genera un nuovo ID
            val newId = UUID.randomUUID().hashCode().absoluteValue
            val newReview = review.copy(id = newId)

            // Aggiungi a Firestore
            reviewsRef.document().set(newReview).await()

            // Aggiorna lo stato locale
            _reviews.update { it + newReview }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


class ReviewViewModel() : ViewModel() {

    private fun sendReviewNotification(review: Review, targetUserId: String) {
        viewModelScope.launch {
            try {
                // 1. Prepara la notifica
                val notification =
                    Notification(
                    userId = targetUserId,
                    travelId = review.travelId,
                    title = "Nuova recensione",
                    body = "Hai ricevuto una nuova recensione sul tuo viaggio!",
                    type = NotificationType.ReviewReceived
                )

                // 2. Salva su Firestore
                NotificationRepository.addNotification(notification)

                // 3. Invia notifica FCM
                FirebaseMessaging.getInstance().send(
                    RemoteMessage.Builder("${Firebase.app.options.projectId}@gcm.googleapis.com")
                        .setMessageId(UUID.randomUUID().toString())
                        .addData("type", "ReviewReceived")
                        .addData("travel_id", review.travelId.toString())
                        .addData("target_user_id", targetUserId.toString())
                        .build()
                )

            } catch (e: Exception) {
                Log.e("ReviewVM", "Errore invio notifica", e)
            }
        }
    }

    // Esponiamo la lista di recensioni dalla repo come StateFlow
    val reviews: StateFlow<List<Review>> = ReviewRepository.reviews

    // Stato per una lista filtrata di recensioni per viaggio
    private val _reviewsByTripId = MutableStateFlow<List<Review>>(emptyList())
    val reviewsByTripId: StateFlow<List<Review>> = _reviewsByTripId.asStateFlow()

    // Stato per una lista filtrata di recensioni per utente
    private val _reviewsByUserId = MutableStateFlow<List<Review>>(emptyList())
    val reviewsByUserId: StateFlow<List<Review>> = _reviewsByUserId.asStateFlow()

    // Carica tutte le recensioni e aggiorna lo StateFlow
    suspend fun getReviewsByTripId(tripId: Int): List<Review> {
        return ReviewRepository.getReviewsByTripId(tripId)
    }

    suspend fun getReviewsByUserId(userId: Int): List<Review> {
        return ReviewRepository.getReviewsByUserId(userId)
    }

    suspend fun getAllReviewsOfUser(tripIdList: List<Int>?): List<Review> {
        return ReviewRepository.getAllReviewOfUserFromDB(tripIdList)
    }

    suspend fun getTripOwnerId(tripId: Int): String {
        return FirebaseFirestore.getInstance()
            .collection("trips")
            .document(tripId.toString())
            .get()
            .await()
            .getString("ownerId") ?: ""
    }

    // Aggiungi una recensione
    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                ReviewRepository.addReview(review)

                val travel = TravelRepository.getTravelById(review.travelId)
                if (travel == null) {
                    Log.e("VOYAGO_DEBUG", "❌ Trip not found: ID ${review.travelId}")
                    return@launch
                }

                val sender = ProfileRepository.getProfileByInternalId(review.userId)
                val senderName = sender?.name ?: "Someone"

                val notification = Notification(
                    userId = travel.userId.toString(),
                    travelId = review.travelId,
                    title = "New Trip Review",
                    body = "You have received a new review from $senderName",
                    read = false,
                    timestamp = Timestamp.now(),
                    type = NotificationType.ReviewReceived
                )

                NotificationRepository.addNotification(notification)

                NotificationSender.sendNotification(
                    type = NotificationType.ReviewReceived,
                    targetUserId = travel.userId.toString(),
                    title = "New Trip Review",
                    body = "You have received a new review from $senderName",
                    travelId = review.travelId,
                )

            } catch (e: Exception) {
                Log.e("VOYAGO_DEBUG", "Error adding review", e)
                e.printStackTrace()
            }
        }
    }

    fun deleteReviewsByTripId(tripId: Int) {
        viewModelScope.launch {
            ReviewRepository.deleteReviewsByTravelId(tripId)
        }
    }

}
