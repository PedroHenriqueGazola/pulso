package app.pulso.watch.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import app.pulso.watch.ui.theme.PulsoTheme

class MainActivity : ComponentActivity() {
  private val statusViewModel: StatusViewModel by viewModels()

  private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionsLauncher.launch(REQUIRED_PERMISSIONS)
    setContent {
      PulsoTheme {
        StatusScreen(statusViewModel)
      }
    }
  }

  private companion object {
    val REQUIRED_PERMISSIONS = arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.POST_NOTIFICATIONS,
    )
  }
}
