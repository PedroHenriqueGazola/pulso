package app.pulso.watch.service

import app.pulso.watch.ancs.IphoneNotification

class CallsAsNotifications(private val notificationDisplay: NotificationDisplay) : CallHandler {
  override fun onCallPosted(notification: IphoneNotification) = notificationDisplay.show(notification)

  override fun onCallChanged(notification: IphoneNotification) = notificationDisplay.show(notification)

  override fun onRemoved(uid: Long) = Unit
}
