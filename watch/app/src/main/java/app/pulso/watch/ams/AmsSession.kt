package app.pulso.watch.ams

import app.pulso.watch.bluetooth.GattClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AmsSession(
  private val gattClient: GattClient,
  private val scope: CoroutineScope,
) {
  private val mutableState = MutableStateFlow(MediaState())

  val state: StateFlow<MediaState> = mutableState

  suspend fun start() {
    if (!gattClient.hasService(AmsUuids.SERVICE)) return

    scope.launch(start = CoroutineStart.UNDISPATCHED) { collectEntityUpdates() }
    gattClient.enableNotifications(AmsUuids.SERVICE, AmsUuids.ENTITY_UPDATE)
    gattClient.write(AmsUuids.SERVICE, AmsUuids.ENTITY_UPDATE, PLAYER_ATTRIBUTES_REGISTRATION)
    gattClient.write(AmsUuids.SERVICE, AmsUuids.ENTITY_UPDATE, TRACK_ATTRIBUTES_REGISTRATION)
  }

  suspend fun send(command: MediaCommand) {
    gattClient.write(AmsUuids.SERVICE, AmsUuids.REMOTE_COMMAND, byteArrayOf(command.id.toByte()))
  }

  private suspend fun collectEntityUpdates() {
    gattClient.characteristicChanges
      .filter { it.characteristic == AmsUuids.ENTITY_UPDATE }
      .mapNotNull { EntityUpdateParser.parse(it.value) }
      .collect { entityUpdate -> mutableState.update { it.apply(entityUpdate) } }
  }

  private companion object {
    val PLAYER_ATTRIBUTES_REGISTRATION = byteArrayOf(0, 0, 1, 2)
    val TRACK_ATTRIBUTES_REGISTRATION = byteArrayOf(2, 0, 2)
  }
}
