package no.nordicsemi.andorid.ble.test.scanner.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications.Companion.UUID_MSG_CHARACTERISTIC
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.suspend

@OptIn(ExperimentalCoroutinesApi::class)
class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName
    var characteristic: BluetoothGattCharacteristic? = null

    private val _replies: MutableSharedFlow<String> = MutableSharedFlow()
    val replies = _replies.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            // Return false if a required service has not been discovered.
            gatt.getService(UUID_SERVICE_DEVICE)?.let { service ->
                characteristic = service.getCharacteristic(UUID_MSG_CHARACTERISTIC)
            }

            return characteristic != null
        }

        override fun initialize() {
            requestMtu(512).enqueue()
            setNotificationCallback(characteristic)
                .asFlow()
                .mapNotNull { it.getStringValue(0) }
                .onEach { replies ->
                    _replies.emit(replies)
                    Log.d(TAG, "initialize: this is replies $replies")
                }
                .launchIn(scope)

            enableNotifications(characteristic).enqueue()
        }

        override fun onServicesInvalidated() {
            characteristic = null
        }

    }

    suspend fun connect() {
        try {
            connect(device)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(100_000)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun hello() {
        val request = "Hello".toByteArray()
        writeCharacteristic(characteristic, request, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}