package no.nordicsemi.andorid.ble.test.server.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.SEND_INDICATION
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.indicationRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestSendIndication : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        serverConnection: ServerConnection
    ) {
        serverConnection.testSendIndication(indicationRequest)
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(SEND_INDICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(SEND_INDICATION, false)
    }
}