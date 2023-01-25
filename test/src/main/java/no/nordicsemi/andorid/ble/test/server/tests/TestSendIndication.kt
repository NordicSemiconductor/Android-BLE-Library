package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.SEND_INDICATION
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.data.indicationRequest
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend

class TestSendIndication : TaskManager {

    /**
     * Sends an Indication response. It utilizes the [WriteRequest.split] callback
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testSendIndication(indicationRequest)
            .split(FlagBasedPacketSplitter())
            .suspend()
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