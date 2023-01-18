package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.data.ATOMIC_REQUEST_QUEUE
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestBeginAtomicRequestQueue : TaskManager {

    // Start the task
    override suspend fun start(
        clientConnection: ClientConnection
    ) {
        clientConnection.testBeginAtomicRequestQueue().suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(ATOMIC_REQUEST_QUEUE, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(ATOMIC_REQUEST_QUEUE, false)
    }
}