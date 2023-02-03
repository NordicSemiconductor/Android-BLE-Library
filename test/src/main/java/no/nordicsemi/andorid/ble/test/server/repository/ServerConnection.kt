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
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.*
import no.nordicsemi.andorid.ble.test.spec.Callbacks.SEND_NOTIFICATION
import no.nordicsemi.andorid.ble.test.spec.Connections.DEVICE_CONNECTION
import no.nordicsemi.andorid.ble.test.spec.Connections.SERVICE_DISCOVERY
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConditionalWaitRequest
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend
import javax.inject.Inject

@SuppressLint("MissingPermission")
class ServerConnection @Inject constructor(
    context: Context,
    private val scope: CoroutineScope,
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
        server.getService(Characteristics.UUID_SERVICE_DEVICE)?.let { service ->
            serverCharacteristics = service.getCharacteristic(Characteristics.WRITE_CHARACTERISTIC)
            indicationCharacteristics = service.getCharacteristic(Characteristics.IND_CHARACTERISTIC)
            reliableCharacteristics = service.getCharacteristic(Characteristics.REL_WRITE_CHARACTERISTIC)
            readCharacteristics = service.getCharacteristic(Characteristics.READ_CHARACTERISTIC)
        }
    }

    override fun initialize() {
        requestMtu(512).enqueue()
        // Waits until the mtu size exceeds 23. When the mtu size increases, it assigns the (mtu - 3) to the maxLength.
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
    suspend fun connectDevice(device: BluetoothDevice) {
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

   /**
    * Write callback [BleManager.setWriteCallback]. It waits until value change in the given characteristics is observed [BleManager.waitForWrite].
    */
    fun testWriteCallback(): ValueChangedCallback {
        return setWriteCallback(serverCharacteristics)
    }

   /**
    * Waits until the Indication is enabled by the remote device [BleManager.waitUntilIndicationsEnabled].
    */
    fun testWaiUntilIndicationEnabled(): ConditionalWaitRequest<BluetoothGattCharacteristic> {
        return waitUntilIndicationsEnabled(indicationCharacteristics)
    }

    /**
     * Sends an Indication [BleManager.sendIndication] response.
    */
    fun testSendIndication(request: ByteArray): WriteRequest {
        return sendIndication(indicationCharacteristics, request)
    }

    /**
     * Waits until the notification is enabled [BleManager.waitUntilNotificationsEnabled] by the remote device.
     * Once enabled, it sends notification [BleManager.sendNotification] in the [WriteRequest.then] callback.
     * To ensure efficient data transfer, it utilizes [WriteRequest.split] to cut the data into smaller packets using [HeaderBasedPacketSplitter].
     * Finally, it employs [WriteRequest.done] to handle successful completion or [WriteRequest.fail] to handle any potential failure of the operation.
     */
    suspend fun testWaitNotificationEnabled(request: ByteArray) {
        waitUntilNotificationsEnabled(serverCharacteristics)
          .then {
                sendNotification(serverCharacteristics, request)
                    .split(HeaderBasedPacketSplitter())
                    .done {scope.launch { _testCase.emit(TestCase(SEND_NOTIFICATION, true)) } }
                    .fail { _, _ ->
                        scope.launch {_testCase.emit(TestCase(SEND_NOTIFICATION,false))}}
                    .enqueue()
            }
            .suspend()
    }

    /**
     * Sends a Notification [BleManager.sendNotification] response.
     */
    fun testSendNotification(request: ByteArray): WriteRequest {
         return sendNotification(serverCharacteristics, request)
    }

   /**
    * Sets write callback [BleManager.setWriteCallback].
    */
    fun testWriteCallbackWithMTUMerger(): ValueChangedCallback {
        return setWriteCallback(serverCharacteristics)
    }

    /**
     * Handles values changes in the [BleManager.beginReliableWrite] procedure initiated by the remote device.
     */
    fun testReliableWriteCallback() {
        waitForWrite(reliableCharacteristics).enqueue()
    }

    /**
     * Facilitates the transfer of data by setting the specified data to the readable characteristic using [BleManager.setCharacteristicValue].
     * To ensure that the remote device has received the read data, it employs [BleManager.waitForRead],
     * which waits for the remote device to receive the readable data before proceeding.
     */
    fun testSetReadCharacteristics(request: ByteArray) {
        setCharacteristicValue(readCharacteristics, request).enqueue()
    }

    /**
     * Handles values changes in the [BleManager.beginAtomicRequestQueue] procedure initiated by the remote device.
     */
    fun testBeginAtomicRequestQueue(atomicRequest: ByteArray) {
        waitForWrite(serverCharacteristics).enqueue()
        waitForRead(readCharacteristics, atomicRequest).enqueue()
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