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
import no.nordicsemi.andorid.ble.test.server.data.DEVICE_CONNECTION
import no.nordicsemi.andorid.ble.test.server.data.SEND_NOTIFICATION
import no.nordicsemi.andorid.ble.test.server.data.SERVICE_DISCOVERY
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConditionalWaitRequest
import no.nordicsemi.android.ble.ValueChangedCallback
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

    // Write callback
    fun testWriteCallback(): ValueChangedCallback {
        waitForWrite(serverCharacteristics).enqueue()
        return setWriteCallback(serverCharacteristics)
    }

    // Write callback for mtu size merger
    fun testWriteCallbackWithMerger(): ValueChangedCallback {
        waitForWrite(serverCharacteristics).enqueue()
        return setWriteCallback(serverCharacteristics)
                // filters packet of size 1
            .filterPacket { data -> data != null && data.size == 1 }
    }

    // Wait until indication is enable
    fun testWaiUntilIndicationEnabled(): ConditionalWaitRequest<BluetoothGattCharacteristic> {
        return waitUntilIndicationsEnabled(indicationCharacteristics)
    }

    // Send indication
    suspend fun testSendIndication(request: ByteArray) {
        sendIndication(indicationCharacteristics, request)
            .split(FlagBasedPacketSplitter())
            .suspend()
    }

    // Wait until notification is enable
    fun testWaitUntilNotificationEnabled(request: ByteArray) {
        waitUntilNotificationsEnabled(serverCharacteristics)
            .then {
                sendNotification(serverCharacteristics, request)
                    .split(HeaderBasedPacketSplitter())
                    .done { scope.launch { _testCase.emit(TestCase(SEND_NOTIFICATION, true)) } }
                    .fail { _, _ ->
                        scope.launch {
                            _testCase.emit(
                                TestCase(
                                    SEND_NOTIFICATION,
                                    false
                                )
                            )
                        }
                    }
                    .enqueue()
            }
            .enqueue()
    }

    // Send notification
    suspend fun testSendNotification(request: ByteArray) {
        sendNotification(serverCharacteristics, request)
            .split(HeaderBasedPacketSplitter())
            .suspend()
    }

    // Reliable Write callback
    fun testReliableWriteCallback() {
        waitForWrite(reliableCharacteristics).enqueue()
        setWriteCallback(reliableCharacteristics)
        setWriteCallback(serverCharacteristics)
    }

    // Set characteristics for to read data
    fun testSetReadCharacteristics(request: ByteArray) {
        waitForRead(readCharacteristics).enqueue()
        setCharacteristicValue(readCharacteristics, request).enqueue()
    }

    // Atomic write
    fun testBeginAtomicRequestQueue(){
        waitForWrite(serverCharacteristics).enqueue()
        setWriteCallback(serverCharacteristics)
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