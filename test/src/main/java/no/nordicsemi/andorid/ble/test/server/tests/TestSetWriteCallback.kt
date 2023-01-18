package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.WRITE_CALLBACK
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestSetWriteCallback : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallback()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WRITE_CALLBACK, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WRITE_CALLBACK, false)
    }
}