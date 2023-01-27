package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ATOMIC_REQUEST_QUEUE
import no.nordicsemi.andorid.ble.test.spec.Requests.atomicRequest

class TestBeginAtomicRequestQueue: TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testBeginAtomicRequestQueue(atomicRequest)
    }

    // Handle the completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(ATOMIC_REQUEST_QUEUE, true)
    }

    // Handle the failure
    override fun onTaskFailed(): TestCase {
        return TestCase(ATOMIC_REQUEST_QUEUE, false)
    }
}