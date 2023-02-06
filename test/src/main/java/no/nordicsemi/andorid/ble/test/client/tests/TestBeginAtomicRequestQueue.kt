package no.nordicsemi.andorid.ble.test.client.tests

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ATOMIC_REQUEST_QUEUE
import no.nordicsemi.andorid.ble.test.spec.Requests.atomicRequestQueue
import no.nordicsemi.android.ble.ktx.suspend

class TestBeginAtomicRequestQueue(
    private val clientConnection: ClientConnection
) : TaskManager {

    // Start the task
    override suspend fun start() {
        clientConnection.testBeginAtomicRequestQueue(atomicRequestQueue)
            .suspend()
    }

    // Return task name
    override fun taskName(): String {
        return ATOMIC_REQUEST_QUEUE
    }

}