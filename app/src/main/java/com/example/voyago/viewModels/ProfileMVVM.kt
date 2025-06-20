package com.example.voyago.viewModels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.DataProfile
import com.example.voyago.Experience
import com.example.voyago.PreferredDestination
import com.example.voyago.utils.SupabaseManager
import com.example.voyago.viewModels.ProfileRepository.updateProfile
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ProfileRepository {
    private val _profile = MutableStateFlow<DataProfile?>(null)
    val profile: StateFlow<DataProfile?> = _profile.asStateFlow()

    fun updateProfile(newProfile: DataProfile) {
        _profile.update { newProfile }
    }

    suspend fun getProfileByUserId(userId: String): DataProfile? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.toObject(DataProfile::class.java)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getProfileByInternalId(internalId: Int): DataProfile? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("profiles")
                .whereEqualTo("id", internalId)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(DataProfile::class.java)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow<DataProfile?>(null)
    val profile: StateFlow<DataProfile?> = _profile.asStateFlow()

    fun reloadProfileByInternalId(internalId: Int) {
        viewModelScope.launch {
            val newProfile = ProfileRepository.getProfileByInternalId(internalId)
            if (newProfile != null) {
                _profile.value = newProfile
            }
        }
    }

    private fun updateProfileById(updatedProfile: DataProfile) {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val querySnapshot = firestore
                    .collection("profiles")
                    .whereEqualTo("id", updatedProfile.id)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val documentId = document.id

                    firestore.collection("profiles")
                        .document(documentId)
                        .set(updatedProfile)
                        .await()

                    updateProfile(updatedProfile)
                } else {
                    println("Profile with id=${updatedProfile.id} not found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            val loadedProfile = ProfileRepository.getProfileByUserId(userId)
            _profile.value = loadedProfile
            loadedProfile?.let { initializeState(it) }
        }
    }

    suspend fun getProfileByInternalId(userId: Int): DataProfile? {
        return ProfileRepository.getProfileByInternalId(userId)
    }

    suspend fun getProfileByUserId(userId: String): DataProfile? {
        return ProfileRepository.getProfileByUserId(userId)
    }


    // Stato del form dinamico
    var id by mutableStateOf(0)
        private set
    var name by mutableStateOf("")
    var surname by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var img by mutableStateOf("")
    var prefDest: List<String> by mutableStateOf(emptyList<String>())
    var prefExp: List<String> by mutableStateOf(emptyList<String>())
    var facebook by mutableStateOf("")
    var instagram by mutableStateOf("")
    var about by mutableStateOf("")
    var languages by mutableStateOf(emptyList<String>())
    var memberSince by mutableStateOf("")
    var responseRate by mutableStateOf("")
    var responseTime by mutableStateOf("")
    var lastSeen by mutableStateOf("")

    // Errori validazione
    var nameError by mutableStateOf<String?>(null)
    var surnameError by mutableStateOf<String?>(null)
    var usernameError by mutableStateOf<String?>(null)
    var emailError by mutableStateOf<String?>(null)
    var phoneError by mutableStateOf<String?>(null)

    private fun initializeState(profile: DataProfile) {
        id = profile.id
        name = profile.name
        surname = profile.surname
        username = profile.username
        email = profile.email
        phone = profile.phone
        img = profile.img ?: ""
        prefDest = profile.prefDest
        prefExp = profile.prefExp
        facebook = profile.facebook ?: ""
        instagram = profile.instagram ?: ""
        about = profile.about ?: ""
        languages = profile.languages
        memberSince = profile.memberSince ?: ""
        responseRate = profile.responseRate ?: ""
        responseTime = profile.responseTime ?: ""
        lastSeen = profile.lastSeen ?: ""
    }

    // Validazione
    private fun validateName() = validateField(name, "Name") { nameError = it }
    private fun validateSurname() = validateField(surname, "Surname") { surnameError = it }
    private fun validateUsername(): Boolean {
        return when {
            username.isBlank() -> {
                usernameError = "Username is mandatory"
                false
            }
            username.length < 3 -> {
                usernameError = "Username must have at least 3 characters"
                false
            }
            else -> {
                usernameError = null
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isBlank() -> {
                emailError = "Email is mandatory"
                false
            }
            !email.matches(emailPattern.toRegex()) -> {
                emailError = "Insert a valid email"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }

    private fun validatePhone(): Boolean {
        val phonePattern = "^[+]?[0-9]{10,13}$"
        return when {
            phone.isBlank() -> {
                phoneError = "Phone is mandatory"
                false
            }
            !phone.matches(phonePattern.toRegex()) -> {
                phoneError = "Enter a valid number (10-13 digits)"
                false
            }
            else -> {
                phoneError = null
                true
            }
        }
    }

    private fun validateField(value: String, fieldName: String, errorField: (String?) -> Unit): Boolean {
        return if (value.isBlank()) {
            errorField("$fieldName is mandatory")
            false
        } else {
            errorField(null)
            true
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.use { it.readBytes() }
                ?: return null

            val fileName = "${UUID.randomUUID()}.${context.contentResolver.getType(imageUri)?.substringAfterLast("/") ?: "jpg"}"

            SupabaseManager.client.storage
                .from("profile-images")
                .upload(
                    path = fileName,
                    data = bytes,
                    upsert = true
                )
            val follia = SupabaseManager.client.storage
                .from("profile-images")
                .publicUrl(fileName)

            println("FOOOOOOOOOO" + follia)
            follia
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun updateName(value: String) { name = value; validateName() }
    fun updateSurname(value: String) { surname = value; validateSurname() }
    fun updateUsername(value: String) { username = value; validateUsername() }
    fun updateEmail(value: String) { email = value; validateEmail() }
    fun updatePhone(value: String) { phone = value; validatePhone() }
    fun updateImg(value: String?) { img = value ?: "" }
    fun updateFacebook(value: String) { facebook = value }
    fun updateInstagram(value: String) { instagram = value }
    fun updatePrefDest(value: List<PreferredDestination>) {
        prefDest = value.map { it.id }
    }

    fun updatePrefExp(value: List<Experience>) {
        prefExp = value.map { it.id }
    }

    fun saveProfile(): Boolean {
        val isValid = validateName() &&
                validateSurname() &&
                validateEmail()

        if (isValid) {
            val updatedProfile = DataProfile(
                id = id,
                name = name,
                surname = surname,
                username = username,
                email = email,
                img = img,
                phone = phone,
                prefDest = prefDest,
                prefExp = prefExp,
                facebook = facebook,
                instagram = instagram,
                about = about,
                languages = languages,
                memberSince = memberSince,
                responseRate = responseRate,
                responseTime = responseTime,
                lastSeen = lastSeen,
            )
            updateProfileById(updatedProfile)
        }

        return isValid
    }
}
