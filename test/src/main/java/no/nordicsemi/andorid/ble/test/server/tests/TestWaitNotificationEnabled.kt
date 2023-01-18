package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.WAIT_UNTIL_NOTIFICATION_ENABLED
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.android.ble.ktx.suspend

class TestWaitNotificationEnabled : TaskManager {

    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWaitUntilNotificationEnabled().suspend()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_UNTIL_NOTIFICATION_ENABLED, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_UNTIL_NOTIFICATION_ENABLED, false)
    }
}