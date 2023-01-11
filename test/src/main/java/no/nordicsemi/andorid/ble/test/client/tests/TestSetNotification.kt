package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.SET_NOTIFICATION
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TestSetNotification : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testSetNotification()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(SET_NOTIFICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(SET_NOTIFICATION, false)
    }
}