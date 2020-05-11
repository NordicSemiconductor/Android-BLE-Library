package no.nordicsemi.android.ble.livedata

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import no.nordicsemi.android.ble.livedata.state.BondState
import no.nordicsemi.android.ble.observer.BondingObserver

@Suppress("unused")
internal class BondingStateLiveData: LiveData<BondState>(BondState.NotBonded), BondingObserver {

    override fun onBonded(device: BluetoothDevice) {
        value = BondState.Bonded
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        value = BondState.NotBonded
    }

    override fun onBondingRequired(device: BluetoothDevice) {
        value = BondState.Bonding
    }

}