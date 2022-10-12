package no.nordicsemi.android.ble.example.game.spec

import android.util.Log
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import java.nio.ByteBuffer

class PacketMerger: DataMerger {
    var expectedSize = 0

    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        Log.d("PacketMerger", "last packet: $lastPacket index: $index")
        if (lastPacket == null)
            return false

        val buffer = ByteBuffer.wrap(lastPacket)
        if (index == 0) {
            expectedSize = buffer.short.toInt()
            Log.d("PacketMerger", "expected size: ")
        }

        if (buffer.remaining() == expectedSize) {
            ByteArray(expectedSize)
                .apply { buffer.get(this) }
                .also { output.write(it) }
                .let { return true }
        } else {
            ByteArray(buffer.remaining())
                .apply { buffer.get(this) }
                .also { output.write(it) }
                .let { return false }
        }
    }

}