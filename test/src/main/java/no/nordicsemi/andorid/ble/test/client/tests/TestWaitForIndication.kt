package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.WAIT_FOR_INDICATION_CALLBACK
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

class TestWaitForIndication : TaskManager {

    /**
     * Waits until indication response is received and [FlagBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start(clientConnection: ClientConnection) {
        clientConnection.testWaitForIndication()
            .merge(FlagBasedPacketMerger())
            .suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_FOR_INDICATION_CALLBACK, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_FOR_INDICATION_CALLBACK, false)
    }
}