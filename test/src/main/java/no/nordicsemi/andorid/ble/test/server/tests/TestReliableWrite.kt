package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.RELIABLE_WRITE
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestReliableWrite : TaskManager {
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testReliableWriteCallback()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(RELIABLE_WRITE, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(RELIABLE_WRITE, false)
    }
}