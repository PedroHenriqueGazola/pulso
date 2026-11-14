package app.pulso.watch.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class ScannedIphone(val address: String, val name: String, val signalStrength: Int)

class IphoneScanner(private val context: Context) {
  @SuppressLint("MissingPermission")
  fun scan(): Flow<ScannedIphone> = callbackFlow {
    val scanner = context.getSystemService(BluetoothManager::class.java).adapter?.bluetoothLeScanner
    if (scanner == null) {
      close(IllegalStateException("Bluetooth desligado no relógio"))
      return@callbackFlow
    }
    val callback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, result: ScanResult) {
        trySend(toScannedIphone(result))
      }

      override fun onScanFailed(errorCode: Int) {
        close(IllegalStateException("Falha no scan Bluetooth: $errorCode"))
      }
    }
    val appleDevicesFilter = ScanFilter.Builder().setManufacturerData(APPLE_COMPANY_ID, byteArrayOf()).build()
    val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
    scanner.startScan(listOf(appleDevicesFilter), settings, callback)
    awaitClose { scanner.stopScan(callback) }
  }

  @SuppressLint("MissingPermission")
  private fun toScannedIphone(result: ScanResult) = ScannedIphone(
    address = result.device.address,
    name = result.device.name ?: result.scanRecord?.deviceName ?: "Aparelho Apple",
    signalStrength = result.rssi,
  )

  private companion object {
    const val APPLE_COMPANY_ID = 0x004C
  }
}
