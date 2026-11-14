package app.pulso.watch.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import app.pulso.watch.ams.MediaCommand
import app.pulso.watch.ams.MediaState

@Composable
fun MediaScreen(viewModel: MediaViewModel) {
  val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    if (!mediaState.hasActivePlayer) {
      Text("Nada tocando no iPhone", textAlign = TextAlign.Center)
      return@Column
    }
    TrackInfo(mediaState)
    PlaybackControls(mediaState, viewModel::send)
    VolumeControls(viewModel::send)
  }
}

@Composable
private fun TrackInfo(mediaState: MediaState) {
  Text(mediaState.playerName, style = MaterialTheme.typography.labelSmall)
  Text(
    mediaState.title.ifEmpty { "—" },
    style = MaterialTheme.typography.titleSmall,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
  Text(mediaState.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun PlaybackControls(mediaState: MediaState, onCommand: (MediaCommand) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    CompactButton(onClick = { onCommand(MediaCommand.PREVIOUS_TRACK) }) { Text("⏮") }
    CompactButton(onClick = { onCommand(MediaCommand.TOGGLE_PLAY_PAUSE) }) { Text(playPauseLabel(mediaState)) }
    CompactButton(onClick = { onCommand(MediaCommand.NEXT_TRACK) }) { Text("⏭") }
  }
}

@Composable
private fun VolumeControls(onCommand: (MediaCommand) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    CompactButton(onClick = { onCommand(MediaCommand.VOLUME_DOWN) }) { Text("−") }
    CompactButton(onClick = { onCommand(MediaCommand.VOLUME_UP) }) { Text("+") }
  }
}

private fun playPauseLabel(mediaState: MediaState): String {
  if (mediaState.isPlaying) return "⏸"
  return "▶"
}
