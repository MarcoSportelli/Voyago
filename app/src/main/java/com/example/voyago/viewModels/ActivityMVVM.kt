package com.example.voyago.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.voyago.Activity
import com.example.voyago.Experience
import kotlinx.coroutines.tasks.await

class ActivityViewModel : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadActivities()
    }

    suspend fun getAllActivities(): List<Activity> {
        return try {
            val snapshot = Firebase.firestore.collection("activities").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val data = doc.data

                if (data != null) {
                    Activity(
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
    fun loadActivities() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                Firebase.firestore.collection("activities").get()
                    .addOnSuccessListener { result ->
                        val activitiesList = result.mapNotNull {
                            it.toObject(Activity::class.java)
                        }
                        _activities.value = activitiesList
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
}

