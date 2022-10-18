package no.nordicsemi.android.ble.example.game.client.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toQuestion
import no.nordicsemi.android.ble.response.ReadResponse

class Request: ReadResponse() {
    enum class Type {
        QUESTION,
        ANSWER,
        GAME_OVER,
    }
    var type: Type? = null
    var answerId: Int? = null
    var question: Question? = null


    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.NEW_QUESTION -> {
                type = Type.QUESTION
                question = request.question?.toQuestion()
            }
            OpCodeProto.RESULT -> {
                type = Type.ANSWER
                answerId = request.answerId
            }
            else -> {}
            // game over
        }

    }
}