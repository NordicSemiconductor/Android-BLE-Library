package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

class TestNotification(
    private val clientConnection: ClientConnection
) : TaskManager {
    /**
     * Enable Notification and waits until notification response is received and [HeaderBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start() {
        clientConnection.testEnableNotification().suspend()
        clientConnection.testWaitForNotification()
            .merge(HeaderBasedPacketMerger())
            .suspend()
    }

    // Return task name
    override fun taskName(): String {
        val indications = listOf(
            Callbacks.ENABLE_NOTIFICATION,
            Callbacks.WAIT_FOR_NOTIFICATION_CALLBACK
        )
        return indications.joinToString(separator = "\n")
    }

}