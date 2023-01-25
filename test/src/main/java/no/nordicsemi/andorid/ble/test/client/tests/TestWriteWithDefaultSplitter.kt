package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.DEFAULT_MTU_SPLITTER
import no.nordicsemi.andorid.ble.test.client.data.splitterRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend

class TestWriteWithDefaultSplitter : TaskManager {

    /**
     * Writes the request data to the given characteristics. It utilizes the [WriteRequest.split] callback
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        val requestToSend = clientConnection.checkSizeOfRequest(splitterRequest)
        clientConnection.testWrite(requestToSend)
            .split()
            .suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(DEFAULT_MTU_SPLITTER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(DEFAULT_MTU_SPLITTER, false)
    }
}
