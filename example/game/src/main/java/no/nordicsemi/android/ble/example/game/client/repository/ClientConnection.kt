package no.nordicsemi.android.ble.example.game.client.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.example.game.client.data.QuestionRequest
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.example.game.spec.PacketMerger
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    var characteristic: BluetoothGattCharacteristic? = null

    private val _question = MutableSharedFlow<Question>()
    val question = _question.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, "AAAClient", message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback: BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                characteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
            log(Log.WARN, "Supported: ${characteristic != null}")
            return characteristic != null
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            requestMtu(512).enqueue()

            setNotificationCallback(characteristic)
                .merge(PacketMerger())
                .asResponseFlow<QuestionRequest>()
                .mapNotNull { it.question }
                .onEach { _question.tryEmit(it) }
                .also { log(Log.INFO, "Receiving reply: ${it.toString()}") }
                .launchIn(scope)

            enableNotifications(characteristic).enqueue()
        }



        override fun onServicesInvalidated() {
            characteristic = null
        }
    }

    /**
     * Connects to the server.
     */
    suspend fun connect() {
        connect(device)
            .retry(4, 300)
            .useAutoConnect(false)
            .timeout(100_000)
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}