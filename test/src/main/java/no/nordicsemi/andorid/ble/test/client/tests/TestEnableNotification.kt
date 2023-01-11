package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.ENABLE_NOTIFICATION
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestEnableNotification : TaskManager {

    // Start the task
    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testEnableNotification()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(ENABLE_NOTIFICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(ENABLE_NOTIFICATION, false)
    }

}