package app.pulso.watch.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BondStateReceiver(
  private val onBonded: (BluetoothDevice) -> Unit,
  private val onBondFailed: (BluetoothDevice) -> Unit,
) : BroadcastReceiver() {

  fun register(context: Context) {
    context.registerReceiver(this, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED), Context.RECEIVER_EXPORTED)
  }

  override fun onReceive(context: Context, intent: Intent) {
    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java) ?: return
    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
    val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
    if (bondState == BluetoothDevice.BOND_BONDED) return onBonded(device)
    if (bondState == BluetoothDevice.BOND_NONE && previousBondState == BluetoothDevice.BOND_BONDING) onBondFailed(device)
  }
}
