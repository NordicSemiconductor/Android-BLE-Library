package no.nordicsemi.andorid.ble.test.spec

import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class HeaderBasedPacketSplitterTest {
    private val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)

    @Test
    fun testFirstPacket() {
        val splitter = HeaderBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 4, index = 0)
        assertArrayEquals(byteArrayOf(0, 13, 1, 2), chunk)
    }

    @Test
    fun testMiddlePacket() {
        val splitter = HeaderBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 4, index = 1)
        assertArrayEquals(byteArrayOf(3, 4, 5, 6), chunk)
    }

    @Test
    fun testLastPacket() {
        val splitter = HeaderBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 10, index = 1)
        assertArrayEquals(byteArrayOf(9, 10, 11, 12, 13), chunk)
    }

    @Test
    fun testAfterLastPacket() {
        val splitter = HeaderBasedPacketSplitter()
        val chunk = splitter.chunk(data, maxLength = 20, index = 1)
        Assert.assertNull(chunk)
    }
}