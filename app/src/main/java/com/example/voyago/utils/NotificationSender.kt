import android.util.Log
import com.example.voyago.GLOBAL_NOTIFICATION_USER_ID
import com.example.voyago.NotificationType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NotificationSender {
    private const val TAG = "NotificationSender"
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendNotification(
        type: NotificationType,
        targetUserId: String = GLOBAL_NOTIFICATION_USER_ID,
        travelId: Int? = null,
        title: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        try {
            val data = mutableMapOf(
                "type" to type.toString(),
                "title" to title,
                "body" to body,
                "target_user_id" to targetUserId
            )

            travelId?.let { data["travel_id"] = it.toString() }

            if (targetUserId == GLOBAL_NOTIFICATION_USER_ID) {
                val tokens = getAllUserTokens()
                tokens.forEach { token ->
                    sendToToken(token, data)
                }
                Log.d(TAG, "🔔 Global notification sent to ${tokens.size} users")
            } else {
                try {
                    val token = getUserToken(targetUserId)
                    sendToToken(token, data)
                    Log.d(TAG, "✅ Notification sent to user $targetUserId")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to send notification to $targetUserId", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending notification", e)
            throw e
        }
    }

    private suspend fun getUserToken(userId: String): String {
        val intId = userId.toIntOrNull()
            ?: throw IllegalArgumentException("userId must be a numeric string")

        val snapshot = db.collection("profiles")
            .whereEqualTo("id", intId)
            .limit(1)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull()
            ?: throw Exception("User with id $userId not found")

        return doc.getString("token")
            ?: throw Exception("Token not found for user $userId")
    }

    private suspend fun getAllUserTokens(): List<String> {
        val snapshot = db.collection("profiles").get().await()
        return snapshot.documents.mapNotNull { it.getString("token") }
    }

    private fun sendToToken(token: String, data: Map<String, String>) {
        val payload = HashMap(data)
        payload["token"] = token

        println(payload)

        Firebase.functions("europe-west1")
            .getHttpsCallable("sendNotificationV2")
            .call(payload)
            .addOnSuccessListener { result ->
                Log.d("FCM", "✅ Success: ${result.data}")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ Error: ${e.message}", e)
            }
    }
}

