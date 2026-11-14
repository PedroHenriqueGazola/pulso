package app.pulso.watch.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pulso.watch.PulsoGraph
import app.pulso.watch.bluetooth.ConnectionState
import app.pulso.watch.bluetooth.ScannedIphone
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class StatusViewModel : ViewModel() {
  private val connection = PulsoGraph.iphoneConnection
  private val mutableScannedIphones = MutableStateFlow<List<ScannedIphone>>(emptyList())
  private var scanJob: Job? = null

  val connectionState: StateFlow<ConnectionState> = connection.state
  val scannedIphones: StateFlow<List<ScannedIphone>> = mutableScannedIphones

  fun startScan() {
    scanJob?.cancel()
    mutableScannedIphones.value = emptyList()
    connection.markScanning()
    scanJob = viewModelScope.launch {
      withTimeoutOrNull(SCAN_DURATION_MILLIS) {
        PulsoGraph.iphoneScanner.scan()
          .catch { error -> Log.w(TAG, "Scan interrompido", error) }
          .collect(::addScannedIphone)
      }
    }
  }

  fun pair(iphone: ScannedIphone) {
    scanJob?.cancel()
    connection.connect(iphone.address)
  }

  fun forget() {
    connection.forget()
  }

  private fun addScannedIphone(iphone: ScannedIphone) {
    mutableScannedIphones.update { current ->
      (current.filterNot { it.address == iphone.address } + iphone).sortedByDescending { it.signalStrength }
    }
  }

  private companion object {
    const val TAG = "Pulso"
    const val SCAN_DURATION_MILLIS = 20_000L
  }
}
