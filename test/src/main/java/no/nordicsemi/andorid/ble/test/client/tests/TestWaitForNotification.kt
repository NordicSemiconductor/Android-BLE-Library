package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WAIT_FOR_NOTIFICATION_CALLBACK
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger
import no.nordicsemi.android.ble.ktx.suspend

class TestWaitForNotification : TaskManager {

    /**
     * Waits until notification response is received and [HeaderBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start(clientConnection: ClientConnection) {
        clientConnection.testWaitForNotification()
            .merge(HeaderBasedPacketMerger())
            .suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WAIT_FOR_NOTIFICATION_CALLBACK, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WAIT_FOR_NOTIFICATION_CALLBACK, false)
    }
}