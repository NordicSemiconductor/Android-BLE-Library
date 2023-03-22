package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.READ_CHARACTERISTICS
import no.nordicsemi.andorid.ble.test.spec.Requests.readRequest

class TestSetReadCharacteristics(
    private val serverConnection: ServerConnection,
) : TaskManager {
    // Start the task
    override suspend fun start() {
        serverConnection.testSetReadCharacteristics(readRequest)
    }

    override fun taskName(): String {
        return READ_CHARACTERISTICS
    }
}