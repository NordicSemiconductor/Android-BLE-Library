package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.REMOVE_NOTIFICATION_CALLBACK
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestRemoveNotificationCallback : TaskManager {

    // Start the task
    override suspend fun start(clientConnection: ClientConnection) {
        clientConnection.testRemoveNotificationCallback()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
       return TestCase(REMOVE_NOTIFICATION_CALLBACK, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(REMOVE_NOTIFICATION_CALLBACK, false)
    }
}