package com.example.voyago.utils

import android.util.Log
import com.example.voyago.BookedTravel
import com.example.voyago.PastTravel
import com.example.voyago.R
import com.example.voyago.RequestStatus
import com.example.voyago.Review
import com.example.voyago.TripRequest
import com.example.voyago.components.ReviewItem
import com.example.voyago.viewModels.BookedRepository
import com.example.voyago.viewModels.PastTravelRepository
import com.example.voyago.viewModels.ReviewRepository
import com.example.voyago.viewModels.TripRequestRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// lore -> userId 1619771197 possiede travel -> 1 2
// gianlu -> 1273795543 travel -> 5
// marti -> 1050097156
// fra -> 1626721575 travel -> 8
// marc -> 1243173879 bs9rgiynSBRFlFzZNMARHf5UIIt1
// sarozza -> 1145110087 -> 10 11 12
// marco -> 1629230648 travel -> 3 4 xTlfObRnUjOk5bMLwE8YYMQw07l2
// matte -> 1824944549 travel -> 6 7 9
object FirestoreSeeder {

    private val db = FirebaseFirestore.getInstance()

    suspend fun seedReviews(reviews: List<Review>) {
        reviews.forEach { review ->
            val reviewMap = hashMapOf(
                "id" to review.id,
                "travelId" to review.travelId,
                "userId" to review.userId,
                "reviewText" to review.reviewText,
                "date" to review.date,
                "rating" to review.rating,
                "images" to review.images
            )

            db.collection("reviews")
                .document(review.id.toString())
                .set(reviewMap)
                .await()
        }
    }
}

fun populate() {
    val tripRequests = listOf(
        TripRequest(id = 1, tripId = 1202527142, senderId = 1243173879, receiverId = 0, adults = 2, children = 0, status = RequestStatus.ACCEPTED),
        TripRequest(id = 2, tripId = 1202527142, senderId = 1619771197, receiverId = 0, adults = 1, children = 0, status = RequestStatus.ACCEPTED),
        TripRequest(id = 3, tripId = 1202527142, senderId = 183865984, receiverId = 0, adults = 1, children = 1, status = RequestStatus.ACCEPTED),
        TripRequest(id = 4, tripId = 1202527142, senderId = 1050097156, receiverId = 0, adults = 1, children = 1, status = RequestStatus.ACCEPTED),
        TripRequest(id = 5, tripId = 1202527142, senderId = 1626721575, receiverId = 0, adults = 1, children = 1, status = RequestStatus.ACCEPTED),
        TripRequest(id = 6, tripId = 1830228557, senderId = 1050097156, receiverId = 0, adults = 2, children = 0, status = RequestStatus.ACCEPTED)
    )


    val pastTravels = listOf(
        PastTravel(id = 1, userId = 1243173879, travelId = 1202527142),
        PastTravel(id = 2, userId = 1619771197, travelId = 1202527142),
        PastTravel(id = 3, userId = 183865984, travelId = 1202527142),
        PastTravel(id = 4, userId = 1050097156, travelId = 1202527142),
        PastTravel(id = 5, userId = 1626721575, travelId = 1202527142),
        PastTravel(id = 6, userId = 1050097156, travelId = 1830228557)
    )

    tripRequests.forEach { review ->
        CoroutineScope(Dispatchers.IO).launch {
            TripRequestRepository.addRequest(review)
            BookedRepository.addBooked(review) // Or just `addReview(review)` if duplicates are not a concern
        }
    }
    pastTravels.forEach { review ->
        CoroutineScope(Dispatchers.IO).launch {
            PastTravelRepository.addPastTravel(userId = review.userId, tripId = review.travelId)
        }
    }

}





