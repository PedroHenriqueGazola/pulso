package app.pulso.watch.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun CallScreen(state: CallState, onAnswer: (Long) -> Unit, onDecline: (Long) -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (state) {
      is CallState.Ringing -> RingingContent(state, onAnswer, onDecline)
      is CallState.ActiveOnIphone -> CallerText(state.callerName, "Em ligação no iPhone")
      CallState.Idle -> Unit
    }
  }
}

@Composable
private fun RingingContent(state: CallState.Ringing, onAnswer: (Long) -> Unit, onDecline: (Long) -> Unit) {
  CallerText(state.callerName, state.appLabel)
  Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    if (state.canDecline) {
      Button(
        onClick = { onDecline(state.uid) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
      ) { Text("Recusar") }
    }
    if (state.canAnswer) {
      Button(onClick = { onAnswer(state.uid) }) { Text("Atender") }
    }
  }
  if (!state.canAnswer) Text("Atenda no iPhone", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun CallerText(callerName: String, detail: String) {
  Text(callerName, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
  Text(detail, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
}
