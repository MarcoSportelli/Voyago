package com.example.voyago.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.example.voyago.FavoriteTravel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.absoluteValue

object FavoriteRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val favoriteRef = firestore.collection("favoriteTravels")

    private val _favoriteList = MutableStateFlow<List<FavoriteTravel>>(emptyList())
    val favoriteList: StateFlow<List<FavoriteTravel>> = _favoriteList.asStateFlow()

    suspend fun addFavorite(userId: Int, travelId: Int) {
        try {
            val newId = UUID.randomUUID().hashCode().absoluteValue
            val newFavorite = FavoriteTravel(
                id = newId,
                travelId = travelId,
                userId = userId
            )
            favoriteRef.document().set(newFavorite).await()
            _favoriteList.update { it + newFavorite }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteFavoriteByTravelId(userId: Int, travelId: Int) {
        val item = _favoriteList.value.find { it.travelId == travelId && it.userId == userId } ?: return
        try {
            favoriteRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("travelId", travelId)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.reference
                ?.delete()
                ?.await()

            _favoriteList.update { list -> list.filterNot { it.travelId == travelId && it.userId == userId } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun getFavoriteTravelIdsByUser(userId: Int): List<Int> {
        return try {
            val snapshot = favoriteRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val favoriteListFromDb = snapshot.documents.mapNotNull { doc ->
                val id = (doc.getLong("id") ?: return@mapNotNull null).toInt()
                val travelId = (doc.getLong("travelId") ?: return@mapNotNull null).toInt()
                val userIdField = (doc.getLong("userId") ?: return@mapNotNull null).toInt()

                FavoriteTravel(
                    id = id,
                    travelId = travelId,
                    userId = userIdField
                )
            }

            _favoriteList.value = favoriteListFromDb
            favoriteListFromDb.map { it.travelId }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
class FavoriteViewModel : ViewModel() {

    val favoriteList: StateFlow<List<FavoriteTravel>> = FavoriteRepository.favoriteList

    fun addFavorite(userId: Int, travelId: Int) {
        viewModelScope.launch {
            FavoriteRepository.addFavorite(userId, travelId)
        }
    }

    fun deleteFavorite(userId: Int, travelId: Int) {
        viewModelScope.launch {
            FavoriteRepository.deleteFavoriteByTravelId(userId,travelId)
        }
    }

    suspend fun getUserFavoriteTravelIds(userId: Int): List<Int> {
        return FavoriteRepository.getFavoriteTravelIdsByUser(userId)
    }
}
