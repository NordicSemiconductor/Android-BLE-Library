package no.nordicsemi.android.ble.example.game.server.repository

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ble.BleServerManager
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import javax.inject.Inject

class ServerManager @Inject constructor(
    @ApplicationContext context: Context,
) : BleServerManager(context) {
    private val TAG = ServerManager::class.java.simpleName

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    /**
     * It opens the GATT server and starts initializing services [BleServerManager.initializeServer].
     * It returns a list of server GATT services with given UUID that will be available to the remote device
     * to use and a list of characteristics.
     */
    override fun initializeServer(): List<BluetoothGattService> {
        return listOf(
            service(DeviceSpecifications.UUID_SERVICE_DEVICE,
                characteristic(
                    DeviceSpecifications.UUID_MSG_CHARACTERISTIC,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_WRITE,
                    cccd(),
                    description("A sample client server interaction.", false)
                )
            )
        )
    }

}