package no.nordicsemi.android.ble.example.game.server.data

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.example.game.proto.OpCodeProto
import no.nordicsemi.android.ble.example.game.proto.RequestProto
import no.nordicsemi.android.ble.response.ReadResponse

/**
 * A ReadResponse class that returns the data received and the device from which data
 * were read.
 */
class QuestionResponse : ReadResponse() {
    var name: String? = null
    var result: ClientResult? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val bytes = data.value!!
        val request = RequestProto.ADAPTER.decode(bytes)
        when (request.opCode) {
            OpCodeProto.NAME -> { name = request.name }
            OpCodeProto.RESULT -> { result = request.result?.toResult() }
            else -> {}
        }
    }
}