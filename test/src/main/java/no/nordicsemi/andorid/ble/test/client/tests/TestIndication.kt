package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ENABLE_INDICATION
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

class TestIndication(
    private val clientConnection: ClientConnection
) : TaskManager {
    /**
     * Enables Indication and waits until indication response is received and [FlagBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start() {
        clientConnection.testEnableIndication().suspend()
        clientConnection.testWaitForIndication()
            .merge(FlagBasedPacketMerger())
            .suspend()
    }

    // Return task name
    override fun taskName(): String {
        val indications = listOf(
            ENABLE_INDICATION,
            Callbacks.WAIT_FOR_INDICATION_CALLBACK
        )
        return indications.joinToString(separator = "\n")
    }
}