package app.pulso.watch.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.pulso.watch.PulsoGraph
import app.pulso.watch.PulsoSessions
import app.pulso.watch.ancs.NotificationAction
import kotlinx.coroutines.launch

class DismissActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val uid = intent.getLongExtra(EXTRA_NOTIFICATION_UID, MISSING_UID)
    if (uid == MISSING_UID) return
    WatchNotificationPresenter(context).remove(uid)
    val pendingResult = goAsync()
    PulsoGraph.applicationScope.launch {
      runCatching { PulsoSessions.ancsSession.value?.performAction(uid, NotificationAction.NEGATIVE) }
        .onFailure { error -> Log.w("Pulso", "Não foi possível dispensar no iPhone", error) }
      pendingResult.finish()
    }
  }

  companion object {
    const val EXTRA_NOTIFICATION_UID = "notification_uid"
    private const val MISSING_UID = -1L
  }
}
