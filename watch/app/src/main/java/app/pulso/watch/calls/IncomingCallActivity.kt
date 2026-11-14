package app.pulso.watch.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pulso.watch.PulsoGraph
import app.pulso.watch.ui.theme.PulsoTheme

class IncomingCallActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setShowWhenLocked(true)
    setTurnScreenOn(true)
    setContent {
      val state by PulsoGraph.callStateMachine.state.collectAsStateWithLifecycle()
      LaunchedEffect(state) { if (state == CallState.Idle) finish() }
      PulsoTheme {
        CallScreen(state, onAnswer = CallActions::answer, onDecline = CallActions::decline)
      }
    }
  }
}
