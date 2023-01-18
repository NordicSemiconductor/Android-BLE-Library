package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestReliableWrite : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testReliableWriteCallback()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(RELIABLE_WRITE, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(RELIABLE_WRITE, false)
    }
}