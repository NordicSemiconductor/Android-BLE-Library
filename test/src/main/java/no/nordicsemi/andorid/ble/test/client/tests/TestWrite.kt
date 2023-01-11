package no.nordicsemi.andorid.ble.test.client.tests

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.data.WRITE_CHARACTERISTICS
import no.nordicsemi.andorid.ble.test.client.data.request
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.task.TaskManager
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.suspend

class TestWrite : TaskManager {

    override suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    ) {
        clientConnection.testWrite(request).suspend()
    }

    // Handle task completion
    override fun onTaskCompleted(): TestCase {
        return TestCase(WRITE_CHARACTERISTICS, true)
    }

    // Handle task failure
    override fun onTaskFailed(): TestCase {
        return TestCase(WRITE_CHARACTERISTICS, false)
    }
}
