package app.pulso.watch.media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pulso.watch.PulsoSessions
import app.pulso.watch.ams.MediaCommand
import app.pulso.watch.ams.MediaState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModel : ViewModel() {
  val mediaState: StateFlow<MediaState> = PulsoSessions.amsSession
    .flatMapLatest { session -> session?.state ?: flowOf(MediaState()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MediaState())

  fun send(command: MediaCommand) {
    viewModelScope.launch {
      runCatching { PulsoSessions.amsSession.value?.send(command) }
        .onFailure { error -> Log.w("Pulso", "Falha ao enviar comando $command", error) }
    }
  }
}
