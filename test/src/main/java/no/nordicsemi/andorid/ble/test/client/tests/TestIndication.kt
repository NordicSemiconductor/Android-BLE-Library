package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ENABLE_INDICATION
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Waits until an indication is received and [FlagBasedPacketMerger]
 * efficiently merges and processes the data received from the remote device.
 */
class TestIndication(
    private val clientConnection: ClientConnection
) : TaskManager {

    override suspend fun start() {
        clientConnection
            .testWaitForIndication()
            .merge(FlagBasedPacketMerger())
            .trigger(
                clientConnection.testEnableIndication()
            )
            .suspend()
    }

    override fun taskName(): String {
        val indications = listOf(
            ENABLE_INDICATION,
            Callbacks.WAIT_FOR_INDICATION_CALLBACK
        )
        return indications.joinToString(separator = "\n")
    }
}