package app.pulso.watch.calls

sealed interface CallState {
  data object Idle : CallState

  data class Ringing(
    val uid: Long,
    val callerName: String,
    val appLabel: String,
    val canAnswer: Boolean,
    val canDecline: Boolean,
  ) : CallState

  data class ActiveOnIphone(val uid: Long, val callerName: String) : CallState
}
