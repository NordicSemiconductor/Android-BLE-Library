package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WRITE_CHARACTERISTICS
import no.nordicsemi.andorid.ble.test.spec.Requests.writeRequest
import no.nordicsemi.android.ble.ktx.suspend

class TestWrite(
    private val clientConnection: ClientConnection
) : TaskManager {
    // Start the task
    override suspend fun start() {
        clientConnection.testWrite(writeRequest)
            .suspend()
    }

    // Return task name
    override fun taskName(): String {
        return WRITE_CHARACTERISTICS
    }
}
