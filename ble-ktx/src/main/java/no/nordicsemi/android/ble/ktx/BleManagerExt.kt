package no.nordicsemi.android.ble.ktx

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.state.BondState
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.observer.BondingObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * Returns the current connection state of the device. In case the device is disconnected,
 * the reason cannot be determined, therefore Unknown is returned.
 * @since 2.3.0
 * @see BleManager.getConnectionState
 * @see BleManager.isReady
 */
val BleManager.state: ConnectionState
    get() = ConnectionState.of(this)

/**
 * Returns the current bonding status of the device.
 *
 * In case the device was not set using [BleManager.connect], this returns [BondState.NotBonded].
 * @since 2.3.0
 * @see BleManager.getConnectionState
 * @see BleManager.isReady
 */
val BleManager.bondingState: BondState
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    get() = when (bluetoothDevice?.bondState) {
        BluetoothDevice.BOND_BONDED -> BondState.Bonded
        BluetoothDevice.BOND_BONDING -> BondState.Bonding
        else -> BondState.NotBonded
    }

/**
 * Returns the connection state as hot state flow.
 */
fun BleManager.stateAsFlow() = MutableStateFlow(state)
        .apply { setConnectionObserver(observeAsFlow(this)) }

/**
 * Returns the bonding state as hot state flow.
 */
fun BleManager.bondingStateAsFlow() = MutableStateFlow(bondingState)
        .apply { setBondingObserver(observeAsFlow(this)) }

// ------------------------------------ Implementation ------------------------------------

private fun observeAsFlow(flow: MutableStateFlow<ConnectionState>) = object: ConnectionObserver {

    override fun onDeviceConnecting(device: BluetoothDevice) {
        flow.tryEmit(ConnectionState.Connecting)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        flow.tryEmit(ConnectionState.Initializing)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        flow.tryEmit(ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.parse(reason)))
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        flow.tryEmit(ConnectionState.Ready)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        flow.tryEmit(ConnectionState.Disconnecting)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        flow.tryEmit(ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.parse(reason)))
    }

}

private fun observeAsFlow(flow: MutableStateFlow<BondState>) = object: BondingObserver {
    override fun onBondingRequired(device: BluetoothDevice) {
        flow.tryEmit(BondState.Bonding)
    }

    override fun onBonded(device: BluetoothDevice) {
        flow.tryEmit(BondState.Bonded)
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        flow.tryEmit(BondState.Bonding)
    }

}