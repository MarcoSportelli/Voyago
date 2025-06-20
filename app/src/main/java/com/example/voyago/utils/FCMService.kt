package com.example.voyago.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.voyago.BuildConfig
import com.example.voyago.GLOBAL_NOTIFICATION_USER_ID
import com.example.voyago.Notification
import com.example.voyago.NotificationType
import com.example.voyago.R
import com.example.voyago.viewModels.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.voyago.MainActivity

class FCMService : FirebaseMessagingService() {

    private val GLOBAL_TOPIC = "global_notifications"

    override fun onCreate() {
        super.onCreate()
        Log.d("FCM", "🔧 Service created - subscribing to topic $GLOBAL_TOPIC")
        FirebaseMessaging.getInstance().subscribeToTopic(GLOBAL_TOPIC)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "📥 Message received from: ${message.from}")
        Log.d("FCM", "📦 Message data: ${message.data}")
        Log.d("FCM", "📦 Message notification: ${message.notification?.title} - ${message.notification?.body}")

        val data = message.data
        val typeString = data["type"]

        if (typeString == null) {
            Log.w("FCM", "❌ Missing 'type' in message")
            return
        }

        val type = NotificationType.valueOf(typeString)

        if (type == NotificationType.Message) {
            handleMessageNotification(data)
            return
        }

