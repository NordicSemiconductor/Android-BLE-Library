package no.nordicsemi.andorid.ble.test.server.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.WAIT_UNTIL_INDICATION_ENABLED
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestWaitNotificationEnabled : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        serverConnection: ServerConnection
    ) {
        serverConnection.testNotificationEnabled()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, false)
    }
}