package app.pulso.watch.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import app.pulso.watch.media.MediaScreen
import app.pulso.watch.media.MediaViewModel
import app.pulso.watch.service.PulsoService
import app.pulso.watch.ui.theme.PulsoTheme

class MainActivity : ComponentActivity() {
  private val statusViewModel: StatusViewModel by viewModels()
  private val mediaViewModel: MediaViewModel by viewModels()

  private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    startServiceWhenBluetoothIsAllowed()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionsLauncher.launch(REQUIRED_PERMISSIONS)
    setContent {
      PulsoTheme {
        val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
        HorizontalPager(state = pagerState) { page ->
          if (page == STATUS_PAGE) StatusScreen(statusViewModel) else MediaScreen(mediaViewModel)
        }
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
    const val PAGE_COUNT = 2
    const val STATUS_PAGE = 0

    val REQUIRED_PERMISSIONS = arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.POST_NOTIFICATIONS,
    )
  }
}
