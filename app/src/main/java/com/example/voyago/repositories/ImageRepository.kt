package com.example.voyago.repositories

import android.content.Context
import android.net.Uri
import com.example.voyago.utils.SupabaseManager
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object ImageRepository {

  enum class Bucket(val bucketName: String) {
    TRAVEL("travel-images"),
    PROFILE("profile-images")
  }

  private val storage: Storage
    get() = SupabaseManager.client.storage

  suspend fun uploadImage(
    context: Context,
    imageUri: Uri,
    bucket: Bucket
  ): String? = withContext(Dispatchers.IO) {
    try {
      println("ImageRepository: imageUri = $imageUri")
      val inputStream = context.contentResolver.openInputStream(imageUri)
      if (inputStream == null) {
        println("ImageRepository: inputStream is null")
        return@withContext null
      }
      val bytes = inputStream.use { it.readBytes() }
      println("ImageRepository: read ${bytes.size} bytes")

      val mimeType = context.contentResolver.getType(imageUri)
      println("ImageRepository: mimeType = $mimeType")

      val fileName = "${UUID.randomUUID()}." +
              (mimeType?.substringAfterLast("/") ?: "jpg")

      println("ImageRepository: fileName = $fileName")

      val result = storage.from(bucket.bucketName).upload(
        path = fileName,
        data = bytes,
        upsert = bucket != Bucket.TRAVEL
      )
      println("ImageRepository: upload result = $result")

      val url = storage.from(bucket.bucketName).publicUrl(fileName)
      println("ImageRepository: publicUrl = $url")

      url
    } catch (e: Exception) {
      e.printStackTrace()
      println("ImageRepository: Upload failed with error: ${e.localizedMessage}")
      null
    }
  }


  suspend fun deleteImage(
    imageUrl: String,
    bucket: Bucket
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val fileName = imageUrl.substringAfterLast("/")
      storage.from(bucket.bucketName).delete(fileName)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}
