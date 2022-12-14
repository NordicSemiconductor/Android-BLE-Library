package no.nordicsemi.andorid.ble.test.client.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.client.data.TestItem
import no.nordicsemi.andorid.ble.test.server.data.TestEvent
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName
    var characteristic: BluetoothGattCharacteristic? = null

    private val _testingFeature: MutableSharedFlow<TestEvent> = MutableSharedFlow()
    val testingFeature = _testingFeature.asSharedFlow()

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
            scope.launch {
                _testingFeature.emit(TestEvent(TestItem.SERVICE_DISCOVERY.item, true))
            }
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                characteristic =
                    service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
            }
            return characteristic != null
        }

        override fun initialize() {
            requestMtu(512).enqueue()
        }

        override fun onServicesInvalidated() {
            characteristic = null
        }
    }

    fun testIndicationsWithCallback() {
        setIndicationCallback(characteristic)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.SET_INDICATION_CALLBACK.item, true))
                }
            }
        enableIndications(characteristic)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.ENABLE_INDICATION.item, true))
                }
            }
            .enqueue()
    }

    fun testNotificationsWithCallback() {
        setNotificationCallback(characteristic)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.SET_NOTIFICATION_CALLBACK.item, true))
                }
            }
        enableNotifications(characteristic)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.ENABLE_NOTIFICATION.item, true))
                }
            }
            .enqueue()
    }

    suspend fun testWrite() {
        val request = "This is write".toByteArray()
        writeCharacteristic(characteristic, request, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also {
                scope.launch {
                    _testingFeature.emit(TestEvent(TestItem.WRITE_CHARACTERISTICS.item, true))
                }
            }
            .suspend()
    }

    suspend fun connect() {
        try {
            connect(device)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(100_000)
                .also {
                    scope.launch {
                        _testingFeature.emit(TestEvent(TestItem.CONNECTED_WITH_SERVER.item, true))
                    }
                }
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}