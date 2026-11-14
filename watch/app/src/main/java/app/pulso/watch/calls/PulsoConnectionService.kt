package app.pulso.watch.calls

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle

class PulsoConnectionService : ConnectionService() {
  override fun onCreateIncomingConnection(
    connectionManagerPhoneAccount: PhoneAccountHandle?,
    request: ConnectionRequest,
  ): Connection {
    val uid = request.extras.getLong(EXTRA_CALL_UID)
    val connection = IphoneCallConnection(uid)
    currentConnection = connection
    return connection
  }

  companion object {
    const val EXTRA_CALL_UID = "call_uid"

    @Volatile var currentConnection: IphoneCallConnection? = null
  }
}
