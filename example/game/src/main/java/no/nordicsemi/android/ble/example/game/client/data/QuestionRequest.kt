package no.nordicsemi.android.ble.example.game.client.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toQuestion
import no.nordicsemi.android.ble.example.game.server.data.Results
import no.nordicsemi.android.ble.example.game.server.data.toResultToClient
import no.nordicsemi.android.ble.response.ReadResponse

/**
 * A ReadResponse class that returns the data received and the device from which data
 * were read.
 */
class Request : ReadResponse() {
    var answerId: Int? = null
    var question: Question? = null
    var result: Results? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.NEW_QUESTION -> { question = request.question?.toQuestion() }
            OpCodeProto.RESULT -> { answerId = request.answerId }
            OpCodeProto.GAME_OVER -> { result = request.resultToClient?.toResultToClient() }
            else -> {}
        }
    }
}