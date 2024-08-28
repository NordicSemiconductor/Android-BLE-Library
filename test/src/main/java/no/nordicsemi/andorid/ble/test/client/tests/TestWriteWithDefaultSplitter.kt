package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Flags.DEFAULT_MTU_SPLITTER
import no.nordicsemi.andorid.ble.test.spec.Requests.splitterRequest
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Writes the request data to the given characteristics. It utilizes the [WriteRequest.split] callback
 * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
 */
class TestWriteWithDefaultSplitter(
    private val clientConnection: ClientConnection
) : TaskManager {

    override suspend fun start() {
        val requestToSend = clientConnection.checkSizeOfRequest(splitterRequest)
        clientConnection
            .testWrite(requestToSend)
            .split()
            .suspend()
    }

    override fun taskName(): String {
        return DEFAULT_MTU_SPLITTER
    }
}
