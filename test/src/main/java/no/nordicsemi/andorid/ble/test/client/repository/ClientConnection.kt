package no.nordicsemi.andorid.ble.test.client.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Characteristics
import no.nordicsemi.andorid.ble.test.spec.Connections.CONNECTED_WITH_SERVER
import no.nordicsemi.andorid.ble.test.spec.Connections.SERVICE_DISCOVERY
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.ktx.suspend
import javax.inject.Inject

@ViewModelScoped
class ClientConnection @Inject constructor(
    @ApplicationContext context: Context,
    private val scope: CoroutineScope,
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
        gatt.getService(Characteristics.UUID_SERVICE_DEVICE)?.let { service ->
            characteristic = service.getCharacteristic(Characteristics.WRITE_CHARACTERISTIC)
            indicationCharacteristics = service.getCharacteristic(Characteristics.IND_CHARACTERISTIC)
            reliableCharacteristics = service.getCharacteristic(Characteristics.REL_WRITE_CHARACTERISTIC)
            readCharacteristics = service.getCharacteristic(Characteristics.READ_CHARACTERISTIC)
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
     * Two requests [BleManager.setWriteCallback], [BleManager.readCharacteristic] have been added and
     * when both requests are enqueued successfully, reliable write process will start automatically.
     */
    suspend fun testReliableWrite(reliableRequest: ByteArray) {
        beginReliableWrite()
            .add(writeCharacteristic(reliableCharacteristics, reliableRequest, WRITE_TYPE_DEFAULT))
            .add(readCharacteristic(readCharacteristics))
            .suspend()
    }

    /**
     * Enable Indication [BleManager.enableIndications]. It enables the indication for the given characteristics.
     */

    fun testEnableIndication(): WriteRequest {
        return enableIndications(indicationCharacteristics)
    }

    /**
     *  Wait for Indication [BleManager.waitForIndication]. It waits until the indication is sent
     *  from the remote device. Once indication is received, then [WaitForReadRequest.then] it will
     *  disable indication [BleManager.disableIndications] request for the given characteristics.
     *  The trigger [WaitForReadRequest.trigger] will sends the read request to the given characteristics.
     */
    fun testWaitForIndication(): WaitForValueChangedRequest {
        return waitForIndication(indicationCharacteristics)
            .then { disableIndications(indicationCharacteristics) }
            .trigger(readCharacteristic(readCharacteristics))
    }

    /**
     * Enable Notification [BleManager.enableNotifications]. It enables the notification for the given characteristics.
     */
    fun testEnableNotification(): WriteRequest {
        return enableNotifications(characteristic)
    }

    /**
     *  Wait for Notification [BleManager.waitForNotification]. It waits until the notification is sent
     *  from the remote device.
     */
    fun testWaitForNotification(): WaitForValueChangedRequest {
        return waitForNotification(characteristic)
    }

    /**
     * Sends the read request to the given characteristic [BleManager.readCharacteristic].
     */
    fun testReadCharacteristics(): ReadRequest {
        return readCharacteristic(readCharacteristics)
    }

    /**
     * It initiates the atomic request queue [BleManager.beginAtomicRequestQueue] and it will execute
     * the requests in the queue in the order.
     * The method has two requests and they will execute together. The method is particularly useful
     * when the user wants to execute multiple requests simultaneously and ensure they are executed together.
     */
    fun testBeginAtomicRequestQueue(request: ByteArray): RequestQueue {
        return beginAtomicRequestQueue()
            .add(writeCharacteristic(characteristic, request, WRITE_TYPE_DEFAULT))
            .add(readCharacteristic(readCharacteristics))
    }

    /**
     * Connect with server.
     */
    suspend fun connectDevice(device: BluetoothDevice) {
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