suspend fun initializeChatsOnce() {
    val db = FirebaseFirestore.getInstance()

    // Dati Tanzania
    val tripId1 = "1830228557"
    val tripTitle1 = "Romantic Escape: CinqueTerre & Portofino"
    val organizerId1 = "1619771197"
    val participantId1 = "1050097156"

    // Dati Romantic Escape
    val tripId2 = "1202527142"
    val tripTitle2 = "Tanzania Wildlife & Zanzibar Retreat"
    val organizerId2 = "1824944549"
    val participantIds2 = listOf("1243173879", "1619771197", "183865984", "1050097156", "1626721575")

    suspend fun chatExists(tripId: String, type: String, participants: List<String>): Boolean {
        val snapshot = db.collection("chats")
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("type", type)
            .get()
            .await()
        return snapshot.documents.any { doc ->
            val docParticipants = doc.get("participants") as? List<String> ?: emptyList()
            docParticipants.toSet() == participants.toSet()
        }
    }

    suspend fun createOrganizerChat(tripId: String, tripTitle: String, organizerId: String, otherUserId: String) {
        val participants = listOf(organizerId, otherUserId)
        if (!chatExists(tripId, "organizer", participants)) {
            val chatData = hashMapOf(
                "tripId" to tripId,
                "tripTitle" to tripTitle,
                "tripImage" to null,
                "lastMessage" to "",
                "timestamp" to System.currentTimeMillis(),
                "unreadCount" to 0,
                "participants" to participants,
                "type" to "organizer"
            )
            db.collection("chats").add(chatData).await()
            Log.d("ChatInit", "Organizer chat created for trip $tripId with $otherUserId")
        } else {
            Log.d("ChatInit", "Organizer chat already exists for trip $tripId with $otherUserId")
        }
    }

    suspend fun createTripChat(tripId: String, tripTitle: String, organizerId: String, participantId: String) {
        val snapshot = db.collection("chats")
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("type", "trip")
            .get()
            .await()

        val existingChat = snapshot.documents.firstOrNull()
        if (existingChat != null) {
            val currentParticipants = existingChat.get("participants") as? List<String> ?: emptyList()
            if (!currentParticipants.contains(participantId)) {
                val updatedParticipants = (currentParticipants + participantId).distinct()
                db.collection("chats").document(existingChat.id)
                    .update("participants", updatedParticipants)
                    .await()
                Log.d("ChatInit", "Added $participantId to existing trip chat $tripId")
            } else {
                Log.d("ChatInit", "Participant $participantId already in trip chat $tripId")
            }
        } else {
            val chatData = hashMapOf(
                "tripId" to tripId,
                "tripTitle" to tripTitle,
                "tripImage" to null,
                "lastMessage" to "",
                "timestamp" to System.currentTimeMillis(),
                "unreadCount" to 0,
                "participants" to listOf(organizerId, participantId),
                "type" to "trip"
            )
            db.collection("chats").add(chatData).await()
            Log.d("ChatInit", "New trip chat created for $tripId with participant $participantId")
        }
    }

    try {
        // Tanzania
        createOrganizerChat(tripId1, tripTitle1, organizerId1, participantId1)
        createTripChat(tripId1, tripTitle1, organizerId1, participantId1)

        // Romantic Escape
        participantIds2.forEach { participantId ->
            createOrganizerChat(tripId2, tripTitle2, organizerId2, participantId)
        }
        participantIds2.forEach { participantId ->
            createTripChat(tripId2, tripTitle2, organizerId2, participantId)
        }
    } catch (e: Exception) {
        Log.e("ChatInit", "Error initializing chats", e)
    }
}



fun createChatWithInitialMessages() {
    val db = FirebaseFirestore.getInstance()
    val organizerId = "1629230648"
    val participantId = "1243173879"


    val chatData = hashMapOf(
        "tripId" to 3,
        "tripTitle" to "Ibiza Nightlife",
        "tripImage" to "android.resource://com.example.voyago/2131231000",
        "lastMessage" to "",
        "timestamp" to System.currentTimeMillis(),
        "unreadCount" to 0,
        "participants" to listOf(organizerId, participantId, "1626721575")
    )

    // Creazione della chat
    db.collection("chats")
        .add(chatData)
        .addOnSuccessListener {
            Log.d("ChatCreation", "Chat e messaggi creati con successo.")
        }
        .addOnFailureListener { e ->
            Log.e("ChatCreation", "Errore durante la creazione della chat", e)
        }
}

