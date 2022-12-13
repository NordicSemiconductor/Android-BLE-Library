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
import no.nordicsemi.andorid.ble.test.server.data.TestItem
import no.nordicsemi.andorid.ble.test.server.view.TestEvent
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
    private var notificationCharacteristic: BluetoothGattCharacteristic? = null
    private var indicationCharacteristic: BluetoothGattCharacteristic? = null

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
                notificationCharacteristic = service.getCharacteristic(DeviceSpecifications.NOTIFICATION_CHARACTERISTIC)
                indicationCharacteristic = service.getCharacteristic(DeviceSpecifications.INDICATION_CHARACTERISTIC)
            }
        }

        override fun initialize() {
        }

        override fun onServicesInvalidated() {
            notificationCharacteristic = null
            indicationCharacteristic = null
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

    suspend fun testIndication(){
        val request = "This is Indication".toByteArray()
        // Creates a request that will wait for enabling indications. If indications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilIndicationsEnabled(indicationCharacteristic).suspend()

        sendIndication(indicationCharacteristic, request).suspend()
    }

    suspend fun testNotification(){
        val request = "This is Notification".toByteArray()

        //Creates a request that will wait for enabling notifications. If notifications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilNotificationsEnabled(notificationCharacteristic).suspend()

        // Sends the notification from the server characteristic. The notifications on this
        // characteristic must be enabled before the request is executed.
        sendNotification(notificationCharacteristic, request).suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}