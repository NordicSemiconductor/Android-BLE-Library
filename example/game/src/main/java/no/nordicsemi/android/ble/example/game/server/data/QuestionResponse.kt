package no.nordicsemi.android.ble.example.game.server.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.response.ReadResponse

class QuestionResponse: ReadResponse() {
    var answerId: Int? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        answerId = RequestProto.ADAPTER.decode(bytes).answerId
    }
}