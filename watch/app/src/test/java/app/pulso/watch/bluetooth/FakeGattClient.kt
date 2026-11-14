package app.pulso.watch.bluetooth

import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeGattClient(private val services: Set<UUID>) : GattClient {
  private val changes = MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 64)
  val writes = mutableListOf<Pair<UUID, ByteArray>>()
  val enabledNotifications = mutableListOf<UUID>()

  override val characteristicChanges: SharedFlow<CharacteristicChange> = changes

  override fun hasService(service: UUID): Boolean = service in services

  override suspend fun enableNotifications(service: UUID, characteristic: UUID) {
    enabledNotifications += characteristic
  }

  override suspend fun write(service: UUID, characteristic: UUID, value: ByteArray) {
    writes += characteristic to value
  }

  suspend fun emit(characteristic: UUID, value: ByteArray) {
    changes.emit(CharacteristicChange(characteristic, value))
  }
}
