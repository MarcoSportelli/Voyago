package com.example.voyago.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.Activity
import com.example.voyago.DataProfile
import com.example.voyago.Destination
import com.example.voyago.Experience
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import com.example.voyago.Participants
import com.example.voyago.Travel
import com.example.voyago.profileViewModel
import com.example.voyago.utils.SupabaseManager
import com.example.voyago.viewModels.TripRequestRepository.db
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.absoluteValue


class TravelViewModel : ViewModel() {

    companion object {
        @Volatile
        private var INSTANCE: TravelViewModel? = null

        fun getInstance(): TravelViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TravelViewModel().also { INSTANCE = it }
            }
        }
    }

    private var _travel = MutableStateFlow(Travel())
    val travel: StateFlow<Travel> = _travel.asStateFlow()

    // Error states
    var titleError by mutableStateOf<String?>(null)
    var destinationError by mutableStateOf<String?>(null)
    var dateError by mutableStateOf<String?>(null)
    var participantsError by mutableStateOf<String?>(null)
    var priceError by mutableStateOf<String?>(null)
    var imagesError by mutableStateOf<String?>(null)

    // Step 1: Basic Info
    fun updateTitle(newTitle: String) {
        _travel.update { it.copy(title = newTitle) }
        validateTitle()
    }

    fun updateDestinations(newDestinations: MutableList<Destination>){
        _travel.update { it.copy(destinations = newDestinations) }
    }

    fun updateExperiences(newExperiences: List<Experience>){
        _travel.update { it.copy(experiences = newExperiences) }
    }

    fun updateDescription(newDescription: String) {
        _travel.update { it.copy(description = newDescription) }
    }

    private fun validateTitle(): Boolean {
        return if (travel.value.title.isBlank()) {
            titleError = "Title is mandatory"
            false
        } else {
            titleError = null
            true
        }
    }

    fun addDestination(destination: Destination) {
        _travel.update { current ->
            val newList = current.destinations.toMutableList().apply {
                add(destination)
            }
            current.copy(destinations = newList)
        }
    }

    fun removeDestination(index: Int) {
        _travel.update { it.copy(destinations = it.destinations.toMutableList().apply { removeAt(index) }) }
    }

    fun updateDestinationMandatory(index: Int, isMandatory: Boolean) {
        val updatedDestinations = _travel.value.destinations.toMutableList()
        updatedDestinations[index] = updatedDestinations[index].copy(isMandatory = isMandatory)
        _travel.update { it.copy(destinations = updatedDestinations) }
    }

    private fun validateDestination(): Boolean {
        return if (travel.value.destinations.isEmpty()) {
            destinationError = "Destination is mandatory"
            false
        } else {
            destinationError = null
            true
        }
    }

    // Step 3: Dates
    fun updateDates(start: String, end: String) {
        _travel.update { it.copy(startDate = start, endDate = end) }
        validateDates()
    }

    private fun validateDates(): Boolean {
        return if (travel.value.startDate.isBlank() || travel.value.endDate.isBlank()) {
            dateError = "Dates are mandatory"
            false
        } else {
            dateError = null
            true
        }
    }

    // Step 4: Participants
    fun updateParticipants(participants: Participants) {
        _travel.update { current ->
            current.copy(maxParticipants = participants)
        }
    }

    private fun validateParticipants(): Boolean {
        return if (travel.value.maxParticipants.adults <= 0 && travel.value.maxParticipants.children <= 0 && travel.value.maxParticipants.newborns <= 0) {
            participantsError = "Must have at least 1 participant"
            false
        } else {
            participantsError = null
            true
        }
    }

    // Step 5: Price
    fun updatePrice(price: Double) {
        _travel.update { it.copy(pricePerPerson = price) }
        validatePrice()
    }

    private fun validatePrice(): Boolean {
        return if (travel.value.pricePerPerson <= 0) {
            priceError = "Price must be positive"
            false
        } else {
            priceError = null
            true
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            // Converti l'Uri in ByteArray
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.use { it.readBytes() }
                ?: return null

            // Genera un nome univoco per il file
            val fileName = "${UUID.randomUUID()}.${context.contentResolver.getType(imageUri)?.substringAfterLast("/") ?: "jpg"}"

            // Carica l'immagine su Supabase

            SupabaseManager.client.storage
                .from("travel-images") // Sostituisci con il nome del tuo bucket
                .upload(
                    path = fileName,
                    data = bytes,
                    upsert = false
                )

            // Ottieni l'URL pubblico
            SupabaseManager.client.storage
                .from("travel-images")
                .publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Step 6: Images
    fun addImage(imageUrl: String) {
        _travel.update { it.copy(images = it.images + imageUrl) }
        validateImages()
    }

    fun removeImage(imageRes: String) {
        _travel.update { it.copy(images = it.images - imageRes) }
        validateImages()
    }

    private fun validateImages(): Boolean {
        return if (travel.value.images.size < 1) {
            imagesError = "Minimum 1 images required"
            false
        } else {
            imagesError = null
            true
        }
    }

    // Activities
    fun addActivity(activity: Activity) {
        _travel.update { it.copy(activities = it.activities + activity) }
    }

    fun removeActivity(activity: Activity) {
        _travel.update { it.copy(activities = it.activities - activity) }
    }

    fun addType(experience: Experience) {
        _travel.update { it.copy(experiences = it.experiences + experience) }
    }

    fun removeType(experience: Experience) {
        _travel.update { it.copy(experiences = it.experiences - experience) }
    }

    // Final validation
    private fun validateAllSteps(): Boolean {
        return validateTitle() &&
                validateDestination() &&
                validateDates() &&
                validateParticipants() &&
                validatePrice() &&
                validateImages()
    }


     fun publishTravel(size: Int): Boolean {

        if (!validateAllSteps()) {
            println(titleError)
            println(dateError)
            println(priceError)
            println(imagesError)
            println(destinationError)
            println(participantsError)
            println("ERRORE VALIDAZIONE PUBLISH")
            return false
        }

         val newTripId = UUID.randomUUID().hashCode().absoluteValue

        _travel.update { it.copy(userId = profileViewModel.id) }
        _travel.update { it.copy(id = newTripId ) }
        _travel.update { it.copy(isPublished = true) }

        recommendedNotification(
            newTripId,
            _travel.value.experiences.map { it.id },
            profileViewModel.id
        )

         lastMinuteNotification(newTripId, departureDateString = travel.value.startDate)

        return true
    }

    fun recommendedNotification(newTripId: Int, newTripExperiences: List<String>, tripCreator: Int) {
        viewModelScope.launch {
            try {

                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("profiles").get().await()
                val allProfiles = snapshot.documents.mapNotNull { it.toObject(DataProfile::class.java) }

                Log.d("recommended", "👉 Invio notifiche recommended")


                for (profile in allProfiles) {

                    Log.d("recommended", "👉 profile: $profile")

                    // Salta il creatore stesso
                    if (profile.id == tripCreator) {
                        continue
                    }
                    val title = getTravelTitle(newTripId)

                    // Conta quante experiences coincidono
                    val commonExperiences = profile.prefExp.intersect(newTripExperiences.toSet())

                    Log.d("recommended", "👉 newTripExperiences: ${newTripExperiences}")
                    Log.d("recommended", "👉 profile.prefExp: ${profile.prefExp}")
                    Log.d("recommended", "👉 commonExperiences: $commonExperiences")


                    if (commonExperiences.size >= 2) {
                        val notificationTitle = "New Trip You Might Like!"
                        val notificationBody = "A new trip matches your interests. Take a look at '${title}'."

                        // Crea e salva la notifica nel DB
                        val notification = Notification(
                            userId = profile.id.toString(),
                            travelId = newTripId,
                            title = notificationTitle,
                            body = notificationBody,
                            read = false,
                            timestamp = Timestamp.now(),
                            type = NotificationType.Recommended
                        )
                        NotificationRepository.addNotification(notification)

                        NotificationSender.sendNotification(
                            type = NotificationType.Recommended,
                            targetUserId = profile.id.toString(),
                            title = notificationTitle,
                            body = notificationBody,
                            travelId = newTripId,
                        )

                        Log.d("TripCheck", "📲 Push notification inviata a utente: ${profile.id}")
                    } else {
                        Log.d("TripCheck", "❌ Esperienze comuni insufficienti (meno di 2), nessuna notifica per utente: ${profile.id}")
                    }
                }

                Log.d("TripCheck", "✅ Notifications sent for matching experiences")
            } catch (e: Exception) {
                Log.e("TripCheck", "❌ Error checking and sending notifications", e)
            }
        }
    }

    fun lastMinuteNotification(newTripId: Int, departureDateString: String) {
        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                val departureDate: Date? = try {
                    dateFormat.parse(departureDateString)
                } catch (e: Exception) {
                    Log.e("UrgentTripCheck", "❌ Errore nel parsing della data: $departureDateString", e)
                    null
                }

                if (departureDate == null) {
                    Log.e("UrgentTripCheck", "❌ Data non valida, uscita dalla funzione")
                    return@launch
                }

                val title = getTravelTitle(newTripId)

                Log.d("UrgentTripCheck", "👉 Inizio controllo viaggio urgente")
                Log.d("UrgentTripCheck", "👉 newTripId: $newTripId")
                Log.d("UrgentTripCheck", "👉 departureDate: $departureDate")

                val currentDate = Date()
                val diffInMillis = departureDate.time - currentDate.time
                val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

                Log.d("UrgentTripCheck", "📅 Giorni mancanti alla partenza: $diffInDays")

                if (diffInDays < 7) {
                    val db = FirebaseFirestore.getInstance()
                    val snapshot = db.collection("profiles").get().await()
                    val allProfiles = snapshot.documents.mapNotNull { it.toObject(DataProfile::class.java) }


                    Log.d("UrgentTripCheck", "📦 Numero profili trovati: ${allProfiles.size}")

                    val notificationTitle = "Last Minute Trip!"
                    val notificationBody = "A new trip departing soon has been published! Check out '${title}'."


                    // Salva UNA notifica sul DB con userId fittizio 9999
                    val notification = Notification(
                        userId = "9999",
                        travelId = newTripId,
                        title = notificationTitle,
                        body = notificationBody,
                        read = false,
                        timestamp = Timestamp.now(),
                        type = NotificationType.LastMinute
                    )
                    NotificationRepository.addNotification(notification)

                    // Invia push notification a tutti i profili
                    for (profile in allProfiles) {
                        Log.d("UrgentTripCheck", "📲 Invio notifica push a utente: ${profile.id}")

                        // Salta il creatore stesso
                        if (profile.id == profileViewModel.id) {
                            continue
                        }
                        NotificationSender.sendNotification(
                            type = NotificationType.LastMinute,
                            targetUserId = profile.id.toString(),
                            title = notificationTitle,
                            body = notificationBody,
                            travelId = newTripId,
                        )
                    }

                    Log.d("UrgentTripCheck", "✅ Notifiche push inviate a tutti i profili")
                } else {
                    Log.d("UrgentTripCheck", "⏰ Mancano più di 7 giorni, nessuna notifica inviata")
                }
            } catch (e: Exception) {
                Log.e("UrgentTripCheck", "❌ Errore nel controllo e invio notifiche urgenti", e)
            }
        }
    }


    fun restart() {
        _travel.value = Travel()
    }

// TravelViewModel.kt

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getTravelTitle(tripId: Int): String {
        return try {
            // 1. Recupera il documento dal DB
            val document = db.collection("travels")
                .document(tripId.toString())
                .get()
                .await()

            // 2. Estrae il titolo (se il documento esiste)
            document.getString("title") ?: ""
        } catch (e: Exception) {
            Log.e("getTravelTitle", "Errore nel recupero del titolo: ${e.message}")
            "" // Fallback in caso di errore
        }
    }

//    fun updateDestinationMandatory(index: Int, isMandatory: Boolean) {
//        _travel.update { currentTravel ->
//            val updatedDestinations = currentTravel.destinations.toMutableList().apply {
//                this[index] = this[index].copy(isMandatory = isMandatory)
//            }
//            currentTravel.copy(destinations = updatedDestinations)
//        }
//    }
}

