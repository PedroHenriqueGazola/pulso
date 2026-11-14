package app.pulso.watch.ancs

sealed interface AncsUpdate {
  data class NotificationPosted(val notification: IphoneNotification) : AncsUpdate

  data class NotificationChanged(val notification: IphoneNotification) : AncsUpdate

  data class NotificationRemoved(val uid: Long) : AncsUpdate
}
