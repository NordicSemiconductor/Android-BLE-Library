package no.nordicsemi.andorid.ble.test.server.tasks

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection

interface TaskManager {
    suspend fun start(
        scope: CoroutineScope,
        serverConnection: ServerConnection
    )

    fun onTaskCompleted(): TestCase
    fun onTaskFailed(): TestCase
}