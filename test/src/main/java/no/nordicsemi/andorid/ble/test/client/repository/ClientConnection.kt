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
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Connections.CONNECTED_WITH_SERVER
import no.nordicsemi.andorid.ble.test.spec.Connections.SERVICE_DISCOVERY
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.ktx.suspend
import kotlin.coroutines.resume

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
            indicationCharacteristics = service.getCharacteristic(DeviceSpecifications.IND_CHARACTERISTIC)
            reliableCharacteristics = service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            readCharacteristics = service.getCharacteristic(DeviceSpecifications.READ_CHARACTERISTIC)
        }
        return characteristic != null &&
                indicationCharacteristics != null &&
                reliableCharacteristics != null &&
                readCharacteristics != null
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

    /**
     * Writes the request data to the given characteristics [BleManager.writeCharacteristic].
     */
    fun testWrite(
        request: ByteArray,
    ): WriteRequest {
        return writeCharacteristic(characteristic, request, WRITE_TYPE_DEFAULT)
    }

    /**
     * Begin Reliable Write Request [BleManager.beginReliableWrite].
     * It will validate all write operations and will cancel the Reliable Write process if the returned
     * data does not match the data supplied. When all enqueued requests have been finished, the reliable write is automatically executed.
     * Two write requests [BleManager.setWriteCallback] have been added in this procedure and
     * when both requests are enqueued successfully, reliable write process will start automatically.
     */
    fun testReliableWrite(
        request: List<ByteArray>,
    ) {
        beginReliableWrite()
            .add(writeCharacteristic(reliableCharacteristics, request[0], WRITE_TYPE_DEFAULT))
            .add(writeCharacteristic(characteristic, request[1], WRITE_TYPE_DEFAULT))
            .enqueue()
    }

    /**
     * Sets an Indication Callback [BleManager.setIndicationCallback] to the given characteristics.
     */

    suspend fun testSetIndication() = suspendCancellableCoroutine { continuation ->
        setIndicationCallback(indicationCharacteristics)
            .also { continuation.resume(Unit) }

        continuation.invokeOnCancellation {
            removeIndicationCallback(indicationCharacteristics)
        }
    }

    /**
     * Enable Indication [BleManager.enableIndications]. It enables the indication for the given characteristics.
     */

    fun testEnableIndication(): WriteRequest {
        return enableIndications(indicationCharacteristics)
    }

    /**
     *  Wait for Indication [BleManager.waitForIndication]. It waits until the indication is sent
     *  from the remote device. Once indication is received, it triggers [WaitForReadRequest.trigger] the
     *  disable indication [BleManager.disableIndications] request for the given characteristics.
     */
    fun testWaitForIndication(): WaitForValueChangedRequest {
        return waitForIndication(indicationCharacteristics)
            .trigger(disableIndications(indicationCharacteristics))
    }

    /**
     * Sets a Notification Callback [BleManager.setNotificationCallback] to the given characteristics.
     */
    suspend fun testSetNotification()= suspendCancellableCoroutine { continuation ->
        setNotificationCallback(characteristic)
            .also { continuation.resume(Unit) }

        continuation.invokeOnCancellation {
            removeNotificationCallback(characteristic)
        }
    }

    /**
     * Enable Notification [BleManager.enableNotifications]. It enables the notification for the given characteristics.
     */
    fun testEnableNotification(): WriteRequest {
        return enableNotifications(characteristic)
    }

    /**
     *  Wait for Notification [BleManager.waitForNotification]. It waits until the notification is sent
     *  from the remote device. Once notification is received, it triggers [WaitForReadRequest.trigger] the
     *  disable notification for the given characteristics [BleManager.disableNotifications].
     */
    fun testWaitForNotification(): WaitForValueChangedRequest {
        return waitForNotification(characteristic)
            .trigger(disableNotifications(characteristic))
    }

    /**
     * Sends the read request to the given characteristic [BleManager.readCharacteristic].
     */
      fun testReadCharacteristics(): ReadRequest {
        return readCharacteristic(readCharacteristics)
    }

    /**
     * It initiates the atomic request queue [BleManager.beginAtomicRequestQueue] and it will execute the requests in the queue in the order.
     * The method has two requests and they will execute together. Thus, the method is particularly useful
     * when the user wants to execute multiple requests simultaneously and ensure they are executed together.
     */
    fun testBeginAtomicRequestQueue(request: ByteArray): RequestQueue {
        return beginAtomicRequestQueue()
            .add(readCharacteristic(readCharacteristics))
            .before { writeCharacteristic(characteristic, request, WRITE_TYPE_DEFAULT) }
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