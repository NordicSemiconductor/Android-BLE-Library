package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream

class MtuBasedMerger(private val maxLength: Int) : DataMerger {

    /**
     * A method that merges the last packet into the output message.
     * The maxLength parameter specifies the maximum number of bytes that can be sent in a single write operation.
     * The method checks if the size of the last packet is less than the maxLength.
     * If it is, it indicates that this is the last chuck to be received. Otherwise, it continues to wait for additional packets.
     *
     * @param output     the stream for the output message, initially empty.
     * @param lastPacket the data received in the last read/notify/indicate operation.
     * @param index      an index of the packet, 0-based.
     * @return True, if the message is complete, false if more data are expected.
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        if (lastPacket == null) {
            return false
        }
        output.write(lastPacket)
        return lastPacket.size < maxLength
    }
}
