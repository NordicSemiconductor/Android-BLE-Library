package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.READ_CHA
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.readRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestSetReadCharacteristics : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testSetReadCharacteristics(readRequest)
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(READ_CHA, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(READ_CHA, false)
    }
}