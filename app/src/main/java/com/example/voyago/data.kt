package com.example.voyago

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

import java.io.Serializable
import java.time.LocalDate

data class Notification(
    val id: String = "",
    val userId: String,
    val travelId: Int? = null,
    val title: String,
    val body: String,
    val read: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val type: NotificationType,
    val imageUrl: String? = null
)
enum class NotificationType{
    LastMinute,
    Recommended,
    RequestSend,
    RequestReceived,
    ReviewReceived,
    Message
}
const val GLOBAL_NOTIFICATION_USER_ID = "9999"

data class PreferredDestination(
    val id: String = "", // es: "tokyo"
    val imageUrl: String = "",
    val name: String = "",
    val icon: String = ""
)

data class Experience(
    val id: String = "", // es: "adventure"
    val icon: String = "",
    val name: String = ""
)

data class Activity(
    val id: String = "", // es: "hiking"
    val name: String = "",
    val icon: String = ""
)

data class DataProfile(
    val id: Int = 0,
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val email: String = "",
    val img: String = "",
    val phone: String = "",
    val instagram: String = "",
    val facebook: String = "",
    val prefDest: List<String> = emptyList(), // es: ["tokyo", "parigi"]
    val prefExp: List<String> = emptyList(), // es: ["adventure", "gastronomy"]
    val about: String = "",
    val languages: List<String> = emptyList(),
    val memberSince: String = "",
    val responseRate: String = "",
    val responseTime: String = "",
    val lastSeen: String = ""
)

data class Review(
    var id: Int,
    val travelId: Int,
    val userId: Int,
    val reviewText: String,
    val date: String,
    val rating: Int,
    val images: List<String?> = emptyList()
) : Serializable


data class TripRequest(
    val id: Int,
    val tripId: Int,
    val senderId: Int,
    val receiverId: Int,
    val adults: Int,
    val children: Int,
    var status: RequestStatus = RequestStatus.PENDING
)
enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class BookedTravel(
    var id: Int,
    var userId: Int,
    var travelId: Int
)
data class PastTravel(
    var id: Int,
    var userId: Int,
    var travelId: Int
)


data class Destination(
    val address: Address = Address(),
    val isMandatory: Boolean = true
)

data class Participants(
    var adults: Int = 0,
    var children: Int = 0,
    var newborns: Int = 0,
) : Serializable
data class Travel(
    val id: Int = -1,
    var title: String = "",
    var description: String = "",
    var destinations: MutableList<Destination> = mutableListOf(),
    var startDate: String = "",
    var endDate: String = "",
    var maxParticipants: Participants = Participants(0,0,0),
    var pricePerPerson: Double = 0.0,
    var images: List<String?> = emptyList(),
    var activities: List<Activity> = emptyList(),
    var experiences: List<Experience> = emptyList(),
    var userId: Int = -1,
    var createdAt: LocalDate = LocalDate.now(),
    var isPublished: Boolean = false,
) : Serializable
data class Address(
    val street: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "",
    val fullAddress: String = "",
    val latLng: LatLng = LatLng(0.0, 0.0)
)





data class FavoriteTravel(
    var id: Int,
    var userId: Int,
    var travelId: Int
)




// Modifiche alle classi dati per Firebase
data class Message(
    val id: String = "", // Lasciamo vuoto per Firebase auto-ID
    val text: String = "",
    val senderId: String, // Cambiato a String per Firebase UID
    val senderName: String, // Aggiunto per mostrare il nome senza fare query aggiuntive
    val isFromCurrentUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(), // Usiamo Long per timestamp
    val status: MessageStatus = MessageStatus.SENT
)

// Manteniamo l'enum come prima
enum class MessageStatus {
    SENT, DELIVERED, READ
}

sealed class ChatItem {
    abstract val id: String
    abstract val tripId: String
    abstract val name: String
    abstract val lastMessage: String
    abstract val timestamp: Long
    abstract val unreadCount: Int
    abstract val participants: List<String>
    abstract val type: String

    data class OrganizerChat(
        override val id: String,
        override val tripId: String,
        override val name: String,
        val image: String?,
        override val lastMessage: String,
        override val timestamp: Long,
        override val unreadCount: Int,
        override val participants: List<String>,
        override val type: String = "organizer"
    ) : ChatItem()

    data class TripChat(
        override val id: String,
        override val tripId: String,
        override val name: String,
        val image: String?,
        override val lastMessage: String,
        override val timestamp: Long,
        override val unreadCount: Int,
        override val participants: List<String>,
        override val type: String = "trip"
    ) : ChatItem()
}

