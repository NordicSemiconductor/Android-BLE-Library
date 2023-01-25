package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.HEADER_BASED_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger

class TestWriteWithHeaderMerger : TaskManager {

    /**
     * Observe the data written to the given characteristics and [HeaderBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallbackWithHeaderBasedMerger()
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