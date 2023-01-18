package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.MTU_SIZE_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.MtuBasedMerger

class TestWriteWithMtuMerger : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallback()
            .merge(MtuBasedMerger(serverConnection.requestMaxLength()))
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(MTU_SIZE_MERGER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(MTU_SIZE_MERGER, false)
    }
}