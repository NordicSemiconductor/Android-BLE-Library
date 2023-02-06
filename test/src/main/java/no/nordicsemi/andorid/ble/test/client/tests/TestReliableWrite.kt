package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.spec.Requests

class TestReliableWrite(
    private val clientConnection: ClientConnection
) : TaskManager {
    // Start the task
    override suspend fun start() {
        clientConnection.testReliableWrite(Requests.reliableRequest)
    }

    // Return task name
    override fun taskName(): String {
        return RELIABLE_WRITE
    }
}