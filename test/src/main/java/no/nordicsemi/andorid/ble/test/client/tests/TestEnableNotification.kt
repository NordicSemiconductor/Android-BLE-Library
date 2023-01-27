package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ENABLE_NOTIFICATION
import no.nordicsemi.android.ble.ktx.suspend

class TestEnableNotification : TaskManager {

    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testEnableNotification().suspend()
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