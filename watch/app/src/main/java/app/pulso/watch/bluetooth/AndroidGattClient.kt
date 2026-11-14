package app.pulso.watch.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("MissingPermission")
class AndroidGattClient(private val gatt: BluetoothGatt) : GattClient {
  private val operationQueue = GattOperationQueue()
  private val changes = MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 256)
  @Volatile private var pendingOperation: CompletableDeferred<Int>? = null

  override val characteristicChanges: SharedFlow<CharacteristicChange> = changes

  override fun hasService(service: UUID): Boolean = gatt.getService(service) != null

  override suspend fun enableNotifications(service: UUID, characteristic: UUID) = operationQueue.run {
    val gattCharacteristic = findCharacteristic(service, characteristic)
    gatt.setCharacteristicNotification(gattCharacteristic, true)
    val descriptor = gattCharacteristic.getDescriptor(CLIENT_CONFIGURATION_DESCRIPTOR)
      ?: throw GattOperationException("Característica $characteristic não aceita notificações")
    awaitOperation("habilitar notificações de $characteristic") {
      gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }
  }

  override suspend fun write(service: UUID, characteristic: UUID, value: ByteArray) = operationQueue.run {
    val gattCharacteristic = findCharacteristic(service, characteristic)
    awaitOperation("escrever em $characteristic") {
      gatt.writeCharacteristic(gattCharacteristic, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
    }
  }

  fun onCharacteristicChanged(characteristic: UUID, value: ByteArray) {
    changes.tryEmit(CharacteristicChange(characteristic, value))
  }

  fun onOperationCompleted(status: Int) {
    pendingOperation?.complete(status)
  }

  fun onDisconnected() {
    pendingOperation?.complete(BluetoothGatt.GATT_FAILURE)
  }

  private suspend fun awaitOperation(description: String, startOperation: () -> Int) {
    val operation = CompletableDeferred<Int>()
    pendingOperation = operation
    val startStatus = startOperation()
    if (startStatus != BluetoothStatusCodes.SUCCESS) {
      throw GattOperationException("Não foi possível $description (código $startStatus)")
    }
    val status = withTimeoutOrNull(OPERATION_TIMEOUT_MILLIS) { operation.await() }
      ?: throw GattOperationException("Tempo esgotado ao $description")
    if (status != BluetoothGatt.GATT_SUCCESS) throw GattOperationException("Falha ao $description (status $status)")
  }

  private fun findCharacteristic(service: UUID, characteristic: UUID): BluetoothGattCharacteristic =
    gatt.getService(service)?.getCharacteristic(characteristic)
      ?: throw GattOperationException("Característica $characteristic não encontrada")

  private companion object {
    val CLIENT_CONFIGURATION_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    const val OPERATION_TIMEOUT_MILLIS = 5_000L
  }
}
