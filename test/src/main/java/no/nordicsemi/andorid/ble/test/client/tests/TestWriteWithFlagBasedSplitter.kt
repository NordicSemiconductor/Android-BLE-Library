package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.Flags.FLAG_BASED_SPLITTER
import no.nordicsemi.andorid.ble.test.spec.Requests.splitterRequest
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend

class TestWriteWithFlagBasedSplitter(
    private val clientConnection: ClientConnection
) : TaskManager {
    /**
     * Writes the request data to the given characteristics. It utilizes the  [WriteRequest.split] callback with [FlagBasedPacketSplitter]
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start() {
        clientConnection.testWrite(splitterRequest)
            .split(FlagBasedPacketSplitter())
            .suspend()
    }

    override fun taskName(): String {
        return FLAG_BASED_SPLITTER
    }

}