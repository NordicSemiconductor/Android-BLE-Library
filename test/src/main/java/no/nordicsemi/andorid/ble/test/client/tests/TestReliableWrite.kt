package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.client.data.reliableRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestReliableWrite : TaskManager {
    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testReliableWrite(reliableRequest).suspend()
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