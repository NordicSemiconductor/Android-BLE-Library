package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.client.data.reliableRequest
import no.nordicsemi.andorid.ble.test.client.data.secondReliableRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestReliableWrite : TaskManager {
    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testReliableWrite(listOf( reliableRequest, secondReliableRequest))
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