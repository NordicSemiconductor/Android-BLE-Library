package no.nordicsemi.android.ble.example.game.client.data

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.toQuestion
import no.nordicsemi.android.ble.response.ReadResponse

class QuestionRequest: ReadResponse() {
    var question: Question? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        question = RequestProto.ADAPTER.decode(bytes).question?.toQuestion()
        Log.d("QuestionRequest", "onDataReceived: ${question?.question} ")
    }
}