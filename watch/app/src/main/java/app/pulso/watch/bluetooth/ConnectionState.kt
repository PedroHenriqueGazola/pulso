package app.pulso.watch.bluetooth

sealed interface ConnectionState {
  data object NotPaired : ConnectionState

  data object Scanning : ConnectionState

  data class Connecting(val iphoneName: String) : ConnectionState

  data class Connected(val iphoneName: String, val gattClient: GattClient) : ConnectionState

  data class WaitingForIphone(val iphoneName: String) : ConnectionState
}
