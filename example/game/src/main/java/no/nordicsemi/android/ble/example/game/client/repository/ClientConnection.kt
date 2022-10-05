package no.nordicsemi.android.ble.example.game.client.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    var characteristic: BluetoothGattCharacteristic? = null

    private val _replies = MutableSharedFlow<String>()
    val replies = _replies.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, "AAAClient", message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback: BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                characteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
            log(Log.WARN, "Supported: ${characteristic != null}")
            return characteristic != null
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            requestMtu(512).enqueue()

            // TODO
            setNotificationCallback(characteristic).asFlow()
                .mapNotNull { it.getStringValue(0) }
                .onEach { _replies.tryEmit(it) }
                .launchIn(scope)

            enableNotifications(characteristic).enqueue()
            // enable notifications, write user name, etc
            // setNotificationCallback()
            // enableNotifications().enqueue()
        }

        override fun onServicesInvalidated() {
            characteristic = null
        }
    }

    /**
     * Connects to the server.
     */
    suspend fun connect() {
        connect(device)
            .retry(4, 300)
            .useAutoConnect(false)
            .timeout(10_000)
            .suspend()
    }

    suspend fun sayHello() {
        writeCharacteristic(
            characteristic,
            "Hello".toByteArray(),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}