package no.nordicsemi.andorid.ble.test.spec

import no.nordicsemi.android.ble.data.DataStream
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class HeaderBasedPacketMergerTest {
    @Test
    fun testMerge() {
        val firstPacket = byteArrayOf(0, 5, 1, 2, 3)
        val lastPacket = byteArrayOf(4, 5)
        val outputs: DataStream = object : DataStream() {}
        val expectedResult = byteArrayOf(1, 2, 3, 4, 5)

        val headerBasedPacketMerger = HeaderBasedPacketMerger()
        headerBasedPacketMerger.merge(outputs, firstPacket, 0)
        headerBasedPacketMerger.merge(outputs, lastPacket, 1)

        assertArrayEquals(expectedResult, outputs.toByteArray())
    }

}