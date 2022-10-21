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
import no.nordicsemi.android.ble.example.game.client.data.Request
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
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
    private val _answer = MutableSharedFlow<Int>()
    val answer = _answer.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, " AAA Client Connection", message) // TODO: Remove all logs with AAA or HHH tags
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
                .asResponseFlow<Request>()
                .onEach {
                    it.answerId?.let { answer -> _answer.emit(answer) }
                    it.question?.let { question ->  _question.emit(question) }
                }.launchIn(scope)
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

    suspend fun sendSelectedAnswer(answer:Int) {
        val result = RequestProto(OpCodeProto.RESPONSE, answerId = answer)
        val resultByteArray = result.encode()
        writeCharacteristic(
            characteristic,
            resultByteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()
    }
    fun sendDeviceName(value: String){
        writeCharacteristic(
            characteristic,
            value.toByteArray(),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}