        if (message.from?.contains(GLOBAL_TOPIC) == true) {
            Log.d("FCM", "🔔 Handling global notification")
            handleGlobalNotification(message)
        } else {
            Log.d("FCM", "👤 Handling user-specific notification")
            handleUserNotification(message)
        }
    }

    private fun handleMessageNotification(data: Map<String, String>) {
        Log.d("FCM", "💬 handleMessageNotification")

        if (isAppInForeground()) {
            Log.d("FCM", "ℹ️ App in foreground, non mostro la notifica per Message")
            // Qui potresti magari inviare un evento interno per aggiornare UI, ecc.
            return
        }

        val title = data["title"] ?: "Nuovo messaggio"
        val body = data["body"] ?: "Hai ricevuto un nuovo messaggio"
        val userId = data["target_user_id"] ?: return  // Se manca userId, esco

        val notification = createNotification(
            type = NotificationType.Message,
            title = title,
            body = body,
            travelId = null,
            userId = userId
        )
        processNotification(notification)
    }


    private fun handleUserNotification(message: RemoteMessage) {
        Log.d("FCM", "➡️ handleUserNotification")
        val data = message.data
        val typeString = data["type"] ?: run {
            Log.w("FCM", "❌ Missing 'type' in user notification")
            return
        }
        val type = NotificationType.valueOf(typeString)
        val travelId = data["travel_id"]?.toIntOrNull()
        val targetUserId = data["target_user_id"] ?: run {
            Log.w("FCM", "❌ Missing 'target_user_id'")
            return
        }

        when (type) {
            NotificationType.RequestSend -> {
                val accepted = data["status"] == "accepted"
                Log.d("FCM", "📬 RequestSend received - accepted: $accepted")
                handleRequestResponse(travelId, accepted, targetUserId, data)
            }
            NotificationType.RequestReceived -> {
                Log.d("FCM", "📬 RequestReceived for travelId=$travelId")
                handleNewRequest(travelId, targetUserId, data)
            }
            NotificationType.ReviewReceived -> {
                Log.d("FCM", "📬 ReviewReceived for travelId=$travelId")
                handleNewReview(travelId, targetUserId, data)
            }
            NotificationType.LastMinute -> {
                Log.d("FCM", "🔥 LastMinute global notification")
                handleLastMinute(travelId, data)
            }
            NotificationType.Recommended -> {
                Log.d("FCM", "⭐ Recommended global notification")
                handleRecommended(travelId, data)
            }
            else -> {
                Log.w("FCM", "❌ Unexpected global type in user handler: $type")
            }
        }
    }
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.processName == packageName) {
                return appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return false
    }

    private fun handleGlobalNotification(message: RemoteMessage) {
        Log.d("FCM", "➡️ handleGlobalNotification")
        val data = message.data
        val typeString = data["type"] ?: run {
            Log.w("FCM", "❌ Missing 'type' in global notification")
            return
        }
        val type = NotificationType.valueOf(typeString)
        val travelId = data["travel_id"]?.toIntOrNull()

        when (type) {
            NotificationType.LastMinute -> {
                Log.d("FCM", "🔥 LastMinute global notification")
                handleLastMinute(travelId, data)
            }
            NotificationType.Recommended -> {
                Log.d("FCM", "⭐ Recommended global notification")
                handleRecommended(travelId, data)
            }
            else -> {
                Log.w("FCM", "❌ Unexpected user-specific type in global handler: $type")
            }
        }
    }

    private fun handleLastMinute(travelId: Int?, data: Map<String, String>) {
        Log.d("FCM", "⏱ handleLastMinute travelId=$travelId")
        val title = data["title"] ?: "Nuova proposta Last Minute!"
        val body = data["body"] ?: "Posti disponibili per il viaggio ${travelId ?: ""}"
        val notification = createNotification(
            type = NotificationType.LastMinute,
            title = title,
            body = body,
            travelId = travelId,
            userId = GLOBAL_NOTIFICATION_USER_ID
        )
        processNotification(notification)
    }

    private fun handleRecommended(travelId: Int?, data: Map<String, String>) {
        Log.d("FCM", "💡 handleRecommended travelId=$travelId")
        val title = data["title"] ?: "Viaggio consigliato!"
        val body = data["body"] ?: "Dai un'occhiata a questo viaggio ${travelId ?: ""}"
        val notification = createNotification(
            type = NotificationType.Recommended,
            title = title,
            body = body,
            travelId = travelId,
            userId = GLOBAL_NOTIFICATION_USER_ID
        )
        processNotification(notification)
    }

    private fun handleRequestResponse(travelId: Int?, accepted: Boolean, targetUserId: String?, data: Map<String, String>) {
        Log.d("FCM", "📨 handleRequestResponse travelId=$travelId, accepted=$accepted, userId=$targetUserId")
        targetUserId?.let {
            val status = if (accepted) "accettata" else "rifiutata"
            val title = data["title"] ?: "Richiesta $status"
            val body = data["body"] ?: "La tua richiesta per il viaggio ${travelId ?: ""} è stata $status"
            val notification = createNotification(
                type = NotificationType.RequestSend,
                title = title,
                body = body,
                travelId = travelId,
                userId = it
            )
            processNotification(notification)
        }
    }

    private fun handleNewRequest(travelId: Int?, targetUserId: String?, data: Map<String, String>) {
        Log.d("FCM", "📩 handleNewRequest travelId=$travelId, userId=$targetUserId")
        targetUserId?.let {
            val title = data["title"] ?: "Nuova richiesta"
            val body = data["body"] ?: "Hai una nuova richiesta per il viaggio ${travelId ?: ""}"
            val notification = createNotification(
                type = NotificationType.RequestReceived,
                title = title,
                body = body,
                travelId = travelId,
                userId = it
            )
            processNotification(notification)
        }
    }

    private fun handleNewReview(travelId: Int?, targetUserId: String?, data: Map<String, String>) {
        Log.d("FCM", "📝 handleNewReview travelId=$travelId, userId=$targetUserId")
        targetUserId?.let {
            val title = data["title"] ?: "Nuova recensione"
            val body = data["body"] ?: "Hai ricevuto una nuova recensione!"
            val notification = createNotification(
                type = NotificationType.ReviewReceived,
                title = title,
                body = body,
                travelId = travelId,
                userId = it
            )
            processNotification(notification)
        }
    }

    private fun createNotification(
        type: NotificationType,
        title: String,
        body: String,
        travelId: Int?,
        userId: String
    ): Notification {
        Log.d("FCM", "🛠 createNotification: $type, $title")
        return Notification(
            userId = userId,
            travelId = travelId,
            title = title,
            body = body,
            type = type,
            read = false,
            timestamp = Timestamp.now()
        )
    }

    private fun processNotification(notification: Notification) {
        Log.d("FCM", "📤 processNotification: ${notification.title}")

        showNotification(notification)

    }

    private fun showNotification(notification: Notification) {
        Log.d("FCM", "📱 showNotification: ${notification.title}")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = NotificationHelper.createChannel(this)

        NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            .also { notif ->
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(notification.hashCode(), notif)
                Log.d("FCM", "🔔 Notifica mostrata all'utente")
            }
    }

    private fun createIntent(deepLink: String): Intent {
        Log.d("FCM", "🔗 createIntent: $deepLink")
        return Intent(Intent.ACTION_VIEW, deepLink.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "🔁 New FCM token generated: $token")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("FCM", "❌ User not logged in, skipping token update")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("profiles")
            .document(userId)
            .update("token", token)
            .addOnSuccessListener {
                Log.d("FCM", "✅ Token aggiornato per user $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ Errore aggiornamento token", e)
            }
    }
}
