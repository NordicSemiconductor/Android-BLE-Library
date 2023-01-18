package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager

class TestBeginAtomicRequestQueue: TaskManager {
    // Start the task
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testBeginAtomicRequestQueue()
    }

    // Handle the completion
    override fun onTaskCompleted(): TestCase {
        return TestCase("Atomic request queue", true)
    }

    // Handle the failure
    override fun onTaskFailed(): TestCase {
        return TestCase("Atomic request queue", false)
    }
}