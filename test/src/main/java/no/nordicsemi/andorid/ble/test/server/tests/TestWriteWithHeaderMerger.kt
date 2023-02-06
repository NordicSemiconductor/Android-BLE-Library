package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Flags.HEADER_BASED_MERGER
import no.nordicsemi.andorid.ble.test.spec.HeaderBasedPacketMerger

class TestWriteWithHeaderMerger(
    private val serverConnection: ServerConnection,
) : TaskManager {

    /**
     * Observe the data written to the given characteristics and [HeaderBasedPacketMerger] to
     * efficiently merge and process the data received from the remote device.
     */
    override suspend fun start() {
        serverConnection.testWriteCallback()
            .merge(HeaderBasedPacketMerger())
    }

    override fun taskName(): String {
        return HEADER_BASED_MERGER
    }
}