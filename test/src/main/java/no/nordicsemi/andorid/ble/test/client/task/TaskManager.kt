package no.nordicsemi.andorid.ble.test.client.task

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.data.TestCase

interface TaskManager {
    suspend fun start(
        scope: CoroutineScope,
        clientConnection: ClientConnection
    )

    fun onTaskCompleted(): TestCase
    fun onTaskFailed(): TestCase
}