fun populateReviews() {
    val initialReviews = listOf(
        // lore -> 1619771197 possiede travel -> 1,2 quindi NON può commentare su 1 e 2
        // Cambio userId per i travelId 1 e 2 con utenti diversi (esempio gianlu, marti, marc)
        Review(1, 1, 1273795543, "Amazing trip in the Alps! The mountains were breathtaking and the air was so fresh. I'd definitely go again!", "2024-04-01", 4, images = listOf("android.resource://com.example.voyago/" + R.drawable.gastronomy)),
        Review(2, 1, 1050097156, "Stunning views and great hiking. The guide was very knowledgeable and helped us discover hidden trails.", "2024-04-03", 4, images = listOf("android.resource://com.example.voyago/" + R.drawable.trail)),
        Review(3, 2, 1243173879, "Delicious food and wine in Tuscany. Every meal felt like a fine-dining experience, and the vineyards were beautiful.", "2024-03-28", 4),
        Review(4, 2, 1273795543, "Loved the cooking class! I learned how to make authentic pasta from scratch. It was fun and educational.", "2024-03-29", 5, images = listOf("android.resource://com.example.voyago/" + R.drawable.bg_travel2)),

        // gianlu -> 1273795543 possiede travel -> 5, quindi NON può commentare sul travel 5
        Review(9, 5, 1619771197, "Japan is amazing! The culture, technology, and traditions blend so uniquely. I loved every minute.", "2024-03-20", 4, images = listOf("android.resource://com.example.voyago/" + R.drawable.gastronomy, "android.resource://com.example.voyago/" + R.drawable.tokyo)),

        // marti -> 1050097156 (non possiede travel specifico)
        Review(5, 3, 1629230648, "Too many people, but the beach was fun. The water was crystal clear and we had some amazing seafood nearby.", "2024-04-02", 3),

        // fra -> 1626721575 possiede travel -> 8 quindi non può commentare su travel 8
        Review(8, 4, 1145110087, "Sunset yoga was magical. Practicing with the ocean breeze and fading sun made it unforgettable.", "2024-04-06", 5),

        // marc -> 1243173879 (non possiede travel)
        Review(6, 3, 1824944549, "Good party atmosphere. The nightlife was vibrant and there were lots of friendly people to meet.", "2024-04-04", 4, images = listOf("android.resource://com.example.voyago/" + R.drawable.party)),

        // sarozza -> 1145110087 possiede travel -> 10 11 12, quindi NON può commentare su questi travel
        Review(11, 7, 1273795543, "Waves were too strong for beginners, but the instructors kept things safe. Still a thrilling experience!", "2024-04-08", 3),
        Review(12, 7, 1050097156, "Still had a great time. The vibe of the trip was relaxed, and I made some lifelong friends during the journey.", "2024-04-10", 3, images = listOf("android.resource://com.example.voyago/" + R.drawable.sport, "android.resource://com.example.voyago/" + R.drawable.party)),

        // marco -> 1629230648 possiede travel -> 3 4 quindi NON può commentare su 3 e 4
        Review(7, 4, 1145110087, "Great surfing instructors. They were patient, fun, and helped me catch my first wave. Highly recommend!", "2024-04-05", 4, images = listOf("android.resource://com.example.voyago/" + R.drawable.bg_hiking)),

        // matte -> 1824944549 possiede travel -> 6 7 9 quindi NON può commentare su questi
        Review(10, 6, 1619771197, "Loved Tokyo and the food. Sushi was unlike anything I've had before, and the city was full of surprises.", "2024-03-21", 4),
        Review(13, 1, 1273795543, "Very disappointing experience. The place looked nothing like the photos and the room was dirty when we arrived.", "2024-04-11", 1, images = listOf("android.resource://com.example.voyago/" + R.drawable.dolomites)),
        Review(14, 1, 1243173879, "Not worth the price. The itinerary was rushed and we barely had time to enjoy the landmarks properly.", "2024-04-12", 2),

        // Altri esempi di recensioni coerenti con la regola "user non può commentare suoi travel"
        Review(15, 1, 1273795543, "The tour guide was unprofessional and often late. It affected the whole group's experience.", "2024-04-09", 2),
        Review(16, 1, 1273795543, "Avoid this trip. Transportation was unreliable, and the support staff was rude when we tried to get help.", "2024-04-07", 1, images = listOf("android.resource://com.example.voyago/" + R.drawable.tokyo)),
        Review(17, 1, 1050097156, "Food poisoning on the second day completely ruined the experience. Hygiene standards seemed very low.", "2024-04-06", 1),
        Review(18, 1, 1243173879, "Mediocre at best. The views were okay, but accommodations were uncomfortable and too far from main attractions.", "2024-04-05", 2, images = listOf("android.resource://com.example.voyago/" + R.drawable.trail))
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            FirestoreSeeder.seedReviews(initialReviews)
            println("Reviews inserite correttamente in Firestore")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


