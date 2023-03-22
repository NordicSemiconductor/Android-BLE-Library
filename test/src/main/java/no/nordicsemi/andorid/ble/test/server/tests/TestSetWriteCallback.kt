package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WRITE_CALLBACK

class TestSetWriteCallback(
    private val serverConnection: ServerConnection,
) : TaskManager {
    // Start the task
    override suspend fun start() {
        serverConnection.testWriteCallback()
    }

    override fun taskName(): String {
        return WRITE_CALLBACK
    }
}