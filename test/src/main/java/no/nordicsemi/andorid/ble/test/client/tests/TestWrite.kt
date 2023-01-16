package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.WRITE_CHARACTERISTICS
import no.nordicsemi.andorid.ble.test.client.data.writeRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestWrite : TaskManager {

    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testWrite(writeRequest).suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WRITE_CHARACTERISTICS, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WRITE_CHARACTERISTICS, false)
    }
}
