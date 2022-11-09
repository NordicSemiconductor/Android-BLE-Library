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
import no.nordicsemi.android.ble.example.game.server.data.Players
import no.nordicsemi.android.ble.example.game.server.data.Results
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.example.game.spec.PacketMerger
import no.nordicsemi.android.ble.example.game.spec.PacketSplitter
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice?,
) : BleManager(context) {
    private val TAG = "Client Connection"
    var characteristic: BluetoothGattCharacteristic? = null

    private val _userJoined = MutableSharedFlow<Players>()
    val userJoined = _userJoined.asSharedFlow()
    private val _question = MutableSharedFlow<Question>()
    val question = _question.asSharedFlow()
    private val _answer = MutableSharedFlow<Int>()
    val answer = _answer.asSharedFlow()
    private val _isGameOver = MutableSharedFlow<Boolean>()
    val isGameOver = _isGameOver.asSharedFlow()
    private val _result = MutableSharedFlow<Results>()
    val result = _result.asSharedFlow()
    private val _answer = MutableSharedFlow<Int>()
    val answer = _answer.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
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
                    it.userJoined?.let { userJoined -> _userJoined.emit(userJoined) }
                    it.question?.let { question -> _question.emit(question) }
                    it.answerId?.let { answer -> _answer.emit(answer) }
                    it.isGameOver?.let { isGameOver -> _isGameOver.emit(isGameOver) }
                    it.result?.let { results -> _result.emit(results) }
                }
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
        device?.let {
            connect(it)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(100_000)
                .suspend()
        }
    }

    /**
     * Send selected answer id to the server.
     */
    suspend fun sendSelectedAnswer(answer: Int) {
        val result =
            RequestProto(
                OpCodeProto.RESULT, answerId = answer)
        val resultByteArray = result.encode()
        writeCharacteristic(
            characteristic,
            resultByteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send device name to the server.
     */
    suspend fun sendPlayersName(name: String) {
        val playersName = RequestProto(OpCodeProto.NAME, name = name)
        val deviceNameByteArray = playersName.encode()
        writeCharacteristic(
            characteristic,
            deviceNameByteArray,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
            .split(PacketSplitter())
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}