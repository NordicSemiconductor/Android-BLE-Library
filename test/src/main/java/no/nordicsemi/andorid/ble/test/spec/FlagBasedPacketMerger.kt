package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.andorid.ble.test.server.data.SplitterFlag
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream


class FlagBasedPacketMerger : DataMerger {

    /**
     * A method that merges the last packet into the output message.
     * The first byte of the last packet contains the flag and remaining bytes contains the message.
     * The merge method adds a packet to the buffer and returns true if the packet contains the FULL or END flag.
     * indicating that the message is complete or that the packet is the last one in a multi-packet message.
     * Otherwise, it returns false to indicate that more packets are expected.
     * @param output     the stream for the output message, initially empty.
     * @param lastPacket the data received in the last read/notify/indicate operation.
     * @param index      an index of the packet, 0-based.
     * @return True, if the message is complete, false if more data are expected.
     */
    override fun merge(output: DataStream, lastPacket: ByteArray?, index: Int): Boolean {
        if (lastPacket == null)
            return false
        when(lastPacket[0]){
            SplitterFlag.FULL.value -> {
                output.write(lastPacket, 1, lastPacket.size - 1)
                return true
            }
            SplitterFlag.BEGIN.value -> {
                output.write(lastPacket, 1, lastPacket.size - 1)
                return false
            }
            SplitterFlag.CONTINUATION.value -> {
                output.write(lastPacket, 1, lastPacket.size - 1)
                return false
            }
            SplitterFlag.END.value -> {
                output.write(lastPacket, 1, lastPacket.size - 1)
                return true
            }
        }
        return false
    }
}


