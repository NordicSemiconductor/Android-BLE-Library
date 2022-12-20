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
                characteristic = service.getCharacteristic(DeviceSpecifications.WRITE_CHARACTERISTIC)
                indicationCharacteristics = service.getCharacteristic(DeviceSpecifications.Ind_CHARACTERISTIC)
                reliableCharacteristics = service.getCharacteristic(DeviceSpecifications.REL_WRITE_CHARACTERISTIC)
            }
            return characteristic != null
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
            .also { _testingFeature.emit(TestEvent(TestItem.WRITE_CHARACTERISTICS.item, true)) }
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
        val request = """
           Lorem ipsum dolor sit amet. Ab vitae odio eos veniam exercitationem qui totam provident in 
            earum eveniet sed suscipit libero est temporibus eius. Ut Quis deserunt sit ipsa earum cum 
            esse tenetur id pariatur delectus vel sapiente exercitationem est harum dolore et accusantium 
            dicta. Qui officia dolor ut provident numquam sit dolor quae sit ipsum dolores et autem rerum. 
            Est maxime nihil aut beatae excepturi ut rerum explicabo. </p><p>Et ullam expedita cum cupiditate 
            doloremque cum omnis incidunt sed dolores maxime sed voluptatibus quisquam. Qui recusandae 
            ipsam qui iste quia sit deleniti mollitia. Qui totam dolorem et ipsa dolor a architecto omnis ab 
            consectetur eveniet. Ex quae laborum id doloribus tenetur non porro dolorum et assumenda nesciunt est 
            nihil enim eos provident officiis. Est itaque nostrum vel accusantium reiciendis nam omnis sunt ad 
            autem omnis ut consequatur inventore. Cum consequatur consequatur et laudantium dolorem et enim odit.
            """.toByteArray()

        writeCharacteristic(characteristic, request, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also {
                    _testingFeature.emit(
                        TestEvent(
                            TestItem.WRITE_WITH_FLAG_BASED_SPLITTER.item,
                            true
                        )
                    )
            }
            .split(FlagBasedPacketSplitter())
            .suspend()

        writeCharacteristic(characteristic, request, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also {
                    _testingFeature.emit(
                        TestEvent(
                            TestItem.WRITE_WITH_HEADER_BASED_SPLITTER.item,
                            true
                        )
                    )
            }
            .split(HeaderBasedPacketSplitter())
            .suspend()

        // Default split
        val requestToSend = checkSizeOfRequest(request)
        writeCharacteristic(characteristic, requestToSend, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also {
                _testingFeature.emit(
                    TestEvent(
                        TestItem.WRITE_WITH_DEFAULT_MTU_SPLITTER.item,
                        true
                    )
                )
            }
            .split()
            .suspend()

        writeCharacteristic(characteristic, requestToSend, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            .also {
                _testingFeature.emit(
                    TestEvent(
                        TestItem.WRITE_WITH_DEFAULT_MTU_SPLITTER.item,
                        true
                    )
                )
            }
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
                        _testingFeature.emit(TestEvent(TestItem.CONNECTED_WITH_SERVER.item, true))
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
     * This prevents the merger function from waiting for the next packet if the last packet
     * is the maxLength.
     */
    private fun checkSizeOfRequest(request: ByteArray): ByteArray {
        val maxLength = mtu - 3
        return if (maxLength % request.size == 0) request + " ".toByteArray()
        else request
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}