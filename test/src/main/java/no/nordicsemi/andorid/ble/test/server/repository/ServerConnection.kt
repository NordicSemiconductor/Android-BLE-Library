package no.nordicsemi.andorid.ble.test.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.*
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.*
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
    private var readCharacteristics: BluetoothGattCharacteristic? = null

    private val _testCase: MutableSharedFlow<TestCase> = MutableSharedFlow()
    val testCases = _testCase.asSharedFlow()
    private var maxLength = 0

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    /**
     * Returns true when the gatt device supports the required services.
     *
     * @param gatt the gatt device with services discovered
     * @return True when the device has the required service.
     */
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        scope.launch {
            _testCase.emit(TestCase(SERVICE_DISCOVERY, true))
        }
        return true
    }

    override fun onServerReady(server: BluetoothGattServer) {
        server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
            serverCharacteristics =
                service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
            indicationCharacteristics =
                service.getCharacteristic(DeviceSpecifications.Ind_CHARACTERISTIC)
            reliableCharacteristics =
                service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            readCharacteristics =
                service.getCharacteristic(DeviceSpecifications.READ_CHARACTERISTIC)
        }
    }

    override fun initialize() {
        requestMtu(512).enqueue()
        waitUntil {
            return@waitUntil if (mtu > 23) {
                maxLength = mtu - 3
                true
            } else false
        }.enqueue()

    }

    override fun onServicesInvalidated() {
        serverCharacteristics = null
        indicationCharacteristics = null
        reliableCharacteristics = null
        readCharacteristics = null
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
            _testCase.emit(TestCase(DEVICE_CONNECTION, true))
        } catch (e: Exception) {
            _testCase.emit(TestCase(DEVICE_CONNECTION, false))
            e.printStackTrace()
        }
    }

    // Wait until indication is enable
    fun testIndicationEnabled(): ConditionalWaitRequest<BluetoothGattCharacteristic> {
        return waitUntilIndicationsEnabled(indicationCharacteristics)
    }

    // Wait until notification is enable
    fun testNotificationEnabled(): ConditionalWaitRequest<BluetoothGattCharacteristic> {
        return waitUntilNotificationsEnabled(serverCharacteristics)
    }

    // Send indication
    fun testSendIndication(request: ByteArray): WriteRequest {
        return sendIndication(indicationCharacteristics, request)
    }

    // Send notification
    fun testSendNotification(request: ByteArray): WriteRequest {
        return sendNotification(serverCharacteristics, request)
    }

    // Write callback
    fun testWriteCallback(): ValueChangedCallback {
        waitForWrite(serverCharacteristics).enqueue()
        return setWriteCallback(serverCharacteristics)
    }

    // Reliable Write callback
    fun testReliableWriteCallback() {
        waitForWrite(reliableCharacteristics).enqueue()
        setWriteCallback(reliableCharacteristics)
        setWriteCallback(serverCharacteristics)
    }

    // Set characteristics for to read data
    fun testSetCharacteristicValue(request: ByteArray): SetValueRequest {
       return setCharacteristicValue(readCharacteristics, request)
    }

    /**
     * Returns the maximum length that can be utilized in a single write operation.
     * MTU (Maximum Transfer Unit) indicates the maximum number of bytes that can be sent in a single write operation.
     * Since 3 bytes are used for internal purposes, so the maximum size is MTU-3.
     */
    fun requestMaxLength(): Int = maxLength

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}