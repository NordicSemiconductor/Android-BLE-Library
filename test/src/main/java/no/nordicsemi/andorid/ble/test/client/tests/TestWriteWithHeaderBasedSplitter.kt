package no.nordicsemi.andorid.ble.test.client.tests

import android.util.Log
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Flags.HEADER_BASED_SPLITTER
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.Requests.splitterRequest
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.WriteProgressCallback
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Writes the request data to the given characteristics. It utilizes the [WriteRequest.split] callback with [HeaderBasedPacketSplitter]
 * to chunk the data into multiple packets, if the data cannot be sent in a single write operation. The [WriteProgressCallback] is used to observe the
 * packet on each time a packet has been sent.
 */
class TestWriteWithHeaderBasedSplitter(
    private val clientConnection: ClientConnection
) : TaskManager {
    private val TAG = "WriteProgressCallback"

    override suspend fun start() {
        clientConnection
            .testWrite(splitterRequest)
            .split(HeaderBasedPacketSplitter()) { _, data, index ->
                Log.i(
                    TAG,
                    "onPacketSent: Packet size ${data?.size} and index $index "
                )
            }
            .suspend()
    }

    // Return task name
    override fun taskName(): String {
        return HEADER_BASED_SPLITTER
    }
}
