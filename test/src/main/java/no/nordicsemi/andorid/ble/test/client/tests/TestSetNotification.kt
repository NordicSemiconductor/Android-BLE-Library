package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.SET_NOTIFICATION
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestSetNotification : TaskManager {
    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testSetNotification()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(SET_NOTIFICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(SET_NOTIFICATION, false)
    }
}