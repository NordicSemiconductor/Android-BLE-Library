package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.FLAG_BASED_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger

class TestWriteWithFlagMerger : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallback()
            .merge(FlagBasedPacketMerger())
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(FLAG_BASED_MERGER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(FLAG_BASED_MERGER, false)
    }
}