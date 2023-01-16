package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.client.data.reliableRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestReliableWrite : TaskManager {
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testReliableWrite(reliableRequest).suspend()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(RELIABLE_WRITE, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(RELIABLE_WRITE, false)
    }
}