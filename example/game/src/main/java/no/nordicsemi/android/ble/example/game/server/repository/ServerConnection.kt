package no.nordicsemi.android.ble.example.game.server.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toProto
import no.nordicsemi.android.ble.example.game.server.data.*
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications
import no.nordicsemi.android.ble.example.game.spec.PacketMerger
import no.nordicsemi.android.ble.example.game.spec.PacketSplitter
import no.nordicsemi.android.ble.ktx.asResponseFlow
import no.nordicsemi.android.ble.ktx.suspend

class ServerConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
): BleManager(context) {
    var serverCharacteristic: BluetoothGattCharacteristic? = null
    private val TAG = "Server Connection"

    private val _playersName = MutableSharedFlow<String>()
    val playersName = _playersName.asSharedFlow()
    private val _clientAnswer = MutableSharedFlow<Int>()
    val clientAnswer = _clientAnswer.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
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
            server.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                serverCharacteristic = service.getCharacteristic(DeviceSpecifications.UUID_MSG_CHARACTERISTIC)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            setWriteCallback(serverCharacteristic)
                // Merges packets until the entire text is present in the stream [PacketMerger.merge].
                .merge(PacketMerger())
                .asResponseFlow<QuestionResponse>()
                .onEach {
                    it.name?.let { name ->  _playersName.emit(name)}
                    it.selectedAnswerId?.let { result ->  _clientAnswer.emit(result) }
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

    /**
     * Send result after game over. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun gameOver(isGameOver: Boolean) {
        val request = RequestProto(OpCodeProto.GAME_OVER, isGameOver = isGameOver)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send correct answer id. The data is split into MTU size packets using
     * packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendCorrectAnswerId(correctAnswerId: Int) {
        val request = RequestProto(OpCodeProto.RESPONSE, answerId = correctAnswerId)
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send question. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendQuestion(question: Question) {
        val request = RequestProto(OpCodeProto.NEW_QUESTION, question = question.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send players name to all clients to eliminate duplicates. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendNameToAllPlayers(players: Players) {
        val request = RequestProto(OpCodeProto.PLAYERS, players = players.toProto())
        val requestByteArray = request.encode()
        sendNotification(serverCharacteristic, requestByteArray)
            .split(PacketSplitter())
            .suspend()
    }

    /**
     * Send final result and scores to all clients. The data is split into MTU size
     * packets using packet splitter [PacketSplitter.chunk] before sending it to the client.
     */
    suspend fun sendResult(results: Results) {
        val request = RequestProto(OpCodeProto.RESULT, results = results.toProto())
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