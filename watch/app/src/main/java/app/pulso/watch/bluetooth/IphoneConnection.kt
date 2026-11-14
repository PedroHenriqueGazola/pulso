package app.pulso.watch.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import app.pulso.watch.ancs.AncsUuids
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class IphoneConnection(
  private val context: Context,
  private val store: PairedIphoneStore,
  private val scope: CoroutineScope,
) {
  private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.NotPaired)
  private val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
  private val bondStateReceiver = BondStateReceiver(onBonded = ::handleBonded, onBondFailed = ::handleBondFailed)
  @Volatile private var gatt: BluetoothGatt? = null
  @Volatile private var gattClient: AndroidGattClient? = null
  @Volatile private var hasDiscoveredServices = false

  val state: StateFlow<ConnectionState> = mutableState

  init {
    bondStateReceiver.register(context)
  }

  fun connect(address: String) {
    closeGatt()
    val device = bluetoothAdapter.getRemoteDevice(address)
    mutableState.value = ConnectionState.Connecting(nameOf(device))
    openGatt(device)
  }

  fun markScanning() {
    mutableState.value = ConnectionState.Scanning
  }

  fun forget() {
    closeGatt()
    scope.launch { store.clear() }
    mutableState.value = ConnectionState.NotPaired
  }

  private val gattCallback = object : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
      Log.i(TAG, "Conexão mudou: status=$status estado=$newState")
      if (newState == BluetoothProfile.STATE_CONNECTED) return handleConnected(gatt)
      if (newState == BluetoothProfile.STATE_DISCONNECTED) handleDisconnected(gatt)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
      handleServicesDiscovered(gatt, status)
    }

    override fun onCharacteristicChanged(
      gatt: BluetoothGatt,
      characteristic: BluetoothGattCharacteristic,
      value: ByteArray,
    ) {
      gattClient?.onCharacteristicChanged(characteristic.uuid, value)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
      gattClient?.onOperationCompleted(status)
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
      gattClient?.onOperationCompleted(status)
    }
  }

  private fun handleConnected(connectedGatt: BluetoothGatt) {
    hasDiscoveredServices = false
    val device = connectedGatt.device
    mutableState.value = ConnectionState.Connecting(nameOf(device))
    if (device.bondState == BluetoothDevice.BOND_BONDED) return discoverServicesOnce(connectedGatt)

    Log.i(TAG, "iPhone ainda não pareado, iniciando bond")
    device.createBond()
  }

  private fun handleBonded(device: BluetoothDevice) {
    val connectedGatt = gatt ?: return
    if (connectedGatt.device.address != device.address) return
    Log.i(TAG, "Bond concluído com ${device.address}")
    discoverServicesOnce(connectedGatt)
  }

  private fun handleBondFailed(device: BluetoothDevice) {
    if (gatt?.device?.address != device.address) return
    Log.w(TAG, "Pareamento recusado ou falhou")
    forget()
  }

  private fun discoverServicesOnce(connectedGatt: BluetoothGatt) {
    if (hasDiscoveredServices) return
    hasDiscoveredServices = true
    connectedGatt.discoverServices()
  }

  private fun handleServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
    if (status != BluetoothGatt.GATT_SUCCESS || discoveredGatt.getService(AncsUuids.SERVICE) == null) {
      Log.w(TAG, "ANCS não encontrado (status=$status); aguardando nova conexão")
      hasDiscoveredServices = false
      discoveredGatt.disconnect()
      return
    }
    val device = discoveredGatt.device
    val client = AndroidGattClient(discoveredGatt)
    gattClient = client
    scope.launch { store.save(device.address) }
    mutableState.value = ConnectionState.Connected(nameOf(device), client)
  }

  private fun handleDisconnected(disconnectedGatt: BluetoothGatt) {
    if (mutableState.value == ConnectionState.NotPaired) return
    val device = disconnectedGatt.device
    closeGatt()
    mutableState.value = ConnectionState.WaitingForIphone(nameOf(device))
    openGatt(device)
  }

  private fun openGatt(device: BluetoothDevice) {
    gatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
  }

  private fun closeGatt() {
    gattClient?.onDisconnected()
    gattClient = null
    gatt?.close()
    gatt = null
  }

  private fun nameOf(device: BluetoothDevice): String = device.name ?: DEFAULT_IPHONE_NAME

  private companion object {
    const val TAG = "Pulso"
    const val DEFAULT_IPHONE_NAME = "iPhone"
  }
}
