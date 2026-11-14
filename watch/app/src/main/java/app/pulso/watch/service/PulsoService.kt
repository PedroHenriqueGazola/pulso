package app.pulso.watch.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import app.pulso.watch.PulsoGraph
import app.pulso.watch.PulsoSessions
import app.pulso.watch.ams.AmsSession
import app.pulso.watch.ancs.AncsSession
import app.pulso.watch.bluetooth.ConnectionState
import app.pulso.watch.calls.CallPresenter
import app.pulso.watch.notifications.NotificationChannels
import app.pulso.watch.notifications.WatchNotificationPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class PulsoService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private lateinit var router: IphoneUpdateRouter

  override fun onCreate() {
    super.onCreate()
    NotificationChannels.register(this)
    val presenter = WatchNotificationPresenter(this)
    router = IphoneUpdateRouter(presenter, PulsoGraph.callStateMachine)
    val callPresenter = CallPresenter(this)
    startForeground(SERVICE_NOTIFICATION_ID, buildServiceNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
    serviceScope.launch { reconnectToPairedIphone() }
    serviceScope.launch { PulsoGraph.callStateMachine.state.collect(callPresenter::render) }
    serviceScope.launch { PulsoGraph.iphoneConnection.state.collectLatest(::runSessionsWhileConnected) }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  private suspend fun reconnectToPairedIphone() {
    val pairedAddress = PulsoGraph.pairedIphoneStore.pairedAddress.first() ?: return
    if (PulsoGraph.iphoneConnection.state.value != ConnectionState.NotPaired) return
    PulsoGraph.iphoneConnection.connect(pairedAddress)
  }

  private suspend fun runSessionsWhileConnected(state: ConnectionState) {
    if (state !is ConnectionState.Connected) return
    try {
      coroutineScope { startSessions(state, this) }
    } catch (error: Exception) {
      Log.w(TAG, "Sessão com o iPhone encerrada", error)
    } finally {
      PulsoSessions.ancsSession.value = null
      PulsoSessions.amsSession.value = null
    }
  }

  private suspend fun startSessions(state: ConnectionState.Connected, sessionScope: CoroutineScope) {
    val ancsSession = AncsSession(state.gattClient, sessionScope)
    val amsSession = AmsSession(state.gattClient, sessionScope)
    sessionScope.launch { ancsSession.updates.collect(router::route) }
    ancsSession.start()
    amsSession.start()
    PulsoSessions.ancsSession.value = ancsSession
    PulsoSessions.amsSession.value = amsSession
    Log.i(TAG, "Sessões ANCS e AMS ativas")
    awaitCancellation()
  }

  private fun buildServiceNotification() = NotificationCompat.Builder(this, NotificationChannels.SERVICE)
    .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
    .setContentTitle("Pulso")
    .setContentText("Conectado ao iPhone")
    .setOngoing(true)
    .build()

  companion object {
    private const val TAG = "Pulso"
    private const val SERVICE_NOTIFICATION_ID = 1

    fun start(context: Context) {
      context.startForegroundService(Intent(context, PulsoService::class.java))
    }
  }
}
