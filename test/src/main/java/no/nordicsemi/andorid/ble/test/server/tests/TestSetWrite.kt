package no.nordicsemi.andorid.ble.test.server.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.WRITE_CALLBACK
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestSetWriteCallback : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        serverConnection: ServerConnection
    ) {
        serverConnection.testWriteCallback()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WRITE_CALLBACK, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WRITE_CALLBACK, false)
    }
}