package app.pulso.watch.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.pulso.watch.PulsoGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    val pendingResult = goAsync()
    PulsoGraph.applicationScope.launch {
      val hasPairedIphone = PulsoGraph.pairedIphoneStore.pairedAddress.first() != null
      if (hasPairedIphone) PulsoService.start(context)
      pendingResult.finish()
    }
  }
}
