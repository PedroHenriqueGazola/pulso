package app.pulso.watch

import android.content.Context
import app.pulso.watch.bluetooth.IphoneConnection
import app.pulso.watch.bluetooth.IphoneScanner
import app.pulso.watch.bluetooth.PairedIphoneStore
import app.pulso.watch.notifications.WatchNotificationPresenter
import app.pulso.watch.service.CallHandler
import app.pulso.watch.service.CallsAsNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object PulsoGraph {
  lateinit var applicationScope: CoroutineScope
    private set
  lateinit var pairedIphoneStore: PairedIphoneStore
    private set
  lateinit var iphoneConnection: IphoneConnection
    private set
  lateinit var iphoneScanner: IphoneScanner
    private set
  lateinit var callHandler: CallHandler
    private set

  fun initialize(context: Context) {
    applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    pairedIphoneStore = PairedIphoneStore(context)
    iphoneConnection = IphoneConnection(context, pairedIphoneStore, applicationScope)
    iphoneScanner = IphoneScanner(context)
    callHandler = CallsAsNotifications(WatchNotificationPresenter(context))
  }
}
