package app.pulso.watch.calls

import android.telecom.Connection
import android.telecom.DisconnectCause

class IphoneCallConnection(private val uid: Long) : Connection() {
  init {
    connectionProperties = PROPERTY_SELF_MANAGED
    setRinging()
  }

  override fun onAnswer() {
    CallActions.answer(uid)
  }

  override fun onReject() {
    CallActions.decline(uid)
    end(DisconnectCause.REJECTED)
  }

  override fun onDisconnect() {
    end(DisconnectCause.LOCAL)
  }

  fun end(cause: Int) {
    setDisconnected(DisconnectCause(cause))
    destroy()
  }
}
