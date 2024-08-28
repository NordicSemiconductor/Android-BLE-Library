package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.spec.Requests

/**
 * Awaits write and read operations from the remote device using reliable write.
 */
class TestReliableWrite(
    private val serverConnection: ServerConnection,
) : TaskManager {

    override suspend fun start() {
        serverConnection
            .testReliableWriteCallback(Requests.secondReliableRequest)
    }

    override fun taskName(): String {
        return RELIABLE_WRITE
    }
}