package no.nordicsemi.andorid.ble.test.server.tasks

import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection

interface TaskManager {
    suspend fun start(
        serverConnection: ServerConnection
    )

    fun onTaskCompleted(): TestCase
    fun onTaskFailed(): TestCase
}