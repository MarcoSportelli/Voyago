package com.example.voyago.viewModels

import android.util.Log
import com.example.voyago.BookedTravel
import com.example.voyago.TripRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.absoluteValue

object BookedRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val bookedRef = firestore.collection("bookedTravels")

    private val _bookedList = MutableStateFlow<List<BookedTravel>>(emptyList())
    val bookedList: StateFlow<List<BookedTravel>> = _bookedList.asStateFlow()


    suspend fun addBooked(request: TripRequest) {
        try {
            val newId = UUID.randomUUID().hashCode().absoluteValue
            val newBooked = BookedTravel(
                id = newId,
                travelId = request.tripId,
                userId = request.senderId
            )
            bookedRef.document().set(newBooked).await()
            _bookedList.update { it + newBooked }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteBookedByTravelId(userId: Int, travelId: Int) {
        val item = _bookedList.value.find { it.travelId == travelId && it.userId == userId } ?: return
        try {
            val snapshot = bookedRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("travelId", travelId)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.reference?.delete()?.await()

            _bookedList.update { list -> list.filterNot { it.travelId == travelId && it.userId == userId } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun getBookedTravelIdsByUser(userId: Int): List<Int> {
        Log.d("BookedTravelRepo", "📥 Inizio recupero booked travels per userId: $userId")

        return try {
            val snapshot = bookedRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val bookedListFromDb = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id")?.toInt()
                val travelId = doc.getLong("travelId")?.toInt()
                val userIdField = doc.getLong("userId")?.toInt()

                if (id != null && travelId != null && userIdField != null) {
                    Log.d("BookedTravelRepo", "✅ BookedTravel valido: id=$id, travelId=$travelId")
                    BookedTravel(id = id, travelId = travelId, userId = userIdField)
                } else {
                    Log.w("BookedTravelRepo", "⚠️ Documento ignorato per dati mancanti: ${doc.id}")
                    null
                }
            }

            val db = FirebaseFirestore.getInstance()
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

            val filteredTravelIds = bookedListFromDb.mapNotNull { booked ->
                try {
                    Log.d("BookedTravelRepo", "🔍 Controllo travelId: ${booked.travelId}")
                    val travelDoc = db.collection("travels")
                        .document(booked.travelId.toString())
                        .get()
                        .await()

                    val endDateStr = travelDoc.getString("endDate")
                    Log.d("BookedTravelRepo", "📅 endDate stringa: $endDateStr")

                    val endDate = endDateStr?.let { LocalDate.parse(it, dateFormatter) }

                    if (endDate != null && (endDate.isEqual(today) || endDate.isAfter(today))) {
                        Log.d("BookedTravelRepo", "✅ Viaggio attuale/futuro: travelId=${booked.travelId}, endDate=$endDate")
                        booked.travelId
                    } else {
                        Log.d("BookedTravelRepo", "⏳ Viaggio passato: travelId=${booked.travelId}, endDate=$endDate")
                        null
                    }

                } catch (e: Exception) {
                    Log.e("BookedTravelRepo", "❌ Errore durante il controllo di endDate per travelId=${booked.travelId}", e)
                    null
                }
            }

            Log.d("BookedTravelRepo", "🎯 Booked travelIds filtrati (endDate >= oggi): ${filteredTravelIds.size}")
            filteredTravelIds

        } catch (e: Exception) {
            Log.e("BookedTravelRepo", "❌ Errore generale nel recupero dei booked travels", e)
            emptyList()
        }
    }


}


class BookedViewModel : ViewModel() {

    val bookedList: StateFlow<List<BookedTravel>> = BookedRepository.bookedList


    fun addBooked(request: TripRequest) {
        viewModelScope.launch {
            BookedRepository.addBooked(request)
        }
    }

    fun deleteBooked(userId: Int,travelId: Int) {
        viewModelScope.launch {
            BookedRepository.deleteBookedByTravelId(userId,travelId)
        }
    }

    suspend fun getUserBookedTravelIds(userId: Int): List<Int> {
        return BookedRepository.getBookedTravelIdsByUser(userId)
    }
}

