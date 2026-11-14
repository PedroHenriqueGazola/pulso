package app.pulso.watch

import app.pulso.watch.ams.AmsSession
import app.pulso.watch.ancs.AncsSession
import kotlinx.coroutines.flow.MutableStateFlow

object PulsoSessions {
  val ancsSession = MutableStateFlow<AncsSession?>(null)
  val amsSession = MutableStateFlow<AmsSession?>(null)
}
