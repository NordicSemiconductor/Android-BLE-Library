package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Waits until a notification is received and [HeaderBasedPacketMerger]
 * efficiently merges and processes the data received from the remote device.
 */
class TestNotification(
    private val clientConnection: ClientConnection
) : TaskManager {

    override suspend fun start() {
        clientConnection
            .testWaitForNotification()
            .merge(HeaderBasedPacketMerger())
            .trigger(
                clientConnection.testEnableNotification()
            )
            .suspend()
    }

    override fun taskName(): String {
        val indications = listOf(
            Callbacks.ENABLE_NOTIFICATION,
            Callbacks.WAIT_FOR_NOTIFICATION_CALLBACK
        )
        return indications.joinToString(separator = "\n")
    }

}