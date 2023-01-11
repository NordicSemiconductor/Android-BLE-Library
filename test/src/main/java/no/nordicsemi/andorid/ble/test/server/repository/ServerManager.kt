package no.nordicsemi.andorid.ble.test.server.repository

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.BleServerManager
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
     * to use and a list of characteristics. [BleServerManager.setServerObserver] will be called once all services are done.
     */
    override fun initializeServer(): List<BluetoothGattService> {
        return listOf(
            service(
                DeviceSpecifications.UUID_SERVICE_DEVICE,
                characteristic(
                    DeviceSpecifications.REL_WRITE_CHARACTERISTIC,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                            BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
                    BluetoothGattCharacteristic.PERMISSION_WRITE,
                    description("Reliable Write", false),
                    reliableWrite()
                ),
                characteristic(
                    DeviceSpecifications.Ind_CHARACTERISTIC,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                            BluetoothGattCharacteristic.PROPERTY_INDICATE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE,
                    cccd(),
                    description("Indication", false),
                ),
                characteristic(
                    DeviceSpecifications.WRITE_CHARACTERISTIC,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_WRITE,
                    cccd(),
                    description("Write and notification", false)
                ),
            )
        )
    }
}