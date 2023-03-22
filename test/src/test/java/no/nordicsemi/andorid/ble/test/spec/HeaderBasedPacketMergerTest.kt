package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataStream
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class HeaderBasedPacketMergerTest {
    private val firstPacket = byteArrayOf(0, 5, 1, 2, 3)
    private val lastPacket = byteArrayOf(4, 5)
    private val fullPacket = byteArrayOf(0, 5, 1, 2, 3, 4, 5)
    private val outputs: DataStream = object : DataStream() {}
    private val headerBasedPacketMerger = HeaderBasedPacketMerger()
    private val expectedResult = byteArrayOf(1, 2, 3, 4, 5)

    @Test
    fun testLargerMessage() {
        headerBasedPacketMerger.merge(outputs, firstPacket, 0)
        headerBasedPacketMerger.merge(outputs, lastPacket, 1)

        assertArrayEquals(expectedResult, outputs.toByteArray())
    }

    @Test
    fun testFullMessage() {
        headerBasedPacketMerger.merge(outputs, fullPacket, 0)
        assertArrayEquals(expectedResult, outputs.toByteArray())
    }
}