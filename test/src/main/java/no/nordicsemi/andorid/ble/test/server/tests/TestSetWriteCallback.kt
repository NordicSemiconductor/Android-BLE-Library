package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WRITE_CALLBACK

/**
 * Tests the write callback.
 */
class TestSetWriteCallback(
    private val serverConnection: ServerConnection,
) : TaskManager {

    override suspend fun start() {
        serverConnection.testWriteCallback()
    }

    override fun taskName(): String {
        return WRITE_CALLBACK
    }
}