package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.READ_CHA
import no.nordicsemi.android.ble.ktx.suspend

/**
 * Reads the characteristics from the remote device.
 */
class TestReadCharacteristics(
    private val clientConnection: ClientConnection
) : TaskManager {

    override suspend fun start() {
        clientConnection
            .testReadCharacteristics()
            .suspend()
    }

    override fun taskName(): String {
        return READ_CHA
    }
}