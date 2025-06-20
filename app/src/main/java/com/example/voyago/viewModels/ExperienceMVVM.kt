package com.example.voyago.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.Experience
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExperienceViewModel : ViewModel() {

    private val _experiences = MutableStateFlow<List<Experience>>(emptyList())
    val experiences: StateFlow<List<Experience>> = _experiences

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadExperiences()
    }

    fun loadExperiences() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                Firebase.firestore.collection("experiences").get()
                    .addOnSuccessListener { result ->
                        val experiencesList = result.mapNotNull {
                            it.toObject(Experience::class.java)
                        }
                        _experiences.value = experiencesList
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        _errorMessage.value = e.message
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    suspend fun getAllExperiences(): List<Experience> {
        return try {
            val snapshot = Firebase.firestore.collection("experiences").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val data = doc.data

                if (data != null) {
                    Experience(
                        id = id,
                        name = data["name"] as? String ?: "",
                        icon = data["icon"] as? String ?: ""
                    )
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
