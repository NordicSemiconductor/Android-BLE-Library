package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.ENABLE_INDICATION
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestEnableIndication : TaskManager {

    // Start the tasks
    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testEnableIndication()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(ENABLE_INDICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(ENABLE_INDICATION, false)
    }

}