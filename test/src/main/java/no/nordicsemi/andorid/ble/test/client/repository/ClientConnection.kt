package no.nordicsemi.andorid.ble.test.client.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.client.data.CONNECTED_WITH_SERVER
import no.nordicsemi.andorid.ble.test.client.data.SERVICE_DISCOVERY
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName

    private var characteristic: BluetoothGattCharacteristic? = null
    private var indicationCharacteristics: BluetoothGattCharacteristic? = null
    private var reliableCharacteristics: BluetoothGattCharacteristic? = null
    private var readCharacteristics: BluetoothGattCharacteristic? = null

    private val _testCase: MutableSharedFlow<TestCase> = MutableSharedFlow()
    val testCase = _testCase.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    // Return false if a required service has not been discovered.
    @SuppressLint("MissingPermission")
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        scope.launch {
            _testCase.emit(TestCase(SERVICE_DISCOVERY, true))
        }
        gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
            characteristic = service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
            indicationCharacteristics =
                service.getCharacteristic(DeviceSpecifications.Ind_CHARACTERISTIC)
            reliableCharacteristics =
                service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            readCharacteristics =
                service.getCharacteristic(DeviceSpecifications.READ_CHARACTERISTIC)
        }
        return characteristic != null && indicationCharacteristics != null && reliableCharacteristics != null && readCharacteristics != null
    }

    override fun initialize() {
        requestMtu(512).enqueue()
    }

    override fun onServicesInvalidated() {
        characteristic = null
        indicationCharacteristics = null
        reliableCharacteristics = null
        readCharacteristics = null
    }

    // Write Request
    fun testWrite(
        request: ByteArray,
    ): WriteRequest {
        return writeCharacteristic(characteristic, request, WRITE_TYPE_DEFAULT)
    }

    // Reliable Write Request
    fun testReliableWrite(
        request: ByteArray,
    ): ReliableWriteRequest {
        return beginReliableWrite()
            .add(writeCharacteristic(reliableCharacteristics, request, WRITE_TYPE_DEFAULT))
            .add(writeCharacteristic(characteristic, request, WRITE_TYPE_DEFAULT))

    }

    // Set Indication Callback
    fun testSetIndication() {
        setIndicationCallback(indicationCharacteristics)
            .merge(HeaderBasedPacketMerger())
    }

    // Enable Indication
    fun testEnableIndication(): WriteRequest {
        return enableIndications(indicationCharacteristics)
    }

    // Wait for Indication
    fun testWaitForIndication(): WaitForValueChangedRequest {
        return waitForIndication(indicationCharacteristics)
    }

    // Remove Indication callback
    fun testRemoveIndicationCallback(){
        return removeIndicationCallback(indicationCharacteristics)
    }

    // Set Notification Callback
    fun testSetNotification(){
        setNotificationCallback(characteristic)
            .merge(HeaderBasedPacketMerger())
    }

    // Enable Notification
    fun testEnableNotification(): WriteRequest {
        return enableNotifications(characteristic)
    }

    // Wait for notification
    fun testWaitForNotification(): WaitForValueChangedRequest {
        return waitForNotification(characteristic)
    }

    // Remove notification callback
    fun testRemoveNotificationCallback() {
        return removeNotificationCallback(characteristic)
    }

    // Read Characteristics
    fun testReadCharacteristics(): ReadRequest {
        return readCharacteristic(readCharacteristics)
    }

    // Begin Atomic
    fun testBeginAtomicRequestQueue(): RequestQueue {
         return beginAtomicRequestQueue()
             .add(writeCharacteristic(characteristic, "this is atomic request".toByteArray(), WRITE_TYPE_DEFAULT))

    }

    /**
     * Connect with server.
     */
    suspend fun connect() {
        try {
            connect(device)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(100_000)
                .suspend()
            _testCase.emit(TestCase(CONNECTED_WITH_SERVER, true))
        } catch (e: Exception) {
            _testCase.emit(TestCase(CONNECTED_WITH_SERVER, false))
            e.printStackTrace()
        }
    }

    /**
     * Checks if the size of the last packet is equal to the maxLength,
     * and if it is, it adds a single space character to the end of the request.
     * This prevents the merger function from waiting for the next packet if size of the last packet
     * is equal to the maxLength.
     */
    fun checkSizeOfRequest(request: ByteArray) =
        if ((mtu - 3) % request.size == 0) request + " ".toByteArray() else request

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}