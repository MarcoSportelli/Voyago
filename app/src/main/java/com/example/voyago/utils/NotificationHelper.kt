import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    const val CHANNEL_ID = "voyago_channel"

    fun createChannel(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                "Notifiche Voyago",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canale per tutte le notifiche"
                enableVibration(true)
                setShowBadge(true)

                // USIAMO NotificationCompat per massima compatibilità
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }.also { channel ->
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            }
        }
        return CHANNEL_ID
    }
}