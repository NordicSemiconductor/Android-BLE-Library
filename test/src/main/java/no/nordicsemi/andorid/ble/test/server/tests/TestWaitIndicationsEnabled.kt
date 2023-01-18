package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.WAIT_UNTIL_INDICATION_ENABLED
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.android.ble.ktx.suspend

class TestWaitIndicationsEnabled : TaskManager {

    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWaiUntilIndicationEnabled().suspend()
    }

    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, true)
    }

    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, false)
    }
}