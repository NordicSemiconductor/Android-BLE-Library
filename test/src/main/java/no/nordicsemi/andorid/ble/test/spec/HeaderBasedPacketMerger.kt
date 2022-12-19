package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import java.nio.ByteBuffer

class HeaderBasedPacketMerger: DataMerger {
    var expectedSize = 0

    /**
     * A method that merges the last packet into the output message.
     * If its a fist packet, the first two bytes of the packet contain a header which includes the expected size
     * of the message to be received. The remaining bytes of the packet contain the message, which is copied to the output stream.
     * For all subsequent packets, the message bytes are simply copied to the output stream
     * until the size of the packet received matches the expected size of the message delivered through header file.
     *
     * @param output     the stream for the output message, initially empty.
     * @param lastPacket the data received in the last read/notify/indicate operation.
     * @param index      an index of the packet, 0-based.
     * @return True, if the message is complete, false if more data are expected.
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        if (lastPacket == null)
            return false

        val buffer = ByteBuffer.wrap(lastPacket)
        if (index == 0) {
            expectedSize = buffer.short.toInt()
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