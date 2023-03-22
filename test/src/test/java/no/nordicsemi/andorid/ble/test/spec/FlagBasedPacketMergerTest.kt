package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataStream
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class FlagBasedPacketMergerTest {
    private val beginPacket = byteArrayOf(Flags.BEGIN, 1, 2, 3)
    private val continuationPacket = byteArrayOf(Flags.CONTINUATION, 4, 5, 6)
    private val endPacket = byteArrayOf(Flags.END, 7)
    private val fullPacket = byteArrayOf(Flags.FULL, 1, 2, 3, 4, 5, 6, 7)
    private val expectedResult = byteArrayOf(1, 2, 3, 4, 5, 6, 7)
    private val outputs: DataStream = object : DataStream() {}

    private val flagBasedPacketMerger = FlagBasedPacketMerger()

    @Test
    fun testLargerMessage() {
        flagBasedPacketMerger.merge(outputs, beginPacket, 0)
        flagBasedPacketMerger.merge(outputs, continuationPacket, 1)
        flagBasedPacketMerger.merge(outputs, endPacket, 2)
        assertArrayEquals(expectedResult, outputs.toByteArray())
    }

    @Test
    fun testFullPacket() {
        flagBasedPacketMerger.merge(outputs, fullPacket, 0)
        assertArrayEquals(expectedResult, outputs.toByteArray())
    }
}