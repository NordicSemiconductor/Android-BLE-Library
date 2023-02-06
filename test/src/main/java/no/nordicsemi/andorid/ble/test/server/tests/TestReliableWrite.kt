package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.spec.Requests

class TestReliableWrite(
    private val serverConnection: ServerConnection,
) : TaskManager {
    // Start the task
    override suspend fun start() {
        serverConnection.testReliableWriteCallback(Requests.secondReliableRequest)
    }

    override fun taskName(): String {
        return RELIABLE_WRITE
    }
}