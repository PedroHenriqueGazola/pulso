package app.pulso.watch.bluetooth

import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeGattClient(private val services: Set<UUID>) : GattClient {
  private val changes = MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 64)
  val writes = mutableListOf<Pair<UUID, ByteArray>>()
  val enabledNotifications = mutableListOf<UUID>()
  var shouldFailNextWrite = false

  override val characteristicChanges: SharedFlow<CharacteristicChange> = changes

  override fun hasService(service: UUID): Boolean = service in services

  override suspend fun enableNotifications(service: UUID, characteristic: UUID) {
    enabledNotifications += characteristic
  }

  override suspend fun write(service: UUID, characteristic: UUID, value: ByteArray) {
    writes += characteristic to value
    if (!shouldFailNextWrite) return
    shouldFailNextWrite = false
    throw GattOperationException("iPhone respondeu com erro 0xA2")
  }

  suspend fun emit(characteristic: UUID, value: ByteArray) {
    changes.emit(CharacteristicChange(characteristic, value))
  }
}
