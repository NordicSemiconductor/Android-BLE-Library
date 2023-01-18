package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.SEND_NOTIFICATION
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.notificationRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestSendNotification : TaskManager {

    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testSendNotification(notificationRequest)
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(SEND_NOTIFICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(SEND_NOTIFICATION, false)
    }
}