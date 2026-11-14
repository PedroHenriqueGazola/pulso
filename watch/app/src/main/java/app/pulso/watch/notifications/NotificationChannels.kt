package app.pulso.watch.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.provider.Settings

object NotificationChannels {
  const val ALERTS = "alerts_v1"
  const val SILENT = "silent_v1"
  const val SERVICE = "service_v1"
  const val CALLS = "calls_v1"

  private val NOTIFICATION_AUDIO = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()

  fun register(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannels(
      listOf(
        buildAlertChannel(ALERTS, "Notificações do iPhone", NotificationManager.IMPORTANCE_HIGH),
        buildAlertChannel(CALLS, "Ligações do iPhone", NotificationManager.IMPORTANCE_HIGH),
        NotificationChannel(SILENT, "Notificações silenciosas (Foco)", NotificationManager.IMPORTANCE_LOW),
        NotificationChannel(SERVICE, "Conexão com o iPhone", NotificationManager.IMPORTANCE_MIN),
      ),
    )
  }

  private fun buildAlertChannel(id: String, name: String, importance: Int) =
    NotificationChannel(id, name, importance).apply {
      enableVibration(true)
      setSound(Settings.System.DEFAULT_NOTIFICATION_URI, NOTIFICATION_AUDIO)
    }
}
