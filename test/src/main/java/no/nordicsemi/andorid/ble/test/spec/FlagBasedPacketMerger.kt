package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.andorid.ble.test.spec.Flags.BEGIN
import no.nordicsemi.andorid.ble.test.spec.Flags.CONTINUATION
import no.nordicsemi.andorid.ble.test.spec.Flags.END
import no.nordicsemi.andorid.ble.test.spec.Flags.FULL
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import java.nio.ByteBuffer

class FlagBasedPacketMerger : DataMerger {

    /**
     * A method that merges the last packet into the output message.
     * The first byte of the each packet contains the flag and remaining bytes contains the message.
     * The merge method adds a packet to the output stream and returns true if the packet contains the FULL or END flag, which
     * specifies that the message is complete. If a packet contains BEGIN or CONTINUATION flag,
     * it indicates that the packet is the beginning or continuation of a multi-packet message and returns false.
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

        when (lastPacket[0]) {
            FULL -> {
                ByteArray(lastPacket.size - 1)
                    .apply { buffer.get(this) }
                    .also { output.write(it) }
                    .let { return true }
            }
            BEGIN -> {
                ByteArray(lastPacket.size - 1)
                    .apply { buffer.get(this) }
                    .also { output.write(it) }
                    .let { return false }
            }
            CONTINUATION -> {
                ByteArray(lastPacket.size - 1)
                    .apply { buffer.get(this) }
                    .also { output.write(it) }
                    .let { return false }
            }
            END -> {
                ByteArray(lastPacket.size - 1)
                    .apply { buffer.get(this) }
                    .also { output.write(it) }
                    .let { return true }
            }
        }
        return false
    }
}


