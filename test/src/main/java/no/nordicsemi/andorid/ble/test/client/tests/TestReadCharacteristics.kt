package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.READ_CHA
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestReadCharacteristics : TaskManager {
    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testReadCharacteristics().suspend()
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