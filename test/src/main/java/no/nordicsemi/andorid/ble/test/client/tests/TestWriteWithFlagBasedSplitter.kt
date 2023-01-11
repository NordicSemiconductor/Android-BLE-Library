package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.FLAG_BASED_SPLITTER
import no.nordicsemi.andorid.ble.test.client.data.splitterRequest
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.android.ble.ktx.suspend

class TestWriteWithFlagBasedSplitter : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testWrite(splitterRequest)
            .split(FlagBasedPacketSplitter())
            .suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(FLAG_BASED_SPLITTER, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(FLAG_BASED_SPLITTER, false)
    }
}