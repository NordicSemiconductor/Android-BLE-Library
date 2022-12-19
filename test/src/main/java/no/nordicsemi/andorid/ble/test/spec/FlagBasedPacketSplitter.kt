package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.andorid.ble.test.server.data.SplitterFlag
import no.nordicsemi.android.ble.data.DataSplitter
import java.nio.ByteBuffer

class FlagBasedPacketSplitter : DataSplitter {

    /**
     * A method that splits the message and returns an index-th byte array from given message,
     * with at most maxLength size, or null if no bytes are left to be sent.
     * The first byte of each packet contains a flag that indicates whether the packet is the first,
     * last, or a continuation of a larger message.
     * The method first checks whether the whole message can fit into a single packet.
     * If so, it returns a new byte array containing the message with the FULL flag appended to the beginning.
     * If the message is longer, it checks whether this is the first packet of the message.
     * If so, it returns a new byte array containing the first maxLength - 1 bytes of the message,
     * with the BEGIN flag appended to the beginning.
     * If this is not the first packet, it calculates the number of bytes that have already
     * been sent and the number of bytes remaining to be sent. If all the bytes have been sent,
     * it returns null. If there are still bytes remaining, it checks whether this
     * is the last packet. If so, it returns a new byte array containing the remaining bytes of the message,
     * with the END flag appended to the beginning. Otherwise, it returns a new byte array containing the
     * next maxLength - 1 bytes of the message, with the CONTINUATION flag appended to the beginning.
     *
     * @param message   full message to be chunked.
     * @param index     index of a packet, 0-based.
     * @param maxLength maximum length of the returned packet.
     * @return The packet to be sent, or null, if the whole message was already split.
     */

    override fun chunk(message: ByteArray, index: Int, maxLength: Int): ByteArray? {
        val availableSize = maxLength - 1
        // If full message fits in a single packet
        if (message.size < availableSize) {
            return ByteArray(message.size + 1).apply {
                ByteBuffer.wrap(this)
                    .put(SplitterFlag.FULL.value)
                    .put(message)
            }
        }

        // First packet of a larger one?
        if (index == 0) {
            return ByteArray(maxLength).apply {
                ByteBuffer.wrap(this)
                    .put(SplitterFlag.BEGIN.value)
                    .put(message, 0, maxLength - 1)
            }
        }

        // Find the next chunk, starting from where we finished before.
        val bytesSent = maxLength * index
        val toBeSent = message.size - bytesSent
        if (toBeSent <= 0) return null

        // Are we sensing the last packet?
        if (toBeSent <= availableSize) {
            return ByteArray(maxLength).apply {
                ByteBuffer.wrap(this)
                    .put(SplitterFlag.END.value)
                    .put(message, bytesSent, toBeSent)
            }
        }

        // We are in the middle then!
        return ByteArray(maxLength).apply {
            ByteBuffer.wrap(this)
                .put(SplitterFlag.CONTINUATION.value)
                .put(message, bytesSent, availableSize)
        }
    }
}

