package app.pulso.watch.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import app.pulso.watch.ancs.IphoneNotification
import app.pulso.watch.service.NotificationDisplay

class WatchNotificationPresenter(private val context: Context) : NotificationDisplay {
  private val notificationManager = context.getSystemService(NotificationManager::class.java)
  private val watchOwner = Person.Builder().setName("Você").build()

  override fun show(notification: IphoneNotification) {
    val appLabel = AppLabels.labelFor(notification.appIdentifier)
    val builder = NotificationCompat.Builder(context, channelFor(notification))
      .setSmallIcon(android.R.drawable.stat_notify_chat)
      .setContentTitle(notification.title.ifEmpty { appLabel })
      .setContentText(notification.message)
      .setSubText(appLabel)
      .setStyle(buildMessagingStyle(notification, appLabel))
      .setGroup("ancs_${notification.uid}")
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
    if (notification.hasNegativeAction) builder.addAction(buildDismissAction(notification))
    notificationManager.notify(notification.uid.toInt(), builder.build())
  }

  override fun remove(uid: Long) {
    notificationManager.cancel(uid.toInt())
  }

  private fun channelFor(notification: IphoneNotification): String {
    if (notification.isSilent) return NotificationChannels.SILENT
    return NotificationChannels.ALERTS
  }

  private fun buildMessagingStyle(notification: IphoneNotification, appLabel: String): NotificationCompat.MessagingStyle {
    val sender = Person.Builder().setName(notification.title.ifEmpty { appLabel }).build()
    val text = listOf(notification.subtitle, notification.message).filter { it.isNotEmpty() }.joinToString("\n")
    return NotificationCompat.MessagingStyle(watchOwner)
      .setConversationTitle(appLabel)
      .addMessage(text, System.currentTimeMillis(), sender)
  }

  private fun buildDismissAction(notification: IphoneNotification): NotificationCompat.Action {
    val intent = Intent(context, DismissActionReceiver::class.java)
      .putExtra(DismissActionReceiver.EXTRA_NOTIFICATION_UID, notification.uid)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      notification.uid.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val label = notification.negativeActionLabel.ifEmpty { "Dispensar" }
    return NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, label, pendingIntent).build()
  }
}
