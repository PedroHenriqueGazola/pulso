package app.pulso.watch.service

import app.pulso.watch.ancs.AncsUpdate
import app.pulso.watch.ancs.IphoneNotification
import app.pulso.watch.calls.CallClassifier

interface NotificationDisplay {
  fun show(notification: IphoneNotification)

  fun remove(uid: Long)
}

interface CallHandler {
  fun onCallPosted(notification: IphoneNotification)

  fun onCallChanged(notification: IphoneNotification)

  fun onRemoved(uid: Long)
}

class IphoneUpdateRouter(
  private val notificationDisplay: NotificationDisplay,
  private val callHandler: CallHandler,
  private val onRouteFailure: (AncsUpdate, Exception) -> Unit = { _, error -> throw error },
) {
  fun route(update: AncsUpdate) {
    try {
      dispatch(update)
    } catch (error: Exception) {
      onRouteFailure(update, error)
    }
  }

  private fun dispatch(update: AncsUpdate) {
    when (update) {
      is AncsUpdate.NotificationPosted -> routePosted(update.notification)
      is AncsUpdate.NotificationChanged -> routeChanged(update.notification)
      is AncsUpdate.NotificationRemoved -> routeRemoved(update.uid)
    }
  }

  private fun routePosted(notification: IphoneNotification) {
    if (CallClassifier.isIncomingCall(notification)) return callHandler.onCallPosted(notification)
    notificationDisplay.show(notification)
  }

  private fun routeChanged(notification: IphoneNotification) {
    if (CallClassifier.isIncomingCall(notification)) return callHandler.onCallChanged(notification)
    notificationDisplay.show(notification)
  }

  private fun routeRemoved(uid: Long) {
    notificationDisplay.remove(uid)
    callHandler.onRemoved(uid)
  }
}
