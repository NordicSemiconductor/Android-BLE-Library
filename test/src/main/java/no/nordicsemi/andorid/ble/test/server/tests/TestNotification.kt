package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.SEND_NOTIFICATION
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WAIT_UNTIL_NOTIFICATION_ENABLED
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.Requests
import no.nordicsemi.andorid.ble.test.spec.Requests.sendNotificationInThenCallback
import no.nordicsemi.android.ble.WriteRequest

class TestNotification(
    private val serverConnection: ServerConnection,
) : TaskManager {
    /**
     * Waits until the notification is enabled and sends a Notification response. It utilizes the [WriteRequest.split] callback
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start() {
        serverConnection.testWaitNotificationEnabled(sendNotificationInThenCallback)
        serverConnection.testSendNotification(Requests.notificationRequest)
            .split(HeaderBasedPacketSplitter())
            .enqueue()
    }

    override fun taskName(): String {
        val notifications = listOf(WAIT_UNTIL_NOTIFICATION_ENABLED, SEND_NOTIFICATION)
        return notifications.joinToString(separator = "\n")
    }
}