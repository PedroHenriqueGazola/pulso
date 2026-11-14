package app.pulso.watch.bluetooth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pairingDataStore by preferencesDataStore(name = "pairing")

class PairedIphoneStore(private val context: Context) {
  private val pairedAddressKey = stringPreferencesKey("paired_address")

  val pairedAddress: Flow<String?> = context.pairingDataStore.data.map { preferences -> preferences[pairedAddressKey] }

  suspend fun save(address: String) {
    context.pairingDataStore.edit { preferences -> preferences[pairedAddressKey] = address }
  }

  suspend fun clear() {
    context.pairingDataStore.edit { preferences -> preferences.remove(pairedAddressKey) }
  }
}
