package app.pulso.watch.calls

import android.util.Log
import app.pulso.watch.PulsoGraph
import app.pulso.watch.PulsoSessions
import app.pulso.watch.ancs.NotificationAction
import kotlinx.coroutines.launch

object CallActions {
  fun answer(uid: Long) {
    sendAction(uid, NotificationAction.POSITIVE)
  }

  fun decline(uid: Long) {
    PulsoGraph.callStateMachine.onDeclinedOnWatch()
    sendAction(uid, NotificationAction.NEGATIVE)
  }

  private fun sendAction(uid: Long, action: NotificationAction) {
    PulsoGraph.applicationScope.launch {
      runCatching { PulsoSessions.ancsSession.value?.performAction(uid, action) }
        .onFailure { error -> Log.w("Pulso", "Falha ao enviar ação $action da ligação", error) }
    }
  }
}
