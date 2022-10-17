package no.nordicsemi.android.ble.example.game.spec

import no.nordicsemi.android.ble.data.DataSplitter
import java.nio.ByteBuffer


class PacketSplitter: DataSplitter {

    override fun chunk(message: ByteArray, index: Int, maxLength: Int): ByteArray? {
        val messageSize = message.size
        return if (index == 0) {
            val nextSize = if (messageSize + 2 <= maxLength) messageSize + 2 else maxLength

            ByteArray(nextSize).apply { ByteBuffer.wrap(this)
                .putShort(messageSize.toShort()) // 2 bytes
                .put(message, 0, size - 2) }
        } else {
            val newIndex = index * maxLength - 2
            val nextSize = if (messageSize - newIndex <= maxLength) messageSize - newIndex else maxLength
            if (nextSize <= 0)
                return null

            ByteArray(nextSize).apply { ByteBuffer.wrap(this)
                .put(message, newIndex, size) }
        }
    }

}

