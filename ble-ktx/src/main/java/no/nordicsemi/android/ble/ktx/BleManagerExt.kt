@file:Suppress("unused")

package no.nordicsemi.android.ble.ktx

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    @SuppressLint("MissingPermission")
    get() = when (bluetoothDevice?.bondState) {
        BluetoothDevice.BOND_BONDED -> BondState.Bonded
        BluetoothDevice.BOND_BONDING -> BondState.Bonding
        else -> BondState.NotBonded
    }

/**
 * Returns the connection state as hot state flow.
 * Multiple calls for this method return the same object.
 * If a different connection observer was set using [BleManager.setConnectionObserver], this
 * method will throw [IllegalStateException].
 * @since 2.3.0
 */
fun BleManager.stateAsFlow(): StateFlow<ConnectionState> = with(connectionObserver) {
    when (this) {
        null -> ConnectionObserverFlow(state).apply { connectionObserver = this }.flow
        is ConnectionObserverFlow -> flow
        else -> throw IllegalStateException("Observer already set")
    }
}

/**
 * Returns the bonding state as hot state flow.
 * Multiple calls for this method return the same object.
 * If a different bond state observer was set using [BleManager.setBondingObserver], this
 * method will throw [IllegalStateException].
 * @since 2.3.0
 */
fun BleManager.bondingStateAsFlow(): StateFlow<BondState> = with(bondingObserver) {
    when (this) {
        null -> BondStateObserverFlow(bondingState).apply { bondingObserver = this }.flow
        is BondStateObserverFlow -> flow
        else -> throw IllegalStateException("Observer already set")
    }
}

// ------------------------------------ Implementation ------------------------------------

private class ConnectionObserverFlow(value: ConnectionState): ConnectionObserver {
    val flow: MutableStateFlow<ConnectionState> = MutableStateFlow(value)

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

private class BondStateObserverFlow(value: BondState): BondingObserver {
    val flow: MutableStateFlow<BondState> = MutableStateFlow(value)

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