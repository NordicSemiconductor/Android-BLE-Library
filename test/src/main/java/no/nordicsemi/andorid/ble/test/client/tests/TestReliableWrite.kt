package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.spec.Requests

/**
 * Writes and reads a characteristic value to the remote device using reliable write.
 */
class TestReliableWrite(
    private val clientConnection: ClientConnection
) : TaskManager {

    override suspend fun start() {
        clientConnection
            .testReliableWrite(Requests.reliableRequest)
    }

    override fun taskName(): String {
        return RELIABLE_WRITE
    }
}