package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WAIT_UNTIL_INDICATION_ENABLED
import no.nordicsemi.andorid.ble.test.spec.Requests

class TestWaitIndicationsEnabled : TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWaiUntilIndicationEnabled(Requests.readRequestInTrigger)
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_UNTIL_INDICATION_ENABLED, false)
    }
}