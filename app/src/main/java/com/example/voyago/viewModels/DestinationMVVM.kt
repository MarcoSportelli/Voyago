package com.example.voyago.viewModels

import com.example.voyago.Destination
import com.example.voyago.PreferredDestination
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DestinationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("destinations")

    suspend fun getAllDestinations(): List<PreferredDestination> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val data = doc.data

                if (data != null) {
                    PreferredDestination(
                        id = id,
                        imageUrl = data["imageUrl"] as? String ?: "",
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

    suspend fun getDestinationById(destId: String): PreferredDestination? {
        return try {
            val doc = collection.document(destId).get().await()
            if (doc.exists()) {
                val data = doc.data
                if (data != null) {
                    PreferredDestination(
                        id = doc.id,
                        imageUrl = data["imageUrl"] as? String ?: "",
                        name = data["name"] as? String ?: "",
                        icon = data["icon"] as? String ?: ""
                    )
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
