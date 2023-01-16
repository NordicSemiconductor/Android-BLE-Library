package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.SET_INDICATION
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger

class TestSetIndication : TaskManager {

    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testSetIndication()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(SET_INDICATION, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(SET_INDICATION, false)
    }
}