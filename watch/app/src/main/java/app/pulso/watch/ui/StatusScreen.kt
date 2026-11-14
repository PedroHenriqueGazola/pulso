package app.pulso.watch.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import app.pulso.watch.bluetooth.ConnectionState
import app.pulso.watch.bluetooth.ScannedIphone

@Composable
fun StatusScreen(viewModel: StatusViewModel) {
  val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
  val scannedIphones by viewModel.scannedIphones.collectAsStateWithLifecycle()

  ScalingLazyColumn(modifier = Modifier.fillMaxWidth()) {
    item { Text("Pulso", style = MaterialTheme.typography.titleMedium) }
    item { Text(describe(connectionState), textAlign = TextAlign.Center) }
    connectionActions(connectionState, scannedIphones, viewModel)
  }
}

private fun ScalingLazyListScope.connectionActions(
  connectionState: ConnectionState,
  scannedIphones: List<ScannedIphone>,
  viewModel: StatusViewModel,
) {
  when (connectionState) {
    ConnectionState.NotPaired -> item { ActionButton("Parear iPhone", viewModel::startScan) }
    ConnectionState.Scanning -> scanResults(scannedIphones, viewModel)
    is ConnectionState.Connecting,
    is ConnectionState.Connected,
    is ConnectionState.WaitingForIphone -> item { ActionButton("Esquecer iPhone", viewModel::forget) }
  }
}

private fun ScalingLazyListScope.scanResults(scannedIphones: List<ScannedIphone>, viewModel: StatusViewModel) {
  items(scannedIphones, key = { it.address }) { iphone ->
    ActionButton(iphone.name) { viewModel.pair(iphone) }
  }
  item { ActionButton("Procurar de novo", viewModel::startScan) }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
  Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

private fun describe(state: ConnectionState): String = when (state) {
  ConnectionState.NotPaired -> "Nenhum iPhone pareado"
  ConnectionState.Scanning -> "Procurando… deixe o iPhone desbloqueado e perto"
  is ConnectionState.Connecting -> "Conectando a ${state.iphoneName}…"
  is ConnectionState.Connected -> "Conectado a ${state.iphoneName}"
  is ConnectionState.WaitingForIphone -> "Aguardando ${state.iphoneName}"
}
