package no.nordicsemi.andorid.ble.test.client.tests

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.andorid.ble.test.client.data.HEADER_BASED_SPLITTER
import no.nordicsemi.andorid.ble.test.client.data.splitterRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.WriteProgressCallback
import no.nordicsemi.android.ble.ktx.suspend

class TestWriteWithHeaderBasedSplitter : TaskManager {
    private val TAG = "WriteProgressCallback"

    /**
     * Writes the request data to the given characteristics. It utilizes the [WriteRequest.split] callback with [HeaderBasedPacketSplitter]
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation. The [WriteProgressCallback] is used to observe the
     * packet on each time a packet has been sent.
     */
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testWrite(splitterRequest)
            .split(
                HeaderBasedPacketSplitter(), object : WriteProgressCallback{
                    override fun onPacketSent(
                        device: BluetoothDevice,
                        data: ByteArray?,
                        index: Int
                    ) {
                        Log.i(TAG, "onPacketSent: Packet size ${data?.size} and index $index ")
                    }
                })
            .suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(HEADER_BASED_SPLITTER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(HEADER_BASED_SPLITTER, false)
    }
}
