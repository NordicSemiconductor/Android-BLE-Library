package no.nordicsemi.android.ble.livedata

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import no.nordicsemi.android.ble.observer.ConnectionObserver

@Suppress("unused")
internal class ConnectionStateLiveData: LiveData<ConnectionState>(
        ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
), ConnectionObserver {

    init {
        value = ConnectionState.Disconnected(reason = ConnectionObserver.REASON_UNKNOWN)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        value = ConnectionState.Connecting
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        value = ConnectionState.Initializing
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        value = ConnectionState.Ready
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        value = ConnectionState.Disconnecting
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        value = ConnectionState.Disconnected(reason)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        value = ConnectionState.Disconnected(reason)
    }

}