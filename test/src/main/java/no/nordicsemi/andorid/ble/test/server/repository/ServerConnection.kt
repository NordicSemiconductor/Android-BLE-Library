package no.nordicsemi.andorid.ble.test.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.TestEvent
import no.nordicsemi.andorid.ble.test.server.data.TestItem
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend

@SuppressLint("MissingPermission")
class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ServerConnection::class.java.simpleName
    private var serverCharacteristics: BluetoothGattCharacteristic? = null

    private val _testingFeature: MutableSharedFlow<TestEvent> = MutableSharedFlow()
    val testingFeature = _testingFeature.asSharedFlow()

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
                _testingFeature.emit(TestEvent(TestItem.SERVICE_DISCOVERY.item, true))
            }
            return true
        }

        override fun onServerReady(server: BluetoothGattServer) {
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristics = service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
            }
        }

        override fun initialize() {
        }

        override fun onServicesInvalidated() {
            serverCharacteristics = null
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
                .also {
                    scope.launch {
                        _testingFeature.emit(TestEvent(TestItem.DEVICE_CONNECTION.item, true))
                    }
                }
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun testIndication() {
        val request = "This is Indication".toByteArray()
        // Creates a request that will wait for enabling indications. If indications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilIndicationsEnabled(serverCharacteristics)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.WAIT_UNTIL_INDICATION_ENABLED.item, true))
                }
            }
            .enqueue()

        sendIndication(serverCharacteristics, request)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.SEND_INDICATION.item, true))
                }
            }
            .enqueue()
    }

    suspend fun testNotification() {
        val request = "This is Notification".toByteArray()
        //Creates a request that will wait for enabling notifications. If notifications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilNotificationsEnabled(serverCharacteristics)
            .also {
                scope.launch {
                    _testingFeature.emit(
                        TestEvent(
                            TestItem.WAIT_UNTIL_NOTIFICATION_ENABLED.item,
                            true
                        )
                    )
                }
            }
            .enqueue()

        // Sends the notification from the server characteristic. The notifications on this
        // characteristic must be enabled before the request is executed.
        sendNotification(serverCharacteristics, request)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.SEND_NOTIFICATION.item, true))
                }
            }
            .enqueue()
    }

    suspend fun testWrite() {
        setWriteCallback(serverCharacteristics)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.WRITE_CALLBACK.item, true))
                }
            }
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}