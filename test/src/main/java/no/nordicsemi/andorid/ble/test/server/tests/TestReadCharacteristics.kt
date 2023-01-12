package no.nordicsemi.andorid.ble.test.server.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.READ
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.readRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.android.ble.ktx.suspend

class TestReadCharacteristics : TaskManager {
    override suspend fun start(scope: CoroutineScope, serverConnection: ServerConnection) {
        serverConnection.testSetCharacteristicValue(readRequest).suspend()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(READ, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(READ, false)
    }
}