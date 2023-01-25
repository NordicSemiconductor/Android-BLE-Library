package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.data.MTU_SIZE_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.MtuBasedMerger
import no.nordicsemi.android.ble.ValueChangedCallback

class TestWriteWithMtuMerger : TaskManager {

    /**
     * Combines the packets received using [MtuBasedMerger].
     * The [ValueChangedCallback.filterPacket]  is utilized to pre-screen packets before merging, discarding any that do not meet the necessary criteria.
     * Additionally, the [ValueChangedCallback.filter] is employed to further refine the data after merging, discarding any that do not meet the specified requirements.
     */
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallbackWithMTUMerger()
            .filterPacket { data -> data != null && data.size > 2 }
            .merge(MtuBasedMerger(maxLength = serverConnection.requestMaxLength()))
            .filter { data -> data != null && data.size > 1020 }
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(MTU_SIZE_MERGER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(MTU_SIZE_MERGER, false)
    }
}