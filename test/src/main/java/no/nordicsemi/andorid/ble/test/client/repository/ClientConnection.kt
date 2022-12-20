package no.nordicsemi.andorid.ble.test.client.repository

import android.annotation.SuppressLint
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
import no.nordicsemi.andorid.ble.test.client.data.reliableRequest
import no.nordicsemi.andorid.ble.test.client.data.request
import no.nordicsemi.andorid.ble.test.client.data.splitterRequest
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName
    var characteristic: BluetoothGattCharacteristic? = null
    var indicationCharacteristics: BluetoothGattCharacteristic? = null
    var reliableCharacteristics: BluetoothGattCharacteristic? = null

    private val _testCase: MutableSharedFlow<TestCase> = MutableSharedFlow()
    val testCase = _testCase.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback : BleManagerGattCallback() {

        @SuppressLint("MissingPermission")
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            // Return false if a required service has not been discovered.
            scope.launch {
                _testCase.emit(TestCase(TestItem.SERVICE_DISCOVERY.item, true))
            }
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                characteristic = service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
                indicationCharacteristics = service.getCharacteristic(DeviceSpecifications.Ind_CHARACTERISTIC)
                reliableCharacteristics = service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            }
            return characteristic != null && indicationCharacteristics != null && reliableCharacteristics != null
        }

        override fun initialize() {
            requestMtu(25).enqueue()
        }

        override fun onServicesInvalidated() {
            characteristic = null
            indicationCharacteristics = null
            reliableCharacteristics = null
        }
    }

    fun testIndicationsWithCallback() {
        setIndicationCallback(indicationCharacteristics)
            .also {
                scope.launch {
                    _testCase.emit(TestCase(TestItem.SET_INDICATION.item, true))
                }
            }
        enableIndications(indicationCharacteristics)
            .also {
                scope.launch {
                    _testCase.emit(TestCase(TestItem.ENABLE_INDICATION.item, true))
                }
            }
            .enqueue()
    }

    fun testNotificationsWithCallback() {
        setNotificationCallback(characteristic)
            .also {
                scope.launch {
                    _testCase.emit(TestCase(TestItem.SET_NOTIFICATION.item, true))
                }
            }
        testAtomicQueue()
    }

    // Begin Atomic Request Queue
    private fun testAtomicQueue(){
        beginAtomicRequestQueue()
            .add(enableNotifications(characteristic)
                .also {scope.launch {
                _testCase.emit(TestCase(TestItem.ENABLE_NOTIFICATION.item, true))
            }  })
            .also { scope.launch { _testCase.emit(TestCase(TestItem.ATOMIC_REQUEST_QUEUE.item, true)) }}
            .enqueue()
    }

    // Write request
    suspend fun testWrite() {
        writeCharacteristic(characteristic, request, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also { _testCase.emit(TestCase(TestItem.WRITE_CHARACTERISTICS.item, true)) }
            .suspend()
    }

    // Reliable write
    suspend fun testReliableWrite() {
        beginReliableWrite()
            .add(writeCharacteristic(reliableCharacteristics, reliableRequest, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .split(FlagBasedPacketSplitter())
            )
            .done {scope.launch { _testCase.emit(TestCase(TestItem.RELIABLE_WRITE.item, true)) }}
            .fail { _, _ ->
                scope.launch {
                    _testCase.emit(TestCase(TestItem.RELIABLE_WRITE.item, false))
                }
            }
            .enqueue()
    }

    suspend fun testWriteWithSplitter() {
        // Write request with flag based splitter
        writeCharacteristic(characteristic, splitterRequest, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also { _testCase.emit(TestCase(TestItem.FLAG_BASED_SPLITTER.item,true)) }
            .split(FlagBasedPacketSplitter())
            .suspend()

        // Write request with header based splitter
        writeCharacteristic(characteristic, splitterRequest, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also { _testCase.emit(TestCase(TestItem.HEADER_BASED_SPLITTER.item, true)) }
            .split(HeaderBasedPacketSplitter())
            .suspend()

        // Write request with default splitter
        val requestToSend = checkSizeOfRequest(splitterRequest)
        writeCharacteristic(characteristic, requestToSend, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also { _testCase.emit(TestCase(TestItem.DEFAULT_MTU_SPLITTER.item, true)) }
            .split()
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
                        _testCase.emit(TestCase(TestItem.CONNECTED_WITH_SERVER.item, true))
                    }
                }
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks if the size of the last packet is equal to the maxLength,
     * and if it is, it adds a single space character to the end of the request.
     * This prevents the merger function from waiting for the next packet if size of the last packet
     * is equal to the maxLength.
     */
    private fun checkSizeOfRequest(request: ByteArray) =
        if ((mtu - 3) % request.size == 0) request + " ".toByteArray() else request

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}