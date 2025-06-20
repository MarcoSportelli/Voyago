package com.example.voyago.viewModels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voyago.Activity
import com.example.voyago.Destination
import com.example.voyago.Experience
import com.example.voyago.Participants
import com.example.voyago.Travel
import com.example.voyago.utils.SupabaseManager
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.UUID

class EditTravelViewModelFactory(private val trav: Travel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTravelViewModel::class.java)) {
            return EditTravelViewModel(trav) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class EditTravelViewModel(private val originalTravel: Travel) : ViewModel() {

    private val _travel = MutableStateFlow(originalTravel)
    val travel: StateFlow<Travel> get() = _travel

    var id by mutableStateOf(originalTravel.id)
        private set
    var title by mutableStateOf(originalTravel.title)
        private set
    var description by mutableStateOf(originalTravel.description)
        private set
    var destinations by mutableStateOf(originalTravel.destinations)
        private set
    var startDate by mutableStateOf(originalTravel.startDate)
        private set
    var endDate by mutableStateOf(originalTravel.endDate)
        private set
    var maxParticipants by mutableStateOf(originalTravel.maxParticipants)
        private set
    var pricePerPerson by mutableStateOf(originalTravel.pricePerPerson)
        private set
    var images = mutableStateListOf<String>().apply { addAll(originalTravel.images.filterNotNull()) }
        private set
    var activities by mutableStateOf(originalTravel.activities)
        private set
    var experiences by mutableStateOf(originalTravel.experiences)
        private set
    var userId by mutableStateOf(originalTravel.userId)
        private set
    var createdAt by mutableStateOf(originalTravel.createdAt)
        private set
    var isPublished by mutableStateOf(originalTravel.isPublished)
        private set

    var titleError by mutableStateOf<String?>(null)
    var priceError by mutableStateOf<String?>(null)

    private fun validateTitle(): Boolean {
        return if (title.isBlank()) {
            titleError = "Title cannot be empty"
            false
        } else {
            titleError = null
            true
        }
    }

    private fun validatePrice(): Boolean {
        return if (pricePerPerson < 0) {
            priceError = "Price must be non-negative"
            false
        } else {
            priceError = null
            true
        }
    }

    fun updateTitle(newTitle: String) {
        title = newTitle
        validateTitle()
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateStartDate(newStartDate: String) {
        startDate = newStartDate
    }

    fun updateEndDate(newEndDate: String) {
        endDate = newEndDate
    }

    fun updatePricePerPerson(newPrice: Double) {
        pricePerPerson = newPrice
        validatePrice()
    }

    fun updateMaxParticipants(participants: Participants) {
        maxParticipants = participants
    }

    fun updateExperiences(newExperiences: List<Experience>) {
        experiences = newExperiences
    }

    fun updateIsPublished(published: Boolean) {
        isPublished = published
    }

    fun updateCreatedAt(date: LocalDate) {
        createdAt = date
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.use { it.readBytes() } ?: return null
            val fileName = "${UUID.randomUUID()}.${context.contentResolver.getType(imageUri)?.substringAfterLast("/") ?: "jpg"}"

            SupabaseManager.client.storage
                .from("travel-images")
                .upload(
                    path = fileName,
                    data = bytes,
                    upsert = false
                )

            SupabaseManager.client.storage
                .from("travel-images")
                .publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addImage(uri: String) {
        images.add(uri)
    }

    fun removeImageAt(index: Int) {
        if (index in images.indices) {
            images.removeAt(index)
        }
    }

    fun updateActivities(newActivities: List<Activity>) {
        activities = newActivities
    }

    fun updateDestinations(newDestinations: List<Destination>) {
        destinations = newDestinations.toMutableList()
    }

    fun saveTravel(): Boolean {
        val isValid = validateTitle() && validatePrice()

        if (isValid) {
            val updatedTravel = Travel(
                id = id,
                title = title,
                description = description,
                destinations = destinations.toMutableList(),
                startDate = startDate,
                endDate = endDate,
                maxParticipants = maxParticipants,
                pricePerPerson = pricePerPerson,
                images = images.toList(),
                activities = activities,
                experiences = experiences,
                userId = userId,
                createdAt = createdAt,
                isPublished = isPublished
            )
            _travel.value = updatedTravel
        }

        return isValid
    }

    // Helper Functions
    fun getStringValue(fieldName: String): String {
        return when (fieldName) {
            "title" -> title
            "description" -> description
            "startDate" -> startDate
            "endDate" -> endDate
            "pricePerPerson" -> pricePerPerson.toString()
            else -> ""
        }
    }

    fun updateStringField(fieldName: String, value: String) {
        when (fieldName) {
            "title" -> title = value
            "description" -> description = value
        }
    }

    fun getDate(fieldName: String): LocalDate {
        return when (fieldName) {
            "startDate" -> LocalDate.parse(startDate)
            "endDate" -> LocalDate.parse(endDate)
            else -> LocalDate.now()
        }
    }

    fun updateDate(fieldName: String, date: LocalDate) {
        when (fieldName) {
            "startDate" -> startDate = date.toString()
            "endDate" -> endDate = date.toString()
        }
    }
}
