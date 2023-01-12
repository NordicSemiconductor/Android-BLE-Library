package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.READ
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestReadCharacteristics : TaskManager {
    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testRead().enqueue()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(READ, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(READ, false)
    }
}