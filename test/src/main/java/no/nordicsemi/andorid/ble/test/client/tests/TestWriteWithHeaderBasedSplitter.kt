package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.HEADER_BASED_SPLITTER
import no.nordicsemi.andorid.ble.test.client.data.splitterRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.android.ble.ktx.suspend

class TestWriteWithHeaderBasedSplitter : TaskManager {
    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testWrite(splitterRequest)
            .split(HeaderBasedPacketSplitter())
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
