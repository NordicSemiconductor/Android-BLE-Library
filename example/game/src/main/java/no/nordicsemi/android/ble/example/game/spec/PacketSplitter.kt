package no.nordicsemi.android.ble.example.game.spec

import no.nordicsemi.android.ble.data.DataSplitter
import java.nio.ByteBuffer

class PacketSplitter : DataSplitter {

    /**
     * A method that splits the message and returns a index-th byte array from given message,
     * with at most maxLength size, or null if no bytes are left to be sent.
     * The first packet contains the data packet along with a two-byte header that indicates the size
     * of the message to be sent. The remaining packets contain the message up to the MTU size or
     * packet size (whichever is smaller).
     * For instance, if the message size is 600 and the MTU-512, the first packet will have
     * 510 bytes of the message along with a 2 byte header,
     * and the following packet will contain the remaining 90 bytes.
     *
     * @param message   full message to be chunked.
     * @param index     index of a packet, 0-based.
     * @param maxLength maximum length of the returned packet.
     * @return The packet to be sent, or null, if the whole message was already split.
     */
    override fun chunk(message: ByteArray, index: Int, maxLength: Int): ByteArray? {
        val messageSize = message.size
        return if (index == 0) {
            val nextSize = if (messageSize + 2 <= maxLength) messageSize + 2 else maxLength

            ByteArray(nextSize).apply {
                ByteBuffer.wrap(this)
                    .putShort(messageSize.toShort()) // 2 bytes
                    .put(message, 0, size - 2)
            }
        } else {
            val newIndex = index * maxLength - 2
            val nextSize =
                if (messageSize - newIndex <= maxLength) messageSize - newIndex else maxLength
            if (nextSize <= 0)
                return null

            ByteArray(nextSize).apply {
                ByteBuffer.wrap(this)
                    .put(message, newIndex, size)
            }
        }
    }

}

