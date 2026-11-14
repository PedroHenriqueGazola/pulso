package app.pulso.watch.calls

import app.pulso.watch.ancs.IphoneNotification
import app.pulso.watch.notifications.AppLabels
import app.pulso.watch.service.CallHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CallStateMachine : CallHandler {
  private val mutableState = MutableStateFlow<CallState>(CallState.Idle)

  val state: StateFlow<CallState> = mutableState

  override fun onCallPosted(notification: IphoneNotification) {
    mutableState.value = buildRinging(notification)
  }

  override fun onCallChanged(notification: IphoneNotification) {
    val current = mutableState.value
    if (current is CallState.Ringing && current.uid == notification.uid) {
      mutableState.value = CallState.ActiveOnIphone(notification.uid, current.callerName)
      return
    }
    if (current is CallState.ActiveOnIphone && current.uid == notification.uid) return
    mutableState.value = buildRinging(notification)
  }

  override fun onRemoved(uid: Long) {
    if (currentUid() != uid) return
    mutableState.value = CallState.Idle
  }

  fun onDeclinedOnWatch() {
    mutableState.value = CallState.Idle
  }

  private fun currentUid(): Long? = when (val current = mutableState.value) {
    CallState.Idle -> null
    is CallState.Ringing -> current.uid
    is CallState.ActiveOnIphone -> current.uid
  }

  private fun buildRinging(notification: IphoneNotification) = CallState.Ringing(
    uid = notification.uid,
    callerName = notification.title.ifEmpty { notification.subtitle.ifEmpty { "Desconhecido" } },
    appLabel = AppLabels.labelFor(notification.appIdentifier),
    canAnswer = notification.hasPositiveAction,
    canDecline = notification.hasNegativeAction,
  )
}
