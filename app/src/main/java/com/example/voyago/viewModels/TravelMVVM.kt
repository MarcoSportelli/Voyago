package com.example.voyago.viewModels

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.Activity
import com.example.voyago.Address
import com.example.voyago.Destination
import com.example.voyago.Experience
import com.example.voyago.Participants
import com.example.voyago.Travel
import com.example.voyago.TripRequest
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

object TravelRepository {

    private val db = FirebaseFirestore.getInstance()
    private val travelsRef = db.collection("travels")

    private val _travels = MutableStateFlow<List<Travel>>(emptyList())
    val travels: StateFlow<List<Travel>> = _travels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenToTravels()
    }

    fun listenToTravels() {
        travelsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            CoroutineScope(Dispatchers.IO).launch {
                _isLoading.value = true
                val tempList = mutableListOf<Travel>()
                for (doc in snapshot.documents) {
                    try {
                        val id = doc.id.toIntOrNull() ?: continue
                        val title = doc.getString("title") ?: ""
                        val description = doc.getString("description") ?: ""
                        val startDate = doc.getString("startDate") ?: ""
                        val endDate = doc.getString("endDate") ?: ""
                        val pricePerPerson = doc.getDouble("pricePerPerson") ?: 0.0
                        val userIdAny = doc.get("userId")
                        val userId = when (userIdAny) {
                            is String -> userIdAny.toIntOrNull() ?: -1
                            is Long -> userIdAny.toInt()
                            is Int -> userIdAny
                            else -> -1
                        }
                        val isPublished = doc.getBoolean("isPublished") ?: false

                        val maxParticipantsMap = doc.get("maxParticipants") as? Map<String, Long>
                        val maxParticipants = Participants(
                            adults = maxParticipantsMap?.get("adults")?.toInt() ?: 0,
                            children = maxParticipantsMap?.get("children")?.toInt() ?: 0,
                            newborns = maxParticipantsMap?.get("newborns")?.toInt() ?: 0
                        )

                        val images = doc.get("images") as? List<String> ?: emptyList()

                        val destinationList = (doc.get("destinations") as? List<Map<String, Any>>)?.map { destMap ->
                            val addressMap = destMap["address"] as? Map<String, Any> ?: emptyMap()
                            val latLngMap = addressMap["latLng"] as? Map<String, Any> ?: emptyMap()
                            Destination(
                                address = Address(
                                    street = addressMap["street"] as? String ?: "",
                                    city = addressMap["city"] as? String ?: "",
                                    region = addressMap["region"] as? String ?: "",
                                    country = addressMap["country"] as? String ?: "",
                                    fullAddress = addressMap["fullAddress"] as? String ?: "",
                                    latLng = LatLng(
                                        latLngMap["latitude"] as? Double ?: 0.0,
                                        latLngMap["longitude"] as? Double ?: 0.0
                                    )
                                ),
                                isMandatory = destMap["isMandatory"] as? Boolean ?: false
                            )
                        } ?: emptyList()

                        val activityIds = doc.get("activities") as? List<String> ?: emptyList()
                        val activities = activityIds.mapNotNull { id ->
                            try {
                                val snap = db.collection("activities").document(id).get().await()
                                val name = snap.getString("name") ?: return@mapNotNull null
                                val icon = snap.getString("icon") ?: ""
                                Activity(
                                    id = snap.id,
                                    name = name,
                                    icon = icon
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }

                        val experienceIds = doc.get("experience") as? List<String> ?: emptyList()
                        val experiences = experienceIds.mapNotNull { id ->
                            try {
                                val snap = db.collection("experiences").document(id).get().await()
                                val name = snap.getString("name") ?: return@mapNotNull null
                                val icon = snap.getString("icon") ?: ""
                                Experience(
                                    id = snap.id,
                                    name = name,
                                    icon = icon
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }

                        val travel = Travel(
                            id = id,
                            title = title,
                            description = description,
                            destinations = destinationList as MutableList<Destination>,
                            startDate = startDate,
                            endDate = endDate,
                            maxParticipants = maxParticipants,
                            pricePerPerson = pricePerPerson,
                            images = images,
                            activities = activities,
                            experiences = experiences,
                            userId = userId,
                            isPublished = isPublished
                        )

                        tempList.add(travel)
                        println(travel)
                        _travels.value = tempList.toList()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                _isLoading.value = false
            }
        }
    }


    fun addTravel(travel: Travel) {
        val travelData = hashMapOf(
            "id" to travel.id,
            "title" to travel.title,
            "description" to travel.description,
            "startDate" to travel.startDate,
            "endDate" to travel.endDate,
            "pricePerPerson" to travel.pricePerPerson,
            "userId" to travel.userId,
            "isPublished" to travel.isPublished,
            "images" to travel.images,
            "maxParticipants" to hashMapOf(
                "adults" to travel.maxParticipants.adults,
                "children" to travel.maxParticipants.children,
                "newborns" to travel.maxParticipants.newborns
            ),
            "destinations" to travel.destinations.map {
                mapOf(
                    "address" to mapOf(
                        "street" to it.address.street,
                        "city" to it.address.city,
                        "region" to it.address.region,
                        "country" to it.address.country,
                        "fullAddress" to it.address.fullAddress,
                        "latLng" to mapOf(
                            "latitude" to it.address.latLng.latitude,
                            "longitude" to it.address.latLng.longitude
                        )
                    ),
                    "isMandatory" to it.isMandatory
                )
            },
            "activities" to travel.activities.map { it.id },
            "experience" to travel.experiences.map { it.id }
        )

        travelsRef.document(travel.id.toString()).set(travelData)
    }


    fun updateTravel(travel: Travel) {
        val travelData = hashMapOf(
            "id" to travel.id,
            "title" to travel.title,
            "description" to travel.description,
            "startDate" to travel.startDate,
            "endDate" to travel.endDate,
            "pricePerPerson" to travel.pricePerPerson,
            "userId" to travel.userId,
            "isPublished" to travel.isPublished,
            "images" to travel.images,
            "maxParticipants" to hashMapOf(
                "adults" to travel.maxParticipants.adults,
                "children" to travel.maxParticipants.children,
                "newborns" to travel.maxParticipants.newborns
            ),
            "destinations" to travel.destinations.map {
                mapOf(
                    "address" to mapOf(
                        "street" to it.address.street,
                        "city" to it.address.city,
                        "region" to it.address.region,
                        "country" to it.address.country,
                        "fullAddress" to it.address.fullAddress,
                        "latLng" to mapOf(
                            "latitude" to it.address.latLng.latitude,
                            "longitude" to it.address.latLng.longitude
                        )
                    ),
                    "isMandatory" to it.isMandatory
                )
            },
            "activities" to travel.activities.map { it.id },
            "experience" to travel.experiences.map { it.id }
        )

        travelsRef.document(travel.id.toString()).set(travelData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Travel updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Failed to update travel: ${e.message}", e)
            }
    }

    suspend fun deleteTravel(id: Int) {

            try {
                val db = FirebaseFirestore.getInstance()

                db.collection("travels").document(id.toString()).delete().await()
                Log.d("Travel", "✅ Travel $id eliminato")

                val chatsSnapshot = db.collection("chats")
                    .whereEqualTo("tripId", id.toString()) // se salvato come stringa
                    .get().await()
                for (chatDoc in chatsSnapshot.documents) {
                    // Elimina i messaggi nella sottocollezione
                    val messagesSnapshot = db.collection("chats/${chatDoc.id}/messages").get().await()
                    val batch = db.batch()
                    for (msg in messagesSnapshot.documents) {
                        batch.delete(msg.reference)
                    }
                    batch.delete(chatDoc.reference)
                    batch.commit().await()
                    Log.d("Travel", "🗑️ Chat ${chatDoc.id} + messaggi eliminati")
                }
                val pastSnapshot = db.collection("pastTravel")
                    .whereEqualTo("travelId", id)
                    .get().await()
                for (past in pastSnapshot.documents) {
                    db.collection("pastTravel").document(past.id).delete().await()
                    Log.d("Travel", "🗑️ PastTravel ${past.id} eliminato")
                }

                val requestsSnapshot = db.collection("requests")
                    .whereEqualTo("tripId", id)
                    .get().await()
                for (req in requestsSnapshot.documents) {
                    db.collection("requests").document(req.id).delete().await()
                    Log.d("Travel", "🗑️ Request ${req.id} eliminata")
                }

//                val reviewsSnapshot = db.collection("reviews")
//                    .whereEqualTo("travelId", id)
//                    .get().await()
//                for (doc in reviewsSnapshot.documents) {
//                    db.collection("reviews").document(doc.id).delete().await()
//                    Log.d("Travel", "🗑️ Review ${doc.id} eliminata")
//                }

                val bookedSnapshot = db.collection("bookedTravels")
                    .whereEqualTo("travelId", id)
                    .get().await()
                for (doc in bookedSnapshot.documents) {
                    db.collection("bookedTravels").document(doc.id).delete().await()
                    Log.d("Travel", "🗑️ BookedTravel ${doc.id} eliminato")
                }
                Log.d("Travel", "🎉 Eliminazione completa per travel $id")

            } catch (e: Exception) {
                Log.e("Travel", "❌ Errore eliminazione travel $id", e)
            }

    }


    suspend fun getTravelById(id: Int): Travel? {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val snapshot = firestore
                .collection("travels")
                .whereEqualTo("id", id)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return null
            val data = snapshot.documents.firstOrNull()?.data ?: return null

            // Campi base
            val title = data["title"] as? String ?: ""
            val description = data["description"] as? String ?: ""
            val startDate = data["startDate"] as? String ?: ""
            val endDate = data["endDate"] as? String ?: ""
            val pricePerPerson = (data["pricePerPerson"] as? Number)?.toDouble() ?: 0.0
            val userId = (data["userId"] as? Number)?.toInt() ?: -1
            val isPublished = data["isPublished"] as? Boolean ?: false
            val createdAt = (data["createdAt"] as? String)?.let { LocalDate.parse(it) } ?: LocalDate.now()
            val images = (data["images"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            // Partecipanti
            val participantsMap = data["maxParticipants"] as? Map<*, *>
            val maxParticipants = Participants(
                adults = (participantsMap?.get("adults") as? Number)?.toInt() ?: 0,
                children = (participantsMap?.get("children") as? Number)?.toInt() ?: 0,
                newborns = (participantsMap?.get("newborns") as? Number)?.toInt() ?: 0
            )

            // Destinazioni
            val destinationsList = (data["destinations"] as? List<*>)?.mapNotNull { dest ->
                val destMap = dest as? Map<*, *> ?: return@mapNotNull null
                val isMandatory = destMap["isMandatory"] as? Boolean ?: true
                val addressMap = destMap["address"] as? Map<*, *> ?: return@mapNotNull null
                val latLngMap = addressMap["latLng"] as? Map<*, *>
                val lat = (latLngMap?.get("latitude") as? Number)?.toDouble() ?: 0.0
                val lng = (latLngMap?.get("longitude") as? Number)?.toDouble() ?: 0.0

                val address = Address(
                    street = addressMap["street"] as? String ?: "",
                    city = addressMap["city"] as? String ?: "",
                    region = addressMap["region"] as? String ?: "",
                    country = addressMap["country"] as? String ?: "",
                    fullAddress = addressMap["fullAddress"] as? String ?: "",
                    latLng = LatLng(lat, lng)
                )

                Destination(address = address, isMandatory = isMandatory)
            } ?: emptyList()

            // ID collegati
            val activitiesIds = (data["activities"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val experiencesIds = (data["experiences"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            // Funzione per recuperare documenti da una lista di ID
            suspend fun <T> fetchByIds(
                collectionName: String,
                ids: List<String>,
                mapper: (Map<String, Any>) -> T
            ): List<T> {
                return ids.mapNotNull { docId ->
                    val docSnap = firestore.collection(collectionName).document(docId).get().await()
                    val docData = docSnap.data
                    try {
                        docData?.let { mapper(it as Map<String, Any>) }
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            // Carica activities e experiences
            val activities = fetchByIds("activities", activitiesIds) { map ->
                Activity(
                    id = map["id"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    icon = map["icon"] as? String ?: ""
                )
            }

            val experiences = fetchByIds("experiences", experiencesIds) { map ->
                Experience(
                    id = map["id"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    icon = map["icon"] as? String ?: ""
                )
            }

            // Restituisci il Travel finale
            Travel(
                id = id,
                title = title,
                description = description,
                destinations = destinationsList.toMutableList(),
                startDate = startDate,
                endDate = endDate,
                maxParticipants = maxParticipants,
                pricePerPerson = pricePerPerson,
                images = images,
                activities = activities,
                experiences = experiences,
                userId = userId,
                createdAt = createdAt,
                isPublished = isPublished
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun getTravelsByUser(userId: String): List<Travel> {
        val firestore = FirebaseFirestore.getInstance()
        val travelsSnapshot = firestore.collection("travels")
            .whereEqualTo("userId", userId.toIntOrNull()) // Assicurati che sia Int
            .get()
            .await()

        return travelsSnapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val data = doc.data ?: return@mapNotNull null

            try {
                val title = data["title"] as? String ?: ""
                val description = data["description"] as? String ?: ""
                val startDate = data["startDate"] as? String ?: ""
                val endDate = data["endDate"] as? String ?: ""
                val pricePerPerson = (data["pricePerPerson"] as? Number)?.toDouble() ?: 0.0
                val travelUserId = (data["userId"] as? Number)?.toInt() ?: -1
                val isPublished = data["isPublished"] as? Boolean ?: false
                val createdAtString = data["createdAt"] as? String
                val createdAt = createdAtString?.let { LocalDate.parse(it) } ?: LocalDate.now()
                val images = (data["images"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                // Participants
                val participantsMap = data["maxParticipants"] as? Map<*, *>
                val maxParticipants = Participants(
                    adults = (participantsMap?.get("adults") as? Number)?.toInt() ?: 0,
                    children = (participantsMap?.get("children") as? Number)?.toInt() ?: 0,
                    newborns = (participantsMap?.get("newborns") as? Number)?.toInt() ?: 0
                )

                // Destinations
                val destinationsList = (data["destinations"] as? List<*>)?.mapNotNull { dest ->
                    val destMap = dest as? Map<*, *> ?: return@mapNotNull null
                    val isMandatory = destMap["isMandatory"] as? Boolean ?: true
                    val addressMap = destMap["address"] as? Map<*, *> ?: return@mapNotNull null
                    val latLngMap = addressMap["latLng"] as? Map<*, *>
                    val lat = (latLngMap?.get("latitude") as? Number)?.toDouble() ?: 0.0
                    val lng = (latLngMap?.get("longitude") as? Number)?.toDouble() ?: 0.0

                    val address = Address(
                        street = addressMap["street"] as? String ?: "",
                        city = addressMap["city"] as? String ?: "",
                        region = addressMap["region"] as? String ?: "",
                        country = addressMap["country"] as? String ?: "",
                        fullAddress = addressMap["fullAddress"] as? String ?: "",
                        latLng = LatLng(lat, lng)
                    )
                    Destination(address = address, isMandatory = isMandatory)
                } ?: emptyList()

                // Activity & Experience IDs
                val activitiesIds = (data["activities"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val experiencesIds = (data["experiences"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                // Funzione interna per fetchare oggetti da una collezione
                suspend fun <T> fetchByIds(
                    collectionName: String,
                    ids: List<String>,
                    mapper: (Map<String, Any>) -> T
                ): List<T> {
                    val result = mutableListOf<T>()
                    for (docId in ids) {
                        val docSnap = firestore.collection(collectionName).document(docId).get().await()
                        val docData = docSnap.data
                        if (docData != null) {
                            try {
                                result.add(mapper(docData))
                            } catch (_: Exception) {
                            }
                        }
                    }
                    return result
                }

                val activities = fetchByIds("activities", activitiesIds) { map ->
                    Activity(
                        id = map["id"] as? String ?: "",
                        name = map["name"] as? String ?: "",
                        icon = map["icon"] as? String ?: ""
                    )
                }

                val experiences = fetchByIds("experiences", experiencesIds) { map ->
                    Experience(
                        id = map["id"] as? String ?: "",
                        name = map["name"] as? String ?: "",
                        icon = map["icon"] as? String ?: ""
                    )
                }

                Travel(
                    id = id.toIntOrNull() ?: -1,
                    title = title,
                    description = description,
                    destinations = destinationsList.toMutableList(),
                    startDate = startDate,
                    endDate = endDate,
                    maxParticipants = maxParticipants,
                    pricePerPerson = pricePerPerson,
                    images = images,
                    activities = activities,
                    experiences = experiences,
                    userId = travelUserId,
                    createdAt = createdAt,
                    isPublished = isPublished
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}


class TravelViewModels : ViewModel() {



    val travels: StateFlow<List<Travel>> = TravelRepository.travels



    fun addTravel(travel: Travel) {
        viewModelScope.launch {
            TravelRepository.addTravel(travel)
        }
    }

    fun updateTravel(travel: Travel) {
        viewModelScope.launch {
            TravelRepository.updateTravel(travel)
        }
    }

    fun deleteTravel(id: Int) {
        viewModelScope.launch {
            TravelRepository.deleteTravel(id)
        }
    }

     suspend fun getTravelById(id: Int): Travel? {
        return TravelRepository.getTravelById(id)
    }

    suspend fun getTravelsByUser(userId: String): List<Travel> {
        return TravelRepository.getTravelsByUser(userId)
    }
}
