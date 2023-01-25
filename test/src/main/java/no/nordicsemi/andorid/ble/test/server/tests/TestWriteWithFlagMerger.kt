package no.nordicsemi.andorid.ble.test.server.tests

import android.util.Log
import no.nordicsemi.andorid.ble.test.server.data.FLAG_BASED_MERGER
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketMerger
import no.nordicsemi.android.ble.ValueChangedCallback

class TestWriteWithFlagMerger : TaskManager {
    private val TAG = "WithCallBack"

    /**
     * Observes the value changes on the give characteristics and [FlagBasedPacketMerger] to
     * efficiently merge and process the data sent from the remote device.
     * It also utilizes the [ValueChangedCallback.with] to monitor the size of the data and log respective messages accordingly.
     */
    override suspend fun start(serverConnection: ServerConnection) {
        serverConnection.testWriteCallbackWithFlagBasedMerger()
            .merge(FlagBasedPacketMerger())
            .with { _, data ->
                if (data.size() < 2) Log.i(TAG, "very small data of size ${data.size()}")
                else Log.i(TAG, "Data size: ${data.size()}")
            }
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(FLAG_BASED_MERGER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(FLAG_BASED_MERGER, false)
    }
}