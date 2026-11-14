package app.pulso.watch.bluetooth

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow

class CharacteristicChange(val characteristic: UUID, val value: ByteArray)

class GattOperationException(message: String) : Exception(message)

interface GattClient {
  val characteristicChanges: SharedFlow<CharacteristicChange>

  fun hasService(service: UUID): Boolean

  suspend fun enableNotifications(service: UUID, characteristic: UUID)

  suspend fun write(service: UUID, characteristic: UUID, value: ByteArray)
}
