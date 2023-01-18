package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.HEADER_BASED_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger

class TestWriteWithHeaderMerger : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallback()
            .merge(HeaderBasedPacketMerger())
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(HEADER_BASED_MERGER, true)

    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(HEADER_BASED_MERGER, false)
    }
}