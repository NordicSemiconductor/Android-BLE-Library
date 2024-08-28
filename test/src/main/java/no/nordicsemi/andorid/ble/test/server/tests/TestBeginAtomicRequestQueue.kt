package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.ATOMIC_REQUEST_QUEUE
import no.nordicsemi.andorid.ble.test.spec.Requests.atomicRequest

/**
 * Begins an atomic request queue.
 */
class TestBeginAtomicRequestQueue(
    private val serverConnection: ServerConnection,
) : TaskManager {

    override suspend fun start() {
        serverConnection
            .testBeginAtomicRequestQueue(atomicRequest)
    }

    override fun taskName(): String {
        return ATOMIC_REQUEST_QUEUE
    }
}