package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.SET_CHARACTERISTIC_VALUE
import no.nordicsemi.andorid.ble.test.spec.Requests.readRequest

/**
 * Reads the characteristics from the remote device.
 */
class TestSetReadCharacteristics(
    private val serverConnection: ServerConnection,
) : TaskManager {

    override suspend fun start() {
        serverConnection.testSetReadCharacteristics(readRequest)
    }

    override fun taskName(): String {
        return SET_CHARACTERISTIC_VALUE
    }
}