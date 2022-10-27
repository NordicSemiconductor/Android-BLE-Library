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
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toProto
import no.nordicsemi.android.ble.example.game.server.data.QuestionResponse
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.example.game.spec.PacketMerger
import no.nordicsemi.android.ble.example.game.spec.PacketSplitter
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.example.game.client.view.Result

class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    var serverCharacteristic: BluetoothGattCharacteristic? = null

    private val _replies = MutableSharedFlow<Int>()
    val replies = _replies.asSharedFlow()

    private val _clientDeviceName = MutableSharedFlow<String>()
    val clientDeviceName = _clientDeviceName.asSharedFlow()

    private val _result = MutableSharedFlow<Result>()
    val result = _result.asSharedFlow()


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
            setWriteCallback(serverCharacteristic)
                .merge(PacketMerger())
                .asResponseFlow<QuestionResponse>()
                .onEach {
                    it.answerId?.let { answer ->  _replies.emit(answer) }
                    it.name?.let { name ->  _clientDeviceName.emit(name)}
                    it.result?.let { result ->  _result.emit(result) }
                }
                .launchIn(scope)

            waitUntilNotificationsEnabled(serverCharacteristic)
                .enqueue()
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
            .timeout(10_000)
            .suspend()
    }

    suspend fun gameStart(isGameStarted: Boolean) {
        log(Log.INFO, "Game Started : $isGameStarted")
        sendNotification(serverCharacteristic, isGameStarted.toString().toByteArray())
            .suspend()
    }

    suspend fun gameOver(isGameOver: ByteArray) {
        log(Log.INFO, "Game over!!")
        val request = RequestProto(OpCodeProto.GAME_OVER, resultToClient = resultToClient.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    suspend fun sendCorrectAnswerId(correctAnswerId: Int) {
        val request = RequestProto(OpCodeProto.RESULT, answerId = correctAnswerId)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    suspend fun sendQuestion(question: Question) {
        val request = RequestProto(OpCodeProto.NEW_QUESTION, question = question.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }

}