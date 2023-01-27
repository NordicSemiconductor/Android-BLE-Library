package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.READ_CHARACTERISTICS
import no.nordicsemi.andorid.ble.test.spec.Requests.readRequest

class TestSetReadCharacteristics : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testSetReadCharacteristics(readRequest)
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(READ_CHARACTERISTICS, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(READ_CHARACTERISTICS, false)
    }
}