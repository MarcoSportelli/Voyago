package com.example.voyago.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.PastTravel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.absoluteValue

object PastTravelRepository {

  private val _pastTravelList = MutableStateFlow<List<PastTravel>>(emptyList())
  val pastTravelList: StateFlow<List<PastTravel>> = _pastTravelList.asStateFlow()

  private val db = FirebaseFirestore.getInstance()
  private val collection = db.collection("pastTravel")

  suspend fun fetchPastTravels() {
    try {
      val snapshot = collection.get().await()

      val travels = snapshot.documents.mapNotNull { doc ->
        val id = doc.getLong("id")?.toInt()
        val userId = doc.getLong("userId")?.toInt()
        val travelId = doc.getLong("travelId")?.toInt()

        if (id != null && userId != null && travelId != null) {
          PastTravel(id = id, userId = userId, travelId = travelId)
        } else null
      }

      _pastTravelList.value = travels

    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  suspend fun addPastTravel(userId: Int, tripId: Int) {
    try {
      // Calcolo ID unico per semplificare (opzionale)
      val newId = UUID.randomUUID().hashCode().absoluteValue

      val pastTravel = mapOf(
        "id" to newId,
        "userId" to userId,
        "travelId" to tripId
      )

      collection.add(pastTravel).await()
      fetchPastTravels()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  suspend fun deletePastTravel(userId: Int, tripId: Int) {
    try {
      val snapshot = collection
        .whereEqualTo("userId", userId)
        .whereEqualTo("travelId", tripId)
        .get()
        .await()

      for (doc in snapshot.documents) {
        collection.document(doc.id).delete().await()
      }

      fetchPastTravels()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  suspend fun getPastTravelsByUserId(userId: Int): List<PastTravel> {
    Log.d("PastTravelRepo", "📥 Inizio recupero past travels per userId: $userId")

    return try {
      val snapshot = collection
        .whereEqualTo("userId", userId)
        .get()
        .await()

      Log.d("PastTravelRepo", "📦 Documenti trovati: ${snapshot.size()}")

      val allPastTravels = snapshot.documents.mapNotNull { doc ->
        val id = doc.getLong("id")?.toInt()
        val travelId = doc.getLong("travelId")?.toInt()

        if (id != null && travelId != null) {
          Log.d("PastTravelRepo", "✅ PastTravel valido: id=$id, travelId=$travelId")
          PastTravel(id = id, userId = userId, travelId = travelId)
        } else {
          Log.w("PastTravelRepo", "⚠️ Documento ignorato per dati mancanti: ${doc.id}")
          null
        }
      }

      val db = FirebaseFirestore.getInstance()
      val today = LocalDate.now()
      val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

      val filtered = allPastTravels.filter { pastTravel ->
        try {
          Log.d("PastTravelRepo", "🔍 Controllo travelId: ${pastTravel.travelId}")
          val travelDoc = db.collection("travels")
            .document(pastTravel.travelId.toString())
            .get()
            .await()

          val endDateStr = travelDoc.getString("endDate")
          Log.d("PastTravelRepo", "📅 endDate stringa: $endDateStr")

          val endDate = endDateStr?.let { LocalDate.parse(it, dateFormatter) }

          if (endDate != null && endDate.isBefore(today)) {
            Log.d("PastTravelRepo", "✅ Viaggio passato: travelId=${pastTravel.travelId}, endDate=$endDate")
            true
          } else {
            Log.d("PastTravelRepo", "⏳ Viaggio futuro/scaduto non valido: travelId=${pastTravel.travelId}, endDate=$endDate")
            false
          }

        } catch (e: Exception) {
          Log.e("PastTravelRepo", "❌ Errore durante il controllo di endDate per travelId=${pastTravel.travelId}", e)
          false
        }
      }

      Log.d("PastTravelRepo", "🎯 Past travels filtrati (endDate < oggi): ${filtered.size}")
      filtered

    } catch (e: Exception) {
      Log.e("PastTravelRepo", "❌ Errore generale nel recupero dei past travels", e)
      emptyList()
    }
  }

}

class PastTravelViewModel : ViewModel() {

  val pastTravelList: StateFlow<List<PastTravel>> = PastTravelRepository.pastTravelList

  init {
    viewModelScope.launch {
      PastTravelRepository.fetchPastTravels()
    }
  }

  fun addPastTravel(userId: Int, tripId: Int) {
    viewModelScope.launch {
      PastTravelRepository.addPastTravel(userId, tripId)
    }
  }

  fun deletePastTravel(userId: Int, tripId: Int) {
    viewModelScope.launch {
      PastTravelRepository.deletePastTravel(userId, tripId)
    }
  }

  fun getPastTravelsByUserId(userId: Int) {
    viewModelScope.launch {
      PastTravelRepository.getPastTravelsByUserId(userId)
    }
  }
}
