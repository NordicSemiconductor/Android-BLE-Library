package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.SEND_NOTIFICATION
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.notificationRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.android.ble.WriteRequest

class TestSendNotification : TaskManager {

    /**
     * Sends a Notification response. It utilizes the [WriteRequest.split] callback
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testSendNotification(notificationRequest)
            .split(HeaderBasedPacketSplitter())
            .enqueue()
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