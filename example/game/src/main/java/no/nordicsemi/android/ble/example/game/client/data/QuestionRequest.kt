package no.nordicsemi.android.ble.example.game.client.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toQuestion
import no.nordicsemi.android.ble.example.game.server.data.Players
import no.nordicsemi.android.ble.example.game.server.data.Results
import no.nordicsemi.android.ble.example.game.server.data.toPlayers
import no.nordicsemi.android.ble.example.game.server.data.toResults
import no.nordicsemi.android.ble.response.ReadResponse

/**
 * This class decodes the received packet using Protobuf.
 */
class Request : ReadResponse() {
    var userJoined: Players? = null
    var question: Question? = null
    var answerId: Int? = null
    var isGameOver: Boolean? = null
    var result: Results? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.PLAYERS -> { userJoined = request.players?.toPlayers() }
            OpCodeProto.NEW_QUESTION -> { question = request.question?.toQuestion() }
            OpCodeProto.RESPONSE -> { answerId = request.answerId }
            OpCodeProto.GAME_OVER -> { isGameOver = request.isGameOver }
            OpCodeProto.RESULT -> { result = request.results?.toResults() }
            else -> {}
        }
    }
}