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
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.TestItem
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger
import no.nordicsemi.andorid.ble.test.spec.MtuBasedMerger
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
    private var indicationCharacteristics: BluetoothGattCharacteristic? = null
    private var reliableCharacteristics: BluetoothGattCharacteristic? = null

    private val _testingFeature: MutableSharedFlow<TestCase> = MutableSharedFlow()
    val testingFeature = _testingFeature.asSharedFlow()

    private var maxLength = 0

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
                _testingFeature.emit(TestCase(TestItem.SERVICE_DISCOVERY.item, true))
            }
            return true
        }

        override fun onServerReady(server: BluetoothGattServer) {
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristics = service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
                indicationCharacteristics = service.getCharacteristic(DeviceSpecifications.Ind_CHARACTERISTIC)
                reliableCharacteristics = service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            }
        }

        override fun initialize() {
            // Returns the current MTU (Maximum Transfer Unit). MTU indicates the maximum number of bytes that can be sent in a single write operation.
            // Since 3 bytes are used for internal purposes, so the maximum size is MTU-3.
            maxLength = mtu - 3
        }

        override fun onServicesInvalidated() {
            serverCharacteristics = null
            indicationCharacteristics = null
            reliableCharacteristics = null
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
                .also { _testingFeature.emit(TestCase(TestItem.DEVICE_CONNECTION.item, true)) }
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun testIndication() {
        val request = "This is Indication".toByteArray()
        // Creates a request that will wait for enabling indications. If indications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilIndicationsEnabled(indicationCharacteristics)
            .also { _testingFeature.emit(TestCase(TestItem.WAIT_UNTIL_INDICATION_ENABLED.item, true)) }
            .enqueue()

        sendIndication(indicationCharacteristics, request)
            .also { _testingFeature.emit(TestCase(TestItem.SEND_INDICATION.item, true)) }
            .enqueue()
    }

    suspend fun testNotification() {
        val request = "This is Notification".toByteArray()
        //Creates a request that will wait for enabling notifications. If notifications were
        // enabled at the time of executing the request, it will complete immediately.
        waitUntilNotificationsEnabled(serverCharacteristics)
            .also { _testingFeature.emit(TestCase(TestItem.WAIT_UNTIL_NOTIFICATION_ENABLED.item, true)) }
            .enqueue()

        // Sends the notification from the server characteristic. The notifications on this
        // characteristic must be enabled before the request is executed.
        sendNotification(serverCharacteristics, request)
            .also { _testingFeature.emit(TestCase(TestItem.SEND_NOTIFICATION.item, true)) }
            .enqueue()
    }

    // Write callback
    suspend fun testWrite() {
        setWriteCallback(serverCharacteristics)
            .also { _testingFeature.emit(TestCase(TestItem.WRITE_CALLBACK.item, true)) }
    }

    // Write with different types merger functions
    suspend fun testWriteWithMerger() {
        // Write callback with flag based merger
        setWriteCallback(serverCharacteristics)
            .merge(FlagBasedPacketMerger())
            .also { _testingFeature.emit( TestCase( TestItem.FLAG_BASED_MERGER.item, true )) }

        // Write callback with header based merger
        setWriteCallback(serverCharacteristics)
            .merge(HeaderBasedPacketMerger())
            .also { _testingFeature.emit( TestCase(TestItem.HEADER_BASED_MERGER.item, true)) }

        // Write callback with mtu based merger
        setWriteCallback(serverCharacteristics)
            .merge(MtuBasedMerger(maxLength))
            .also { _testingFeature.emit(TestCase(TestItem.MTU_SIZE_MERGER.item,true)) }
    }

    // Reliable write
    suspend fun testReliableWrite(){
        setWriteCallback(reliableCharacteristics)
            .also {  _testingFeature.emit(TestCase(TestItem.RELIABLE_WRITE.item,true))  }
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}