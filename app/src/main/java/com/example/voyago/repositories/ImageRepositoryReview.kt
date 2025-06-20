package com.example.voyago.repositories

import android.content.Context
import android.net.Uri
import com.example.voyago.utils.SupabaseManager
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object ImageRepositoryReview {
  private const val BUCKET_NAME = "review-images"

  private val storage: Storage
    get() = SupabaseManager.client.storage

  suspend fun uploadImage(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
    try {
      // Converti l'Uri in ByteArray
      val inputStream = context.contentResolver.openInputStream(imageUri)
      val bytes = inputStream?.use { it.readBytes() }
        ?: return@withContext null

      // Genera un nome univoco per il file
      val fileName = "${UUID.randomUUID()}.${context.contentResolver.getType(imageUri)?.substringAfterLast("/") ?: "jpg"}"

      // Carica l'immagine
      storage.from(BUCKET_NAME).upload(
        path = fileName,
        data = bytes,
        upsert = false
      )

      // Ottieni l'URL pubblico dell'immagine
      storage.from(BUCKET_NAME).publicUrl(fileName)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  suspend fun deleteImage(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val fileName = imageUrl.substringAfterLast("/")
      storage.from(BUCKET_NAME).delete(fileName)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}