package no.nordicsemi.andorid.ble.test.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.TestItem
import no.nordicsemi.andorid.ble.test.server.view.TestEvent
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.suspend

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ServerConnection::class.java.simpleName
    private var serverCharacteristic: BluetoothGattCharacteristic? = null

    private val _replies: MutableSharedFlow<String> = MutableSharedFlow()
    val replies = _replies.asSharedFlow()
    private val _testingFeature: MutableSharedFlow<TestEvent> = MutableSharedFlow()
    val testingFeature =_testingFeature.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ServerGattCallback()

    private inner class ServerGattCallback : BleManagerGattCallback() {

        /**
         * Returns true when the gatt device supports the required services.
         * @param gatt the gatt device with services discovered
         * @return True when the device has the required service.
         */
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            scope.launch {
                _testingFeature.emit(TestEvent(TestItem.SERVICE_DISCOVERY.item, true) )
            }
            return true
        }

        override fun onServerReady(server: BluetoothGattServer) {
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
        }

        override fun initialize() {
        }

        override fun onServicesInvalidated() {
            serverCharacteristic = null
        }
    }

    /**
     * Connects to the client.
     */
    suspend fun connect() {
        try {
            connect(device)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(10_000)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun replyHello(){
        val request = "Hello!".toByteArray()
        sendNotification(serverCharacteristic,request)
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}