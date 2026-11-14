package app.pulso.watch.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import app.pulso.watch.service.PulsoService
import app.pulso.watch.ui.theme.PulsoTheme

class MainActivity : ComponentActivity() {
  private val statusViewModel: StatusViewModel by viewModels()

  private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    startServiceWhenBluetoothIsAllowed()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionsLauncher.launch(REQUIRED_PERMISSIONS)
    setContent {
      PulsoTheme {
        StatusScreen(statusViewModel)
      }
    }
  }

  private fun startServiceWhenBluetoothIsAllowed() {
    val isBluetoothAllowed =
      checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    if (!isBluetoothAllowed) return
    PulsoService.start(this)
  }

  private companion object {
    val REQUIRED_PERMISSIONS = arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.POST_NOTIFICATIONS,
    )
  }
}
