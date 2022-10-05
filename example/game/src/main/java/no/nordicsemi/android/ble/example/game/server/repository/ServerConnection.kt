package no.nordicsemi.android.ble.example.game.server.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.suspend

class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    var serverCharacteristic: BluetoothGattCharacteristic? = null

    private val _replies = MutableSharedFlow<String>()
    val replies = _replies.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, "AAAServer", message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback: BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return true
        }

        override fun onServerReady(server: BluetoothGattServer) {
            log(Log.INFO, "Server ready")
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            setWriteCallback(serverCharacteristic).asFlow()
                .onEach { log(Log.WARN, "Write received: $it") }
                .mapNotNull { it.getStringValue(0) }
                .onEach { _replies.emit(it) }
                .launchIn(scope)

            // TODO
            waitUntilNotificationsEnabled(serverCharacteristic)
                .enqueue()
            // enable notifications, write user name, etc
            // setNotificationCallback()
            // enableNotifications().enqueue()
        }

        override fun onServicesInvalidated() {
            serverCharacteristic = null
        }
    }

    /**
     * Connects to the server.
     */
    suspend fun connect() {
        connect(device)
            .retry(4, 300)
            .useAutoConnect(false)
            .timeout(5_000)
            .suspend()
    }

    suspend fun reply() {
        log(Log.INFO, "Sending reply")
        sendNotification(serverCharacteristic, "world".toByteArray())
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}