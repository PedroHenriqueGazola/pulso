package app.pulso.watch

import android.app.Application

class PulsoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    PulsoGraph.initialize(this)
  }
}
