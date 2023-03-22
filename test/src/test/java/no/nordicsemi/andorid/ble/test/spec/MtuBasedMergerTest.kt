package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataStream
import org.junit.Assert.*
import org.junit.Test

class MtuBasedMergerTest {
    private val fullPacket = byteArrayOf(1, 2, 3, 4, 5, 6, 7)
    private val firstPacket = byteArrayOf(1, 2, 3, 4, 5)
    private val continuationPacket = byteArrayOf(6, 7)
    private val expectedPacket = byteArrayOf(1, 2, 3, 4, 5, 6, 7)
    private val outputs: DataStream = object : DataStream() {}

    @Test
    fun testFullMessage() {
        val mtuBasedMerger = MtuBasedMerger(maxLength = 7)
        mtuBasedMerger.merge(outputs, fullPacket, 0)
        assertArrayEquals(expectedPacket, outputs.toByteArray())
    }

    @Test
    fun testContinuationPacket() {
        val mtuBasedMerger = MtuBasedMerger(maxLength = 5)
        mtuBasedMerger.merge(outputs, firstPacket, 0)
        mtuBasedMerger.merge(outputs, continuationPacket, 1)
        assertArrayEquals(expectedPacket, outputs.toByteArray())
    }
}