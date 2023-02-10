package no.nordicsemi.andorid.ble.test.spec

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class FlagBasedPacketSplitterTest {
    private val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

    @Test
    fun testFirstPacketOfLargerMessage() {
        val splitter = FlagBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 3, index = 0)
        assertArrayEquals(byteArrayOf(Flags.BEGIN, 1, 2), chunk)
    }

    @Test
    fun testContinuationPacket() {
        val splitter = FlagBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 3, index = 1)
        assertArrayEquals(byteArrayOf(Flags.CONTINUATION, 3, 4), chunk)
    }

    @Test
    fun testLastPacket() {
        val splitter = FlagBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 11, index = 1)
        assertArrayEquals(byteArrayOf(Flags.END, 11, 12), chunk)
    }

    @Test
    fun testFullPacket() {
        val splitter = FlagBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 20, index = 0)
        assertArrayEquals(byteArrayOf(Flags.FULL) + data, chunk)
    }

    @Test
    fun testPacketAfterLastPacket() {
        val splitter = FlagBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 20, index = 1)
        assertNull(chunk)
    